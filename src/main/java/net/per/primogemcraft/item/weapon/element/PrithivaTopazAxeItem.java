package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponAxeItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class PrithivaTopazAxeItem extends WishWeaponAxeItem {
    private static final ToolMaterial TIER = new WeaponTier(1890, 6.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRITHIVA_TOPAZ_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 11.0F;
    private static final float ATTACK_SPEED = -3.4F;
    private static final double GOLD_CHANCE = 0.1D;
    private static final double MORA_DIVISOR = 10.0D;
    private static final int PICKUP_DELAY = 10;
    private static final String PASSIVE_ACTION = "passive";
    private static final String WOODCUTTING_TEXT = "woodcutting";
    private static final String REFINEMENT_TEXT = "refinement";

    public PrithivaTopazAxeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.GEO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, WOODCUTTING_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, GOLD_CHANCE, true, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerWorld server) || !(miningEntity instanceof PlayerEntity player)) return result;
        if (!state.isIn(BlockTags.LOGS) && !state.isIn(BlockTags.PLANKS)) return result;
        var chance = ElementWeapons.scaled(player, stack, Element.GEO, GOLD_CHANCE, true);
        if (server.getRandom().nextDouble() < chance) drop(server, pos, new ItemStack(Items.GOLD_INGOT));
        if (server.getRandom().nextDouble() < chance / MORA_DIVISOR) drop(server, pos, new ItemStack(PGCItems.REFINED_MORA.get()));
        return result;
    }

    private static void drop(ServerWorld level, BlockPos pos, ItemStack stack) {
        var center = Vec3d.ofCenter(pos);
        var dropped = new ItemEntity(level, center.x, center.y, center.z, stack);
        dropped.setPickupDelay(PICKUP_DELAY);
        level.spawnEntity(dropped);
    }
}
