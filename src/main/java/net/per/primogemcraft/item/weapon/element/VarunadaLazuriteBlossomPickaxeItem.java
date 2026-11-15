package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;

import java.util.List;

public class VarunadaLazuriteBlossomPickaxeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(2561, 8.0F, 15, WeaponTier.NETHERITE_INCORRECT,
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

    public VarunadaLazuriteBlossomPickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(WeaponDescription.of(PASSIVE_ACTION, DENDRO_CORE_TEXT));
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (!(level instanceof ServerWorld server) || !(miningEntity instanceof PlayerEntity player)) return result;
        if (server.getRandom().nextDouble() >= SPAWN_CHANCE) return result;
        var spawned = PGCEntities.DENDRO_CORE.get().spawn(server, player.getBlockPos(), SpawnReason.MOB_SUMMONED);
        if (spawned != null) spawned.setYaw(server.getRandom().nextFloat() * FULL_TURN);
        level.playSound(null, player.getBlockPos(), PGCSounds.SOIL_SHAPING.get(), SoundCategory.NEUTRAL,
                SOUND_VOLUME, MathHelper.nextFloat(server.getRandom(), (float) SOUND_PITCH_MIN, (float) SOUND_PITCH_MAX));
        return result;
    }
}
