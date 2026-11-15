package net.per.primogemcraft.item.weapon;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PhilosophiesOfGoldItem extends WishWeaponHoeItem {
    private static final ToolMaterial TIER = new WeaponTier(800, 4.0F, 5, WeaponTier.NETHERITE_INCORRECT, PGCItems.MORA_PILE);

    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -2.0F;

    public PhilosophiesOfGoldItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(PhilosophyWeapons.moraDescription(stack));
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player == null || world.isClient()) return ActionResult.PASS;
            var stack = player.getStackInHand(hand);
            if (!(stack.getItem() instanceof PhilosophiesOfGoldItem)) return ActionResult.PASS;
            var pos = hitResult.getBlockPos();
            var state = world.getBlockState(pos);
            if (!state.isIn(BlockTags.DIRT) || state.isOf(Blocks.FARMLAND) || state.isOf(Blocks.MYCELIUM)) return ActionResult.PASS;
            if (!(world instanceof ServerWorld level)) return ActionResult.PASS;
            PhilosophyWeapons.drop(level, Vec3d.ofCenter(pos), player, stack);
            return ActionResult.PASS;
        });
    }
}
