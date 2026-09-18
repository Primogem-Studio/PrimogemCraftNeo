package net.per.primogemcraft.system.weapon;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.per.primogemcraft.config.PGCConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class WeaponAttributes {
    public static final EquipmentSlotGroup SLOT = EquipmentSlotGroup.MAINHAND;

    private static final ResourceLocation LEVEL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/level");
    private static final String PASSIVE_ROOT = "weapon/passive";
    private static final String PASSIVE_PREFIX = PASSIVE_ROOT + "/";

    private static final int FIRST_TIER = 30;
    private static final int SECOND_TIER = 60;
    private static final double FIRST_STEP = 0.01D;
    private static final double SECOND_STEP = 0.02D;
    private static final double THIRD_STEP = 0.03D;
    private static final double EQUILIBRIUM_FIRST = 1.0D;
    private static final double EQUILIBRIUM_SECOND = 1.5D;
    private static final double EQUILIBRIUM_THIRD = 2.0D;

    private WeaponAttributes() {
    }

    public static void refreshLevel(ItemStack stack, Player player) {
        var type = WeaponType.of(stack);
        write(stack, type.attribute(), levelAttackBonus(stack, player, WeaponState.of(stack).level()));
    }

    public static void refreshLevels(Player player) {
        var inventory = player.getInventory();
        for (var index = 0; index < inventory.getContainerSize(); index++) {
            var stack = inventory.getItem(index);
            if (stack.getItem() instanceof WishWeapon) refreshLevel(stack, player);
        }
    }

    public static double levelAttackBonus(ItemStack stack, Player player, int level) {
        var bonus = levelBonus(level, Equilibrium.of(player).bonus(), PGCConfig.WEAPON_DAMAGE_MULTIPLIER.get());
        return bonus * WeaponType.of(stack).coefficient();
    }

    public static void refreshPassive(ItemStack stack, Player player, int slot) {
        if (!(stack.getItem() instanceof WishWeapon weapon)) return;
        var refinement = WeaponEnhancement.refinementOf(player, stack);
        var modifiers = weapon.allPassives(player, stack, slot, refinement);
        var target = new LinkedHashMap<ResourceLocation, Applied>(modifiers.size());
        for (var index = 0; index < modifiers.size(); index++) {
            var modifier = modifiers.get(index);
            var id = ResourceLocation.fromNamespaceAndPath(MOD_ID, PASSIVE_PREFIX + index);
            target.put(id, new Applied(modifier.attribute(), modifier.amount(player, stack, refinement), modifier.operation()));
        }
        var current = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (matches(current, target)) return;
        var builder = ItemAttributeModifiers.builder();
        for (var entry : current.modifiers())
            if (isPassive(entry)) builder.add(entry.attribute(), entry.modifier(), entry.slot());
        for (var change : target.entrySet())
            builder.add(change.getValue().attribute(),
                    new AttributeModifier(change.getKey(), change.getValue().amount(), change.getValue().operation()), SLOT);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    public static boolean restoreDurability(ItemStack stack, int amount) {
        if (amount <= 0 || !stack.isDamageableItem()) return false;
        var damage = stack.getDamageValue();
        if (damage <= 0) return false;
        stack.setDamageValue(Math.max(0, damage - amount));
        return true;
    }

    private static boolean matches(ItemAttributeModifiers modifiers, Map<ResourceLocation, Applied> target) {
        var found = 0;
        for (var entry : modifiers.modifiers()) {
            if (isPassive(entry)) continue;
            var change = target.get(entry.modifier().id());
            if (change == null || !same(entry, change)) return false;
            found++;
        }
        return found == target.size();
    }

    private static boolean isPassive(ItemAttributeModifiers.Entry entry) {
        return !entry.modifier().id().getNamespace().equals(MOD_ID)
                || (!entry.modifier().id().getPath().equals(PASSIVE_ROOT)
                && !entry.modifier().id().getPath().startsWith(PASSIVE_PREFIX));
    }

    private static boolean same(ItemAttributeModifiers.Entry entry, Applied change) {
        return entry.attribute().equals(change.attribute()) && entry.modifier().amount() == change.amount()
                && entry.modifier().operation() == change.operation() && entry.slot().equals(SLOT);
    }

    private static double levelBonus(int level, double equilibrium, double multiplier) {
        if (level > SECOND_TIER)
            return FIRST_TIER * multiplier * (FIRST_STEP + equilibrium)
                    + FIRST_TIER * multiplier * (SECOND_STEP + equilibrium * EQUILIBRIUM_SECOND)
                    + (level - SECOND_TIER) * multiplier * (THIRD_STEP + equilibrium * EQUILIBRIUM_THIRD);
        if (level >= FIRST_TIER)
            return FIRST_TIER * multiplier * (FIRST_STEP + equilibrium)
                    + (level - FIRST_TIER) * multiplier * (SECOND_STEP + equilibrium * EQUILIBRIUM_SECOND);
        return level * multiplier * (FIRST_STEP + equilibrium);
    }

    private static void write(ItemStack stack, Holder<Attribute> attribute, double amount) {
        var modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        var entry = find(modifiers, WeaponAttributes.LEVEL_ID);
        if (entry != null && entry.modifier().amount() == amount && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE
                && entry.slot().equals(SLOT) && entry.attribute().equals(attribute)) return;
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
                modifiers.withModifierAdded(attribute, new AttributeModifier(WeaponAttributes.LEVEL_ID, amount, AttributeModifier.Operation.ADD_VALUE), SLOT));
    }

    private static ItemAttributeModifiers.Entry find(ItemAttributeModifiers modifiers, ResourceLocation id) {
        for (var entry : modifiers.modifiers()) if (entry.modifier().is(id)) return entry;
        return null;
    }

    private record Applied(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
    }
}
