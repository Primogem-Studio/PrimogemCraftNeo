package net.per.primogemcraft.item.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ChasingStarlightWithYouItem extends DescribedItem {
    public ChasingStarlightWithYouItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE)
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MOD_ID, "chasing_starlight_with_you"))));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || !player.isShiftKeyDown()) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        var flame = level.registryAccess().holderOrThrow(Enchantments.FLAME);
        if (EnchantmentHelper.getItemEnchantmentLevel(flame, stack) > 0) EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(flame, 0));
        else stack.enchant(flame, 1);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
