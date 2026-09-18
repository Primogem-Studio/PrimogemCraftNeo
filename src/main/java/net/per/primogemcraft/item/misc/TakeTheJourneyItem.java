package net.per.primogemcraft.item.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCSounds;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class TakeTheJourneyItem extends DescribedItem {
    private static final int COOLDOWN_TICKS = 1280;
    private static final float VOLUME = 0.4F;
    private static final float PITCH = 1.0F;

    public TakeTheJourneyItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE)
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MOD_ID, "take_the_journey"))));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        play(level, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player != null) play(context.getLevel(), player);
        return InteractionResult.SUCCESS;
    }

    private void play(Level level, Player player) {
        if (level.isClientSide()) return;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.TAKE_THE_JOURNEY.get(), SoundSource.PLAYERS, VOLUME, PITCH);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }
}
