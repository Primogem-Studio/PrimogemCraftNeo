package net.per.primogemcraft.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCPotions;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 丰饶药剂酿造配方。
 *
 * <p>1.21 通过 {@code RegisterBrewingRecipesEvent} 注册，并用
 * {@code DataComponents.POTION_CONTENTS} / {@code PotionContents} 读写药水；
 * 1.20.1 没有该事件与数据组件，改为在 {@link FMLCommonSetupEvent} 里调用
 * {@link BrewingRecipeRegistry#addRecipe}，药水读写用 {@link PotionUtils}。
 */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AbundanceElixirBrewing implements IBrewingRecipe {
    private static final List<Item> CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(new AbundanceElixirBrewing()));
    }

    @Override
    public boolean isInput(ItemStack input) {
        if (!CONTAINERS.contains(input.getItem())) return false;
        return PotionUtils.getPotion(input) == Potions.THICK;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(PGCItems.UNKNOWN_LEAF.get());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
        return PotionUtils.setPotion(new ItemStack(input.getItem()), PGCPotions.ABUNDANCE_ELIXIR.get());
    }
}
