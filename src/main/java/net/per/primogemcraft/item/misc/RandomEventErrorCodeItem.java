package net.per.primogemcraft.item.misc;

import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.event.EventLoot;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class RandomEventErrorCodeItem extends DescribedItem {
    private static final Identifier ERROR_CODE_LOOT = Identifier.of(MOD_ID, "gameplay/error_code");
    private static final float VOLUME = 2.0F;
    private static final float PITCH = 0.5F;

    public RandomEventErrorCodeItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld serverLevel) || !(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.success(stack);
        var rolled = EventLoot.roll(serverLevel, ERROR_CODE_LOOT);
        if (rolled.isEmpty()) return TypedActionResult.fail(stack);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, VOLUME, PITCH);
        OtherworldBankbook.give(serverPlayer, rolled.get(0));
        stack.decrement(1);
        return TypedActionResult.success(stack);
    }
}
