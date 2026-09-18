package net.per.primogemcraft.item.weapon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class PhilosophiesOfGoldItem extends WishWeaponHoeItem {
    private static final Tier TIER = new WeaponTier(800, 4.0F, 5, WeaponTier.NETHERITE_INCORRECT, PGCItems.MORA_PILE);

    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -2.0F;

    public PhilosophiesOfGoldItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(PhilosophyWeapons.moraDescription(stack));
    }

    @SubscribeEvent
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated() || event.getItemAbility() != ItemAbilities.HOE_TILL) return;
        var context = event.getContext();
        var player = event.getPlayer();
        if (player == null) return;
        var stack = context.getItemInHand();
        if (!(stack.getItem() instanceof PhilosophiesOfGoldItem)) return;
        var state = event.getState();
        if (!state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND) || state.is(Blocks.MYCELIUM)) return;
        if (!(context.getLevel() instanceof ServerLevel level)) return;
        PhilosophyWeapons.drop(level, Vec3.atCenterOf(context.getClickedPos()), player, stack);
    }
}
