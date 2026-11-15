package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
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
    private static final ToolMaterial TIER = new WeaponTier(1861, 12.5F, 15, WeaponTier.DIAMOND_INCORRECT,
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

    public PrithivaTopazPickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.GEO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE, TRANSMUTE_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, SUCCESS_CHANCE, true, sealed), Formatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null || !player.isSneaking()) return result;
        var pos = context.getBlockPos();
        var state = server.getBlockState(pos);
        if (!transmutable(state)) return result;
        var stack = context.getStack();
        var chance = ElementWeapons.scaled(player, stack, Element.GEO, SUCCESS_CHANCE, true);
        stack.damage((int) MathHelper.nextDouble(server.getRandom(), 1.0D, DAMAGE_MAX * chance), player, item -> {
        });
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.GEO, COOLDOWN_TICKS, false));
        if (server.getRandom().nextDouble() < chance) {
            server.playSound(null, pos, SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.BLOCKS, THUNDER_VOLUME, THUNDER_PITCH);
            if (server.getRandom().nextDouble() < chance / ALLOY_DIVISOR)
                server.setBlockState(pos, PGCBlocks.CHEAP_NETHERITE_BLOCK.get().getDefaultState(), 3);
            else
                server.setBlockState(pos, goldOre(state).getDefaultState(), 3);
            return ActionResult.SUCCESS;
        }
        server.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, BEACON_VOLUME, BEACON_PITCH);
        return ActionResult.SUCCESS;
    }

    private static boolean transmutable(BlockState state) {
        return state.isIn(BlockTags.BASE_STONE_OVERWORLD) || state.isIn(BlockTags.STONE_BRICKS)
                || state.isIn(BlockTags.ANCIENT_CITY_REPLACEABLE) || COBBLESTONES.contains(state.getBlock());
    }

    private static Block goldOre(BlockState state) {
        return state.isOf(Blocks.POLISHED_DEEPSLATE) || state.isIn(BlockTags.ANCIENT_CITY_REPLACEABLE) ? Blocks.DEEPSLATE_GOLD_ORE : Blocks.GOLD_ORE;
    }
}
