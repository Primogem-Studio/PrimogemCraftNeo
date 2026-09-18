package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.Curios;

import java.util.List;

public class OtherworldBankbookItem extends Item {
    private static final String MODE_KEY = "message.primogemcraft.otherworld_bankbook.mode";
    private static final String DEPOSITED_KEY = "message.primogemcraft.otherworld_bankbook.deposited";
    private static final String WITHDRAWN_KEY = "message.primogemcraft.otherworld_bankbook.withdrawn";
    private static final String EMPTY_KEY = "message.primogemcraft.otherworld_bankbook.empty";
    private static final String MODE_PREFIX = ".mode.";
    private static final String TOOLTIP_SUFFIX = ".tooltip.";
    private static final int INSTRUCTION_LINES = 5;
    private static final float VOLUME = 0.8F;

    public OtherworldBankbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) interact(serverPlayer, stack);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer player) interact(player, context.getItemInHand());
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && entity instanceof ServerPlayer player && player.isShiftKeyDown()) deposit(player, stack);
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var prefix = stack.getDescriptionId() + TOOLTIP_SUFFIX;
        for (var index = 0; index < INSTRUCTION_LINES; index++) tooltip.add(Component.translatable(prefix + index));
        tooltip.add(Component.translatable(prefix + INSTRUCTION_LINES, Component.translatable(modeKey(stack))));
        tooltip.add(Component.translatable(prefix + (INSTRUCTION_LINES + 1), OtherworldBankbook.stored(stack), OtherworldBankbook.CAPACITY));
    }

    private void interact(ServerPlayer player, ItemStack stack) {
        if (player.isShiftKeyDown()) withdraw(player, stack);
        else switchMode(player, stack);
    }

    private void switchMode(ServerPlayer player, ItemStack stack) {
        var mode = OtherworldBankbook.cycle(stack);
        player.displayClientMessage(Component.translatable(MODE_KEY, Component.translatable(modeKey(stack))), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, VOLUME, 1.0F + mode * 0.1F);
    }

    private void deposit(ServerPlayer player, ItemStack stack) {
        var before = OtherworldBankbook.stored(stack);
        OtherworldBankbook.deposit(player, stack);
        var stored = OtherworldBankbook.stored(stack) - before;
        if (stored <= 0) return;
        player.displayClientMessage(Component.translatable(DEPOSITED_KEY, stored, OtherworldBankbook.totalStored(player)), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, VOLUME, 1.0F);
    }

    private void withdraw(ServerPlayer player, ItemStack stack) {
        var taken = OtherworldBankbook.withdraw(stack, OtherworldBankbook.WITHDRAWAL);
        if (taken <= 0) {
            player.displayClientMessage(Component.translatable(EMPTY_KEY), true);
            return;
        }
        Curios.give(player, new ItemStack(PGCItems.COSMIC_FRAGMENT.get(), taken));
        player.displayClientMessage(Component.translatable(WITHDRAWN_KEY, taken), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, VOLUME, 1.0F);
    }

    private String modeKey(ItemStack stack) {
        return stack.getDescriptionId() + MODE_PREFIX + OtherworldBankbook.mode(stack);
    }
}
