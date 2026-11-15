package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.recipe.RecipeType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponShovelItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class AgnidusAgateShovelItem extends WishWeaponShovelItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 14, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.AGNIDUS_AGATE_SLIVER);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double TRIGGER_CHANCE = 0.2D;
    private static final double COOLDOWN_TICKS = 100.0D;
    private static final int MAX_OUTPUT = 4;
    private static final int DURABILITY_LOSS = 1;
    private static final int PICKUP_DELAY = 10;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String SNEAK_USE_BLOCK = "sneak_use_block";
    private static final String SMELTING_TEXT = "smelting";
    private static final String REFINEMENT_TEXT = "refinement";

    public AgnidusAgateShovelItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.PYRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE_BLOCK, SMELTING_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, TRIGGER_CHANCE, true, sealed), Formatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null || !player.isSneaking()) return result;
        var pos = context.getBlockPos();
        var input = new SimpleInventory(new ItemStack(server.getBlockState(pos).getBlock()));
        var smelted = server.getRecipeManager().getFirstMatch(RecipeType.SMELTING, input, server)
                .map(recipe -> recipe.getOutput(server.getRegistryManager()).copy())
                .orElse(ItemStack.EMPTY);
        if (smelted.isEmpty() || smelted.isOf(Blocks.AIR.asItem())) return result;
        var stack = context.getStack();
        server.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        if (server.getRandom().nextDouble() < ElementWeapons.scaled(player, stack, Element.PYRO, TRIGGER_CHANCE, true)) {
            smelted.setCount(MathHelper.nextInt(server.getRandom(), 1, MAX_OUTPUT));
            var center = Vec3d.ofCenter(pos);
            var dropped = new ItemEntity(server, center.x, center.y, center.z, smelted);
            dropped.setPickupDelay(PICKUP_DELAY);
            server.spawnEntity(dropped);
        }
        server.breakBlock(pos, false);
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.PYRO, COOLDOWN_TICKS, false));
        return ActionResult.SUCCESS;
    }
}
