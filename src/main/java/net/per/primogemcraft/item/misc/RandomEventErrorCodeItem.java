package net.per.primogemcraft.item.misc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.event.EventLoot;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class RandomEventErrorCodeItem extends DescribedItem {
    private static final ResourceLocation ERROR_CODE_LOOT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "gameplay/error_code");
    private static final float VOLUME = 2.0F;
    private static final float PITCH = 0.5F;

    public RandomEventErrorCodeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.success(stack);
        var rolled = EventLoot.roll(serverLevel, ERROR_CODE_LOOT);
        if (rolled.isEmpty()) return InteractionResultHolder.fail(stack);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, VOLUME, PITCH);
        OtherworldBankbook.give(serverPlayer, rolled.getFirst());
        stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }
}
