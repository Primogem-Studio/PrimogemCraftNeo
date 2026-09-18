package net.per.primogemcraft.recipe;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCPotions;

import java.util.List;
import java.util.Optional;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class AbundanceElixirBrewing implements IBrewingRecipe {
    private static final List<Item> CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

    @SubscribeEvent
    private static void register(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(new AbundanceElixirBrewing());
    }

    @Override
    public boolean isInput(ItemStack input) {
        if (!CONTAINERS.contains(input.getItem())) return false;
        Optional<Holder<Potion>> potion = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        return potion.isPresent() && potion.get().is(Potions.THICK);
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(PGCItems.UNKNOWN_LEAF.get());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
        return PotionContents.createItemStack(input.getItem(), PGCPotions.ABUNDANCE_ELIXIR);
    }
}
