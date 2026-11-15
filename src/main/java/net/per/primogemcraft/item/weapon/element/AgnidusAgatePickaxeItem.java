package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
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

public class AgnidusAgatePickaxeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 9.0F, 15, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.AGNIDUS_AGATE_SLIVER);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double DROP_CHANCE = 0.15D;
    private static final int PICKUP_DELAY = 10;
    private static final String PASSIVE_ACTION = "passive";
    private static final String CRYSTAL_DUST_TEXT = "crystal_dust";
    private static final String REFINEMENT_TEXT = "refinement";

    public AgnidusAgatePickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.PYRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, CRYSTAL_DUST_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, DROP_CHANCE, true, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerWorld server) || !(miningEntity instanceof PlayerEntity player)) return result;
        if (!state.isOf(Blocks.NETHER_QUARTZ_ORE)) return result;
        if (server.getRandom().nextDouble() >= ElementWeapons.scaled(player, stack, Element.PYRO, DROP_CHANCE, true)) return result;
        var center = Vec3d.ofCenter(pos);
        var dropped = new ItemEntity(server, center.x, center.y, center.z, new ItemStack(PGCItems.ELEMENTAL_CRYSTAL_DUST.get()));
        dropped.setPickupDelay(PICKUP_DELAY);
        server.spawnEntity(dropped);
        return result;
    }
}
