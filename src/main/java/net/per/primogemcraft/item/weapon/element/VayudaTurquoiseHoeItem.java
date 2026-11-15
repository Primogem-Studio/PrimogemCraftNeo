package net.per.primogemcraft.item.weapon.element;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

public class VayudaTurquoiseHoeItem extends WishWeaponHoeItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.VAYUDA_TURQUOISE_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 10.5F;
    private static final float ATTACK_SPEED = -3.6F;
    private static final int KNOCKBACK_MIN = 8;
    private static final int KNOCKBACK_MAX = 10;
    private static final int CREATIVE_KNOCKBACK = 10;
    private static final String PLAIN_TOOL = "plain_tool";
    private static final String KNOCKBACK_TEXT = "knockback";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public VayudaTurquoiseHoeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(PLAIN_TOOL, KNOCKBACK_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(level instanceof ServerWorld server) || !(entity instanceof ServerPlayerEntity player)) return;
        var knockback = Enchantments.KNOCKBACK;
        if (EnchantmentHelper.getLevel(knockback, stack) != 0) return;
        stack.addEnchantment(knockback, player.isCreative() ? CREATIVE_KNOCKBACK : MathHelper.nextInt(server.getRandom(), KNOCKBACK_MIN, KNOCKBACK_MAX));
    }
}
