package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ShivadaJadePickaxeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.SHIVADA_JADE_SLIVER, PGCItems.PRIMOGEM);

    private static final TagKey<Block> CRYSTAL_BLOCKS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "elemental_crystal_blocks"));
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double TRANSFORM_CHANCE = 0.08D;
    private static final double DESTROY_CHANCE = 0.1D;
    private static final int PICKUP_DELAY = 10;
    private static final String PASSIVE_ACTION = "passive";
    private static final String CRYSTAL_TEXT = "crystal";
    private static final String REFINEMENT_TEXT = "refinement";

    public ShivadaJadePickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.CRYO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, CRYSTAL_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, TRANSFORM_CHANCE, true, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.mineBlock(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerLevel server) || !(miningEntity instanceof Player player)) return result;
        if (!state.is(CRYSTAL_BLOCKS)) return result;
        if (server.getRandom().nextDouble() < ElementWeapons.scaled(player, stack, Element.CRYO, TRANSFORM_CHANCE, true)) {
            server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            var center = Vec3.atCenterOf(pos);
            var dropped = new ItemEntity(server, center.x, center.y, center.z, new ItemStack(PGCBlocks.ELEMENTAL_CRYSTAL_BLOCK.get()));
            dropped.setPickUpDelay(PICKUP_DELAY);
            server.addFreshEntity(dropped);
            return result;
        }
        if (server.getRandom().nextDouble() < DESTROY_CHANCE) server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return result;
    }
}
