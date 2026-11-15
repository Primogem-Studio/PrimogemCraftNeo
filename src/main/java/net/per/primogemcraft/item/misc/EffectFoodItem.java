package net.per.primogemcraft.item.misc;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;

import java.util.List;
import java.util.function.Consumer;

public class EffectFoodItem extends DescribedItem {
    private final List<StatusEffectInstance> effects;
    private final Consumer<ServerPlayerEntity> afterEaten;

    public EffectFoodItem(Settings properties, Consumer<ServerPlayerEntity> afterEaten, StatusEffectInstance... effects) {
        super(properties);
        this.effects = List.of(effects);
        this.afterEaten = afterEaten;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (!(entity instanceof ServerPlayerEntity player)) return result;
        for (var effect : effects) player.addStatusEffect(new StatusEffectInstance(effect));
        if (afterEaten != null) afterEaten.accept(player);
        return result;
    }
}
