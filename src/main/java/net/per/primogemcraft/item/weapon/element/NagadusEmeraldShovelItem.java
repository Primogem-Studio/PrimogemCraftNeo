package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
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
    private static final ToolMaterial TIER = new WeaponTier(1561, 2.0F, 5, WeaponTier.NETHERITE_INCORRECT,
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

    public NagadusEmeraldShovelItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.DENDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE_BLOCK, SOIL_STATE_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), Formatting.AQUA)));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, LOOT_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, LOOT_CHANCE, true, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerWorld server) || !(miningEntity instanceof PlayerEntity player)) return result;
        if (!state.isIn(BlockTags.DIRT) && !state.isOf(Blocks.DIRT_PATH)) return result;
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
        var center = Vec3d.ofCenter(pos);
        var dropped = new ItemEntity(server, center.x, center.y, center.z, new ItemStack(PGCBlocks.DENDRO_BLESSING.get()));
        dropped.setPickupDelay(PICKUP_DELAY);
        server.spawnEntity(dropped);
        return result;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null || !player.isSneaking()) return result;
        var pos = context.getBlockPos();
        var state = server.getBlockState(pos);
        if (!state.isIn(BlockTags.DIRT) && !state.isOf(Blocks.DIRT_PATH)) return result;
        var stack = context.getStack();
        player.swingHand(context.getHand());
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.DENDRO, COOLDOWN, false));
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        server.playSound(null, pos, PGCSounds.SOIL_SHAPING.get(), SoundCategory.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        var reshaped = reshaped(server.getRandom());
        if (reshaped != null) server.setBlockState(pos, reshaped.getDefaultState(), Block.NOTIFY_ALL);
        return ActionResult.SUCCESS;
    }

    private static Block reshaped(Random random) {
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

    private static void deferredPlace(ServerWorld level, BlockPos pos) {
        var server = level.getServer();
        server.execute(new ServerTask(server.getTicks() + DEFERRED_TICKS,
                () -> level.setBlockState(pos, PGCBlocks.DENDRO_BLESSING.get().getDefaultState(), Block.NOTIFY_ALL)));
    }
}
