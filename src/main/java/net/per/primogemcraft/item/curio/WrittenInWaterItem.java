package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

    public WrittenInWaterItem(Properties properties) {
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
        var data = player.getFoodData();
        data.setFoodLevel(MAX_FOOD);
        data.setSaturation(MAX_FOOD);
        player.removeAllEffects();
        context.stack().set(PGCDataComponents.RECALL_POSITION.get(), player.blockPosition());
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof ServerPlayer player)) return false;
        var position = stack.get(PGCDataComponents.RECALL_POSITION.get());
        if (position == null) return false;
        player.teleportTo(player.serverLevel(), position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.level().playSound(null, position.getX(), position.getY(), position.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.9F);
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendTrigger(stack, tooltip);
        var prefix = stack.getDescriptionId() + ".tooltip.";
        for (var index = 0; index < TOOLTIP_LINES; index++) tooltip.add(Component.translatable(prefix + index));
        var position = stack.get(PGCDataComponents.RECALL_POSITION.get());
        tooltip.add(Component.translatable(prefix + TOOLTIP_LINES,
                position == null ? 0 : position.getX(),
                position == null ? 0 : position.getY(),
                position == null ? 0 : position.getZ()));
    }
}
