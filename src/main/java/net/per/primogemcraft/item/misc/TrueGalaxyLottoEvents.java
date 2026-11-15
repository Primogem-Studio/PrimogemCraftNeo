package net.per.primogemcraft.item.misc;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.GameMode;
import net.minecraft.block.BlockState;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
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

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, playerEntity, pos, state, blockEntity) -> {
            if (!(playerEntity instanceof ServerPlayerEntity player)) return;
            if (!isJar(state)) return;
            var offhand = player.getOffHandStack();
            if (!offhand.isOf(PGCItems.TRUE_GALAXY_LOTTO.get())) return;
            if (player.getRandom().nextDouble() >= TRIGGER_CHANCE) return;
            if (player.interactionManager.getGameMode() == GameMode.CREATIVE || !player.hasStatusEffect(PGCEffects.LOTTO_PUNISHMENT.get())) {
                if (player.getRandom().nextDouble() < CURIO_CHANCE) {
                    CurioReward.open(player, ChoiceSupport.CURIO_CARDS, List.of(Curios.randomCurio(player.getRandom())));
                    player.sendMessage(Text.translatable(GAINED_KEY), false);
                    return;
                }
                LottoPunishmentEffect.apply(player);
                player.sendMessage(Text.translatable(BROKEN_KEY), false);
                player.getWorld().playSound(null, player.getBlockPos(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
                return;
            }
            player.damage(player.getDamageSources().generic(), RUIN_DAMAGE);
            player.server.getPlayerManager().broadcast(Text.translatable(RUINED_KEY, player.getName()), false);
        });
    }

    private static boolean isJar(BlockState state) {
        return state.isOf(PGCBlocks.SMALL_JAR.get()) || state.isOf(PGCBlocks.BIG_JAR.get());
    }
}
