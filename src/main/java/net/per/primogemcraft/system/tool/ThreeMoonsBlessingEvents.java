package net.per.primogemcraft.system.tool;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 三月祝福：免疫摔落伤害。
 *
 * <p>1.21 通过 {@code Attributes.FALL_DAMAGE_MULTIPLIER} 属性修饰器（-1.0，MULTIPLY_BASE）
 * 把摔落伤害倍率降为 0；1.20.1 没有该属性，改为监听 {@link LivingFallEvent}，
 * 当玩家手持三月祝福时把摔落伤害倍率设为 0，行为等价。
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public final class ThreeMoonsBlessingEvents {
    private ThreeMoonsBlessingEvents() {
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) return;
        if (!player.getMainHandItem().is(PGCItems.THREE_MOONS_BLESSING.get())
                && !player.getOffhandItem().is(PGCItems.THREE_MOONS_BLESSING.get())) return;
        event.setDamageMultiplier(0.0F);
    }
}
