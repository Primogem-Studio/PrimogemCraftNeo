package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
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

public class VajradaAmethystPickaxeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.5F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.VAJRADA_AMETHYST_SLIVER);

    private static final float ATTACK_DAMAGE = 4.5F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int DURABILITY_LOSS = 10;
    private static final double TRANSFORM_CHANCE = 0.5D;
    private static final double COOLDOWN_TICKS = 1200.0D;
    private static final float SOUND_VOLUME = 5.0F;
    private static final float SOUND_PITCH = 0.5F;
    private static final double AMETHYST_ORE_CHANCE = 0.05D;
    private static final double DIAMOND_CHANCE = 0.2D;
    private static final double QUARTZ_CHANCE = 0.3D;
    private static final double REDSTONE_CHANCE = 0.3D;
    private static final double PRIMOGEM_CHANCE = 0.3D;
    private static final double LAPIS_CHANCE = 0.3D;
    private static final double EMERALD_CHANCE = 0.2D;
    private static final String RIGHT_CLICK = "right_click";
    private static final String COAL_TRANSFORM_TEXT = "coal_transform";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public VajradaAmethystPickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ELECTRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, COAL_TRANSFORM_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null) return result;
        var pos = context.getBlockPos();
        var state = server.getBlockState(pos);
        if (!state.isIn(BlockTags.COAL_ORES)) return result;
        var stack = context.getStack();
        var bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos.up()));
            bolt.setCosmetic(true);
            server.spawnEntity(bolt);
        }
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        if (server.getRandom().nextDouble() < TRANSFORM_CHANCE) {
            server.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
            server.setBlockState(pos, transformed(server.getRandom(), state).getDefaultState(), 3);
        } else {
            player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.ELECTRO, COOLDOWN_TICKS, false));
        }
        return ActionResult.SUCCESS;
    }

    private static Block transformed(Random random, BlockState state) {
        var deepslate = state.isOf(Blocks.DEEPSLATE_COAL_ORE);
        if (random.nextDouble() < AMETHYST_ORE_CHANCE) return PGCBlocks.VAJRADA_AMETHYST_ORE.get();
        if (random.nextDouble() < DIAMOND_CHANCE) return deepslate ? Blocks.DEEPSLATE_DIAMOND_ORE : Blocks.DIAMOND_ORE;
        if (random.nextDouble() < QUARTZ_CHANCE) return Blocks.NETHER_QUARTZ_ORE;
        if (random.nextDouble() < REDSTONE_CHANCE) return deepslate ? Blocks.DEEPSLATE_REDSTONE_ORE : Blocks.AIR;
        if (random.nextDouble() < PRIMOGEM_CHANCE) return deepslate ? PGCBlocks.DEEPSLATE_PRIMOGEM_ORE.get() : PGCBlocks.PRIMOGEM_ORE.get();
        if (random.nextDouble() < LAPIS_CHANCE) return deepslate ? Blocks.DEEPSLATE_LAPIS_ORE : Blocks.LAPIS_ORE;
        if (random.nextDouble() < EMERALD_CHANCE) return deepslate ? Blocks.DEEPSLATE_EMERALD_ORE : Blocks.EMERALD_ORE;
        return deepslate ? Blocks.DEEPSLATE : Blocks.STONE;
    }
}
