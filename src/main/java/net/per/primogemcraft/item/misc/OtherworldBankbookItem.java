package net.per.primogemcraft.item.misc;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;
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

    public OtherworldBankbookItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (player instanceof ServerPlayerEntity serverPlayer) interact(serverPlayer, stack);
        return TypedActionResult.consume(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity player) interact(player, context.getStack());
        return ActionResult.SUCCESS;
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity player && player.isSneaking()
                && player.getActiveHand() == Hand.MAIN_HAND) deposit(player, stack);
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        var prefix = stack.getTranslationKey() + TOOLTIP_SUFFIX;
        for (var index = 0; index < INSTRUCTION_LINES; index++) tooltip.add(Text.translatable(prefix + index));
        tooltip.add(Text.translatable(prefix + INSTRUCTION_LINES, Text.translatable(modeKey(stack))));
        tooltip.add(Text.translatable(prefix + (INSTRUCTION_LINES + 1), OtherworldBankbook.stored(stack), OtherworldBankbook.CAPACITY));
    }

    private void interact(ServerPlayerEntity player, ItemStack stack) {
        if (player.isSneaking()) withdraw(player, stack);
        else switchMode(player, stack);
    }

    private void switchMode(ServerPlayerEntity player, ItemStack stack) {
        var mode = OtherworldBankbook.cycle(stack);
        player.sendMessage(Text.translatable(MODE_KEY, Text.translatable(modeKey(stack))), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, VOLUME, 1.0F + mode * 0.1F);
    }

    private void deposit(ServerPlayerEntity player, ItemStack stack) {
        var before = OtherworldBankbook.stored(stack);
        OtherworldBankbook.deposit(player, stack);
        var stored = OtherworldBankbook.stored(stack) - before;
        if (stored <= 0) return;
        player.sendMessage(Text.translatable(DEPOSITED_KEY, stored, OtherworldBankbook.totalStored(player)), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, VOLUME, 1.0F);
    }

    private void withdraw(ServerPlayerEntity player, ItemStack stack) {
        var taken = OtherworldBankbook.withdraw(stack, OtherworldBankbook.WITHDRAWAL);
        if (taken <= 0) {
            player.sendMessage(Text.translatable(EMPTY_KEY), true);
            return;
        }
        Curios.give(player, new ItemStack(PGCItems.COSMIC_FRAGMENT.get(), taken));
        player.sendMessage(Text.translatable(WITHDRAWN_KEY, taken), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, VOLUME, 1.0F);
    }

    private String modeKey(ItemStack stack) {
        return stack.getTranslationKey() + MODE_PREFIX + OtherworldBankbook.mode(stack);
    }
}
