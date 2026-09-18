package net.per.primogemcraft.item.weapon.element;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

public class VayudaTurquoiseHoeItem extends WishWeaponHoeItem {
    private static final Tier TIER = new WeaponTier(1561, 8.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.VAYUDA_TURQUOISE_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 10.5F;
    private static final float ATTACK_SPEED = -3.6F;
    private static final int KNOCKBACK_MIN = 8;
    private static final int KNOCKBACK_MAX = 10;
    private static final int CREATIVE_KNOCKBACK = 10;
    private static final String PLAIN_TOOL = "plain_tool";
    private static final String KNOCKBACK_TEXT = "knockback";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public VayudaTurquoiseHoeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(PLAIN_TOOL, KNOCKBACK_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(level instanceof ServerLevel server) || !(entity instanceof ServerPlayer player)) return;
        var knockback = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK);
        if (EnchantmentHelper.getItemEnchantmentLevel(knockback, stack) != 0) return;
        stack.enchant(knockback, player.isCreative() ? CREATIVE_KNOCKBACK : Mth.nextInt(server.getRandom(), KNOCKBACK_MIN, KNOCKBACK_MAX));
    }
}
