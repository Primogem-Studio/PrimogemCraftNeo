package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponShovelItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class NagadusEmeraldShovelItem extends WishWeaponShovelItem {
    private static final Tier TIER = new WeaponTier(1561, 2.0F, 5, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.NAGADUS_EMERALD_SLIVER);

    private static final float ATTACK_DAMAGE = 1.0F;
    private static final float ATTACK_SPEED = -1.0F;
    private static final double LOOT_CHANCE = 0.01D;
    private static final double PLACE_CHANCE = 0.5D;
    private static final int PICKUP_DELAY = 10;
    private static final int COOLDOWN = 10;
    private static final int DEFERRED_TICKS = 2;
    private static final int DURABILITY_LOSS = 1;
    private static final float SOUND_VOLUME = 0.3F;
    private static final float SOUND_PITCH = 5.0F;
    private static final double GRASS_CHANCE = 0.2D;
    private static final double ORE_CHANCE = 0.02D;
    private static final String SNEAK_USE_BLOCK = "sneak_use_block";
    private static final String SOIL_STATE_TEXT = "soil_state";
    private static final String PASSIVE_ACTION = "passive";
    private static final String LOOT_TEXT = "loot";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public NagadusEmeraldShovelItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.DENDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE_BLOCK, SOIL_STATE_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), ChatFormatting.AQUA)));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, LOOT_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, LOOT_CHANCE, true, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.mineBlock(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerLevel server) || !(miningEntity instanceof Player player)) return result;
        if (!state.is(BlockTags.DIRT) && !state.is(Blocks.DIRT_PATH)) return result;
        var granted = WeaponCharge.of(stack) > 0;
        WeaponCharge.set(stack, 1);
        if (!granted) {
            deferredPlace(server, pos);
            return result;
        }
        var chance = ElementWeapons.scaled(player, stack, Element.DENDRO, LOOT_CHANCE, true);
        if (server.getRandom().nextDouble() >= chance) return result;
        if (server.getRandom().nextDouble() < PLACE_CHANCE) {
            deferredPlace(server, pos);
            return result;
        }
        var center = Vec3.atCenterOf(pos);
        var dropped = new ItemEntity(server, center.x, center.y, center.z, new ItemStack(PGCBlocks.DENDRO_BLESSING.get()));
        dropped.setPickUpDelay(PICKUP_DELAY);
        server.addFreshEntity(dropped);
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null || !player.isShiftKeyDown()) return result;
        var pos = context.getClickedPos();
        var state = server.getBlockState(pos);
        if (!state.is(BlockTags.DIRT) && !state.is(Blocks.DIRT_PATH)) return result;
        var stack = context.getItemInHand();
        player.swing(context.getHand(), true);
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.DENDRO, COOLDOWN, false));
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        server.playSound(null, pos, PGCSounds.SOIL_SHAPING.get(), SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        var reshaped = reshaped(server.getRandom());
        if (reshaped != null) server.setBlock(pos, reshaped.defaultBlockState(), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    private static Block reshaped(RandomSource random) {
        if (random.nextDouble() < GRASS_CHANCE) return Blocks.GRASS_BLOCK;
        if (random.nextDouble() < GRASS_CHANCE) return Blocks.DIRT_PATH;
        if (random.nextDouble() < GRASS_CHANCE) return Blocks.MYCELIUM;
        if (random.nextDouble() < GRASS_CHANCE) return Blocks.DIRT;
        if (random.nextDouble() < GRASS_CHANCE) return Blocks.COARSE_DIRT;
        if (random.nextDouble() < GRASS_CHANCE) return Blocks.PODZOL;
        if (random.nextDouble() < GRASS_CHANCE) return Blocks.FARMLAND;
        if (random.nextDouble() < ORE_CHANCE) return PGCBlocks.NAGADUS_EMERALD_ORE.get();
        return null;
    }

    private static void deferredPlace(ServerLevel level, BlockPos pos) {
        var server = level.getServer();
        server.tell(new TickTask(server.getTickCount() + DEFERRED_TICKS,
                () -> level.setBlock(pos, PGCBlocks.DENDRO_BLESSING.get().defaultBlockState(), Block.UPDATE_ALL)));
    }
}
