package net.per.primogemcraft.item.weapon;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;

import java.util.List;

public class PhilosophiesOfDiligenceItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(800, 18.0F, 5, WeaponTier.NETHERITE_INCORRECT, PGCItems.MORA_PILE);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PhilosophiesOfDiligenceItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(PhilosophyWeapons.moraDescription(stack));
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (level instanceof ServerWorld server && miningEntity instanceof PlayerEntity player && state.isOpaque())
            PhilosophyWeapons.drop(server, Vec3d.ofCenter(pos), player, stack);
        return result;
    }
}
