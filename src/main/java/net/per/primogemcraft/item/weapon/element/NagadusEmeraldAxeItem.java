package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
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

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class NagadusEmeraldAxeItem extends WishWeaponAxeItem {
    private static final Tier TIER = new WeaponTier(1561, 10.0F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.NAGADUS_EMERALD_SLIVER);

    private static final ResourceLocation TREE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "nagadus_emerald_tree");
    private static final float ATTACK_DAMAGE = 6.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final float DURABILITY_RATIO = 0.1F;
    private static final int COOLDOWN = 1200;
    private static final int TREE_OFFSET = 3;
    private static final double TREE_HEIGHT = 10.0D;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 0.5F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String TREE_TEXT = "tree";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public NagadusEmeraldAxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.DENDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, TREE_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        level.playSound(null, player.blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        stack.hurtAndBreak((int) (stack.getMaxDamage() * DURABILITY_RATIO), server, player, item -> {
        });
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.DENDRO, COOLDOWN, false));
        var origin = BlockPos.containing(player.getX() - TREE_OFFSET, player.getY(), player.getZ() - TREE_OFFSET);
        var template = server.getStructureManager().getOrCreate(TREE);
        template.placeInWorld(server, origin, origin,
                new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false), server.random, Block.UPDATE_ALL);
        player.teleportTo(player.getX(), player.getY() + TREE_HEIGHT, player.getZ());
        return InteractionResultHolder.success(stack);
    }
}
