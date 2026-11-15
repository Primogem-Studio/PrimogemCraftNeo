package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

public class NagadusEmeraldHoeItem extends WishWeaponHoeItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT,
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

    public NagadusEmeraldHoeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, BONE_MEAL_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack);
        apply(server, player.getBlockPos(), player, stack);
        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null) return result;
        apply(server, context.getBlockPos(), player, context.getStack());
        return ActionResult.SUCCESS;
    }

    private static void apply(ServerWorld level, BlockPos pos, PlayerEntity player, ItemStack stack) {
        var target = player.isSneaking() ? pos : (level.getBlockState(pos).isAir() ? pos.down() : null);
        if (target == null) return;
        level.playSound(null, target, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        player.getItemCooldownManager().set(stack.getItem(), COOLDOWN_TICKS);
        var boneMeal = new ItemStack(Items.BONE_MEAL);
        if (BoneMealItem.useOnFertilizable(boneMeal, level, target) || BoneMealItem.useOnGround(boneMeal, level, target, (Direction) null))
            level.syncWorldEvent(GROW_EVENT, target, 0);
        level.spawnParticles(ParticleTypes.COMPOSTER, target.getX() + 0.5D, target.getY() + 1.5D, target.getZ() + 0.5D,
                PARTICLE_COUNT, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        stack.damage(BONE_MEAL_DURABILITY, player, item -> {
        });
    }
}
