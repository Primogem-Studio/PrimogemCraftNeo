package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
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

public class VajradaAmethystPickaxeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(1561, 8.5F, 10, WeaponTier.DIAMOND_INCORRECT,
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

    public VajradaAmethystPickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ELECTRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, COAL_TRANSFORM_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null) return result;
        var pos = context.getClickedPos();
        var state = server.getBlockState(pos);
        if (!state.is(BlockTags.COAL_ORES)) return result;
        var stack = context.getItemInHand();
        var bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos.above()));
            bolt.setVisualOnly(true);
            server.addFreshEntity(bolt);
        }
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        if (server.getRandom().nextDouble() < TRANSFORM_CHANCE) {
            server.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
            server.setBlock(pos, transformed(server.getRandom(), state).defaultBlockState(), 3);
        } else {
            player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.ELECTRO, COOLDOWN_TICKS, false));
        }
        return InteractionResult.SUCCESS;
    }

    private static Block transformed(RandomSource random, BlockState state) {
        var deepslate = state.is(Blocks.DEEPSLATE_COAL_ORE);
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
