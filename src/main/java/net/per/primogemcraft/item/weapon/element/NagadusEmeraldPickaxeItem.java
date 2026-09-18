package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.NAGADUS_EMERALD_SLIVER);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double DROP_CHANCE = 0.8D;
    private static final float FULL_TURN = 360.0F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String GRASS_CORE_TEXT = "grass_core";
    private static final String REFINEMENT_TEXT = "refinement";

    public NagadusEmeraldPickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.DENDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, GRASS_CORE_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, DROP_CHANCE, true, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.mineBlock(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerLevel server) || !(miningEntity instanceof Player player)) return result;
        var feet = server.getBlockState(player.blockPosition());
        if (!feet.isAir() && !feet.is(Blocks.WATER) && !feet.is(Blocks.BUBBLE_COLUMN)) return result;
        if (server.getRandom().nextDouble() >= ElementWeapons.scaled(player, stack, Element.DENDRO, DROP_CHANCE, true)) return result;
        var spawned = PGCEntities.DENDRO_CORE.get().spawn(server, pos, MobSpawnType.MOB_SUMMONED);
        if (spawned != null) spawned.setYRot(server.getRandom().nextFloat() * FULL_TURN);
        return result;
    }
}
