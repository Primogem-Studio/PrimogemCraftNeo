package net.per.primogemcraft.item.curio;

import net.minecraft.world.World;
import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

import java.util.List;

public class WrittenInWaterItem extends CurioItem {
    private static final int RESTORE_INTERVAL = 6000;
    private static final int TOOLTIP_LINES = 5;
    private static final int MAX_FOOD = 20;

    public WrittenInWaterItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public int presenceInterval() {
        return RESTORE_INTERVAL;
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        player.setHealth(player.getMaxHealth());
        var data = player.getHungerManager();
        data.setFoodLevel(MAX_FOOD);
        data.setSaturationLevel(MAX_FOOD);
        player.clearStatusEffects();
        StackComponents.set(context.stack(), PGCDataComponents.RECALL_POSITION, player.getBlockPos());
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) return false;
        var position = StackComponents.get(stack, PGCDataComponents.RECALL_POSITION);
        if (position == null) return false;
        player.teleport(player.getServerWorld(), position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, player.getYaw(), player.getPitch());
        player.getWorld().playSound(null, position.getX(), position.getY(), position.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8F, 0.9F);
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        appendTrigger(stack, tooltip);
        var prefix = stack.getTranslationKey() + ".tooltip.";
        for (var index = 0; index < TOOLTIP_LINES; index++) tooltip.add(Text.translatable(prefix + index));
        var position = StackComponents.get(stack, PGCDataComponents.RECALL_POSITION);
        tooltip.add(Text.translatable(prefix + TOOLTIP_LINES,
                position == null ? 0 : position.getX(),
                position == null ? 0 : position.getY(),
                position == null ? 0 : position.getZ()));
    }
}
