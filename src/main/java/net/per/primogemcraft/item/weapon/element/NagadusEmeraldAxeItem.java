package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.structure.StructurePlacementData;
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
    private static final ToolMaterial TIER = new WeaponTier(1561, 10.0F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.NAGADUS_EMERALD_SLIVER);

    private static final Identifier TREE = Identifier.of(MOD_ID, "nagadus_emerald_tree");
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

    public NagadusEmeraldAxeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.DENDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, TREE_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!player.isSneaking()) return TypedActionResult.pass(stack);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack);
        level.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        stack.damage((int) (stack.getMaxDamage() * DURABILITY_RATIO), player, item -> {
        });
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.DENDRO, COOLDOWN, false));
        var origin = BlockPos.ofFloored(player.getX() - TREE_OFFSET, player.getY(), player.getZ() - TREE_OFFSET);
        var template = server.getStructureTemplateManager().getTemplateOrBlank(TREE);
        template.place(server, origin, origin,
                new StructurePlacementData().setRotation(BlockRotation.NONE).setMirror(BlockMirror.NONE).setIgnoreEntities(false), server.random, Block.NOTIFY_ALL);
        player.requestTeleport(player.getX(), player.getY() + TREE_HEIGHT, player.getZ());
        return TypedActionResult.success(stack);
    }
}
