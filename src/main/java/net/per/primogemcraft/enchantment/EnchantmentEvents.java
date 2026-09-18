package net.per.primogemcraft.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.per.primogemcraft.system.curio.CurioLoot;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class EnchantmentEvents {
    private static final int BREAK_MARGIN = 2;
    private static final int SHIELD_MARGIN = 6;
    private static final int USE_MARGIN = 3;
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private EnchantmentEvents() {
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var state = event.getState();
        if (CurioLoot.isJar(state)) CanOpener.pryOpen(player, state, event.getPos().getCenter());
        AmbrosialArborAttachment.restore(player, player.getMainHandItem(), BREAK_MARGIN);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var victim = event.getEntity();
        for (var slot : ARMOR_SLOTS) AmbrosialArborAttachment.restore(victim, victim.getItemBySlot(slot), BREAK_MARGIN);
        var source = event.getSource();
        var attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            FoolsWrath.punish(living, victim);
            TheHunt.harvest(living, victim);
        }
        var direct = source.getDirectEntity();
        if (direct instanceof LivingEntity living && direct != attacker) FoolsWrath.punish(living, victim);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        protectHands(event.getEntity(), SHIELD_MARGIN);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        protectHands(event.getEntity(), USE_MARGIN);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        protectHands(event.getEntity(), USE_MARGIN);
    }

    private static void protectHands(LivingEntity entity, int margin) {
        AmbrosialArborAttachment.restore(entity, entity.getMainHandItem(), margin);
        AmbrosialArborAttachment.restore(entity, entity.getOffhandItem(), margin);
    }
}
