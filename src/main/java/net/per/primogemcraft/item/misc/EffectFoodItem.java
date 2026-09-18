package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

import java.util.List;
import java.util.function.Consumer;

public class EffectFoodItem extends DescribedItem {
    private final List<MobEffectInstance> effects;
    private final Consumer<ServerPlayer> afterEaten;

    public EffectFoodItem(Properties properties, Consumer<ServerPlayer> afterEaten, MobEffectInstance... effects) {
        super(properties);
        this.effects = List.of(effects);
        this.afterEaten = afterEaten;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) return result;
        for (var effect : effects) player.addEffect(new MobEffectInstance(effect));
        if (afterEaten != null) afterEaten.accept(player);
        return result;
    }
}
