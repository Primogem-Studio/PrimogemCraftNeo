package net.per.primogemcraft.item.weapon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;

import java.util.List;

public class PhilosophiesOfDiligenceItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(800, 18.0F, 5, WeaponTier.NETHERITE_INCORRECT, PGCItems.MORA_PILE);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PhilosophiesOfDiligenceItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(PhilosophyWeapons.moraDescription(stack));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.mineBlock(stack, level, state, pos, miningEntity);
        if (level instanceof ServerLevel server && miningEntity instanceof Player player && state.canOcclude())
            PhilosophyWeapons.drop(server, Vec3.atCenterOf(pos), player, stack);
        return result;
    }
}
