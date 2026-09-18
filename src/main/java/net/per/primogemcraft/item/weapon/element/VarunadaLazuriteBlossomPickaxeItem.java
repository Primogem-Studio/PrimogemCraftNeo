package net.per.primogemcraft.item.weapon.element;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;

import java.util.List;

public class VarunadaLazuriteBlossomPickaxeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(2561, 8.0F, 15, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.NAGADUS_EMERALD_SLIVER, PGCItems.VARUNADA_LAZURITE_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 6.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double SPAWN_CHANCE = 0.1D;
    private static final float SOUND_VOLUME = 0.5F;
    private static final double SOUND_PITCH_MIN = 0.8D;
    private static final double SOUND_PITCH_MAX = 1.0D;
    private static final float FULL_TURN = 360.0F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String DENDRO_CORE_TEXT = "dendro_core";

    public VarunadaLazuriteBlossomPickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(WeaponDescription.of(PASSIVE_ACTION, DENDRO_CORE_TEXT));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.mineBlock(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerLevel server) || !(miningEntity instanceof Player player)) return result;
        if (server.getRandom().nextDouble() >= SPAWN_CHANCE) return result;
        var spawned = PGCEntities.DENDRO_CORE.get().spawn(server, player.blockPosition(), MobSpawnType.MOB_SUMMONED);
        if (spawned != null) spawned.setYRot(server.getRandom().nextFloat() * FULL_TURN);
        level.playSound(null, player.blockPosition(), PGCSounds.SOIL_SHAPING.get(), SoundSource.NEUTRAL,
                SOUND_VOLUME, Mth.nextFloat(server.getRandom(), (float) SOUND_PITCH_MIN, (float) SOUND_PITCH_MAX));
        return result;
    }
}
