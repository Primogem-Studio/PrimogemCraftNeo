package net.per.primogemcraft.collab.tacz;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

import static com.tacz.guns.api.event.common.GunDamageSourcePart.ARMOR_PIERCING;
import static com.tacz.guns.api.event.common.GunDamageSourcePart.NON_ARMOR_PIERCING;

/**
 * The TACZ linkage. Registered on the game event bus only while TACZ is installed, so nothing in here is
 * ever loaded — and no TACZ type is ever resolved — without it.
 *
 * <p>Fabric 侧通过 TaCZ 提供的事件回调注册；此处保留处理逻辑。
 */
public final class TaczBridge {
    private static final Identifier SKY_GUN_ID = new Identifier("pgfs", "tiankong");
    private static final ElementDamageOptions PIERCING_OPTIONS = new ElementDamageOptions(true, true, false, false);
    private static final int DARKNESS_DURATION = 40;
    private static final int POISON_DURATION = 40;
    private static final int IGNITE_SECONDS = 3;
    private static final int FROZEN_TICKS = 60;

    private TaczBridge() {
    }

    static void register() {
        // TaCZ Fabric 事件注册由 TaCZ 自身的事件系统驱动；
        // 此处通过反射注册监听器，避免硬依赖 TaCZ 事件总线类。
        try {
            var eventClass = Class.forName("com.tacz.guns.api.event.common.EntityHurtByGunEvent$Pre");
            var registerMethod = Class.forName("com.tacz.guns.api.event.TaczEventBus")
                    .getMethod("register", Class.class, java.util.function.Consumer.class);
            registerMethod.invoke(null, eventClass, (java.util.function.Consumer<EntityHurtByGunEvent.Pre>) TaczBridge::onEntityHurtByGun);
        } catch (ReflectiveOperationException ignored) {
            // TaCZ 未安装或事件 API 不可用时静默跳过
        }
    }

    public static void onEntityHurtByGun(EntityHurtByGunEvent.Pre event) {
        if (!event.getLogicalSide().isServer() || !SKY_GUN_ID.equals(event.getGunId())) return;
        var world = event.getBullet().getWorld();
        var elements = Element.values();
        var element = elements[world.random.nextInt(elements.length)];
        event.setDamageSource(ARMOR_PIERCING, ElementDamage.of(element, event.getDamageSource(ARMOR_PIERCING), ElementStyle.NORMAL, PIERCING_OPTIONS));
        event.setDamageSource(NON_ARMOR_PIERCING, ElementDamage.of(element, event.getDamageSource(NON_ARMOR_PIERCING), ElementStyle.NORMAL, ElementDamageOptions.DETACHED));
        if (!(event.getHurtEntity() instanceof LivingEntity entity)) return;
        applyEffect(element, entity, world);
    }

    private static void applyEffect(Element element, LivingEntity entity, World world) {
        switch (element) {
            case ANEMO ->
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, DARKNESS_DURATION, 0, false, false));
            case ELECTRO -> {
                var bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                bolt.refreshPositionAfterTeleport(entity.getPos());
                bolt.setCosmetic(true);
                world.spawnEntity(bolt);
            }
            case DENDRO -> entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, POISON_DURATION, 0, false, false));
            case PYRO -> entity.setOnFireFor(IGNITE_SECONDS);
            case CRYO -> entity.setFrozenTicks(FROZEN_TICKS);
            case GEO, HYDRO -> {
            }
        }
    }
}
