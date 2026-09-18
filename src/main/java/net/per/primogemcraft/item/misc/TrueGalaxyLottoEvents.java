package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.CurioReward;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.curio.effect.LottoPunishmentEffect;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class TrueGalaxyLottoEvents {
    private static final double TRIGGER_CHANCE = 0.3D;
    private static final double CURIO_CHANCE = 0.4D;
    private static final float RUIN_DAMAGE = 1000.0F;
    private static final float SOUND_VOLUME = 5.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String GAINED_KEY = "message.primogemcraft.true_galaxy_lotto.curio_gained";
    private static final String BROKEN_KEY = "message.primogemcraft.true_galaxy_lotto.broken";
    private static final String RUINED_KEY = "message.primogemcraft.true_galaxy_lotto.ruined";

    private TrueGalaxyLottoEvents() {
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!isJar(event.getState())) return;
        var offhand = player.getOffhandItem();
        if (!offhand.is(PGCItems.TRUE_GALAXY_LOTTO.get())) return;
        if (player.getRandom().nextDouble() >= TRIGGER_CHANCE) return;
        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE || !player.hasEffect(PGCEffects.LOTTO_PUNISHMENT)) {
            if (player.getRandom().nextDouble() < CURIO_CHANCE) {
                CurioReward.open(player, ChoiceSupport.CURIO_CARDS, List.of(Curios.randomCurio(player.getRandom())));
                player.displayClientMessage(Component.translatable(GAINED_KEY), false);
                return;
            }
            LottoPunishmentEffect.apply(player);
            player.displayClientMessage(Component.translatable(BROKEN_KEY), false);
            player.level().playSound(null, player.blockPosition(), PGCSounds.CURIO_BROKEN.get(), SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
            return;
        }
        player.hurt(player.damageSources().generic(), RUIN_DAMAGE);
        player.server.getPlayerList().broadcastSystemMessage(Component.translatable(RUINED_KEY, player.getDisplayName()), false);
    }

    private static boolean isJar(BlockState state) {
        return state.is(PGCBlocks.SMALL_JAR.get()) || state.is(PGCBlocks.BIG_JAR.get());
    }
}
