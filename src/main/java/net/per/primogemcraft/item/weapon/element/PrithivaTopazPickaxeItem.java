package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PrithivaTopazPickaxeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(1861, 12.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRITHIVA_TOPAZ_SLIVER, PGCItems.PRIMOGEM);

    private static final Set<Block> COBBLESTONES = Set.of(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE,
            Blocks.COBBLED_DEEPSLATE, Blocks.INFESTED_COBBLESTONE);
    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final double SUCCESS_CHANCE = 0.6D;
    private static final double DAMAGE_MAX = 10.0D;
    private static final double COOLDOWN_TICKS = 1200.0D;
    private static final double ALLOY_DIVISOR = 20.0D;
    private static final float THUNDER_VOLUME = 0.5F;
    private static final float THUNDER_PITCH = 2.0F;
    private static final float BEACON_VOLUME = 0.5F;
    private static final float BEACON_PITCH = 2.0F;
    private static final String SNEAK_USE = "sneak_use";
    private static final String TRANSMUTE_TEXT = "transmute";
    private static final String REFINEMENT_TEXT = "refinement";

    public PrithivaTopazPickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.GEO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE, TRANSMUTE_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, SUCCESS_CHANCE, true, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null || !player.isShiftKeyDown()) return result;
        var pos = context.getClickedPos();
        var state = server.getBlockState(pos);
        if (!transmutable(state)) return result;
        var stack = context.getItemInHand();
        var chance = ElementWeapons.scaled(player, stack, Element.GEO, SUCCESS_CHANCE, true);
        stack.hurtAndBreak((int) Mth.nextDouble(server.getRandom(), 1.0D, DAMAGE_MAX * chance), server, player, item -> {
        });
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.GEO, COOLDOWN_TICKS, false));
        if (server.getRandom().nextDouble() < chance) {
            server.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.BLOCKS, THUNDER_VOLUME, THUNDER_PITCH);
            if (server.getRandom().nextDouble() < chance / ALLOY_DIVISOR)
                server.setBlock(pos, PGCBlocks.CHEAP_NETHERITE_BLOCK.get().defaultBlockState(), 3);
            else
                server.setBlock(pos, goldOre(state).defaultBlockState(), 3);
            return InteractionResult.SUCCESS;
        }
        server.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, BEACON_VOLUME, BEACON_PITCH);
        return InteractionResult.SUCCESS;
    }

    private static boolean transmutable(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.STONE_BRICKS)
                || state.is(BlockTags.ANCIENT_CITY_REPLACEABLE) || COBBLESTONES.contains(state.getBlock());
    }

    private static Block goldOre(BlockState state) {
        return state.is(Blocks.POLISHED_DEEPSLATE) || state.is(BlockTags.ANCIENT_CITY_REPLACEABLE) ? Blocks.DEEPSLATE_GOLD_ORE : Blocks.GOLD_ORE;
    }
}
