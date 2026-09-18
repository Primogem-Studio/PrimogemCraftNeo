package net.per.primogemcraft.collab.tacz;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

import static com.tacz.guns.api.event.common.GunDamageSourcePart.ARMOR_PIERCING;
import static com.tacz.guns.api.event.common.GunDamageSourcePart.NON_ARMOR_PIERCING;

/**
 * The TACZ linkage. Registered on the game event bus only while TACZ is installed, so nothing in here is
 * ever loaded — and no TACZ type is ever resolved — without it.
 */
public final class TaczBridge {
    private static final ResourceLocation SKY_GUN_ID = ResourceLocation.fromNamespaceAndPath("pgfs", "tiankong");
    private static final ElementDamageOptions PIERCING_OPTIONS = new ElementDamageOptions(true, true, false, false);
    private static final int DARKNESS_DURATION = 40;
    private static final int POISON_DURATION = 40;
    private static final int IGNITE_SECONDS = 3;
    private static final int FROZEN_TICKS = 60;

    private TaczBridge() {
    }

    static void register() {
        NeoForge.EVENT_BUS.register(TaczBridge.class);
    }

    @SubscribeEvent
    public static void onEntityHurtByGun(EntityHurtByGunEvent.Pre event) {
        if (!event.getLogicalSide().isServer() || !SKY_GUN_ID.equals(event.getGunId())) return;
        var level = event.getBullet().level();
        var elements = Element.values();
        var element = elements[level.random.nextInt(elements.length)];
        event.setDamageSource(ARMOR_PIERCING, ElementDamage.of(element, event.getDamageSource(ARMOR_PIERCING), ElementStyle.NORMAL, PIERCING_OPTIONS));
        event.setDamageSource(NON_ARMOR_PIERCING, ElementDamage.of(element, event.getDamageSource(NON_ARMOR_PIERCING), ElementStyle.NORMAL, ElementDamageOptions.DETACHED));
        if (!(event.getHurtEntity() instanceof LivingEntity entity)) return;
        applyEffect(element, entity, level);
    }

    private static void applyEffect(Element element, LivingEntity entity, Level level) {
        switch (element) {
            case ANEMO ->
                    entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_DURATION, 0, false, false));
            case ELECTRO -> {
                var bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
                bolt.moveTo(entity.position());
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            case DENDRO -> entity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, 0, false, false));
            case PYRO -> entity.igniteForSeconds(IGNITE_SECONDS);
            case CRYO -> entity.setTicksFrozen(FROZEN_TICKS);
            case GEO, HYDRO -> {
            }
        }
    }
}
