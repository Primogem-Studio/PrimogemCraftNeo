package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.registry.PGCEntities;
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

public class NagadusEmeraldPickaxeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.NAGADUS_EMERALD_SLIVER);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double DROP_CHANCE = 0.8D;
    private static final float FULL_TURN = 360.0F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String GRASS_CORE_TEXT = "grass_core";
    private static final String REFINEMENT_TEXT = "refinement";

    public NagadusEmeraldPickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.DENDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, GRASS_CORE_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, DROP_CHANCE, true, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerWorld server) || !(miningEntity instanceof PlayerEntity player)) return result;
        var feet = server.getBlockState(player.getBlockPos());
        if (!feet.isAir() && !feet.isOf(Blocks.WATER) && !feet.isOf(Blocks.BUBBLE_COLUMN)) return result;
        if (server.getRandom().nextDouble() >= ElementWeapons.scaled(player, stack, Element.DENDRO, DROP_CHANCE, true)) return result;
        var spawned = PGCEntities.DENDRO_CORE.get().spawn(server, pos, SpawnReason.MOB_SUMMONED);
        if (spawned != null) spawned.setYaw(server.getRandom().nextFloat() * FULL_TURN);
        return result;
    }
}
