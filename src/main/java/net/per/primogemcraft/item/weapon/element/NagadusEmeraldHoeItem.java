package net.per.primogemcraft.item.weapon.element;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

public class NagadusEmeraldHoeItem extends WishWeaponHoeItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.NAGADUS_EMERALD_SLIVER);

    private static final float ATTACK_DAMAGE = 2.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int COOLDOWN_TICKS = 2;
    private static final int BONE_MEAL_DURABILITY = 1;
    private static final int GROW_EVENT = 2005;
    private static final int PARTICLE_COUNT = 10;
    private static final double PARTICLE_SPREAD = 0.5D;
    private static final double PARTICLE_SPEED = 0.2D;
    private static final float SOUND_VOLUME = 0.7F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String BONE_MEAL_TEXT = "bone_meal";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public NagadusEmeraldHoeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, BONE_MEAL_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        apply(server, player.blockPosition(), player, stack);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null) return result;
        apply(server, context.getClickedPos(), player, context.getItemInHand());
        return InteractionResult.SUCCESS;
    }

    private static void apply(ServerLevel level, BlockPos pos, Player player, ItemStack stack) {
        var target = player.isShiftKeyDown() ? pos : (level.getBlockState(pos).isAir() ? pos.below() : null);
        if (target == null) return;
        level.playSound(null, target, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
        var boneMeal = new ItemStack(Items.BONE_MEAL);
        if (BoneMealItem.growCrop(boneMeal, level, target) || BoneMealItem.growWaterPlant(boneMeal, level, target, (Direction) null))
            level.levelEvent(GROW_EVENT, target, 0);
        level.sendParticles(ParticleTypes.COMPOSTER, target.getX() + 0.5D, target.getY() + 1.5D, target.getZ() + 0.5D,
                PARTICLE_COUNT, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        stack.hurtAndBreak(BONE_MEAL_DURABILITY, level, player, item -> {
        });
    }
}
