package net.per.primogemcraft.registry;

import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.attachment.AttachmentType;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.compat.registries.NeoForgeRegistries;
import net.per.primogemcraft.system.weapon.Equilibrium;
import net.per.primogemcraft.system.wish.WishPity;
import net.per.primogemcraft.util.PlayerFlags;
import net.per.primogemcraft.util.TemporalRecord;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WishPity>> WISH_PITY = REGISTRY.register("wish_pity", () -> AttachmentType.builder(() -> WishPity.EMPTY).serialize(WishPity.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerFlags>> PLAYER_FLAGS = REGISTRY.register("player_flags", () -> AttachmentType.builder(PlayerFlags::new).serialize(PlayerFlags.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Equilibrium>> EQUILIBRIUM = REGISTRY.register("equilibrium", () -> AttachmentType.builder(Equilibrium::new).serialize(Equilibrium.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TemporalRecord>> TEMPORAL_RECORD = REGISTRY.register("temporal_record", () -> AttachmentType.builder(() -> TemporalRecord.EMPTY).serialize(TemporalRecord.CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> POWDER_SNOW_WALK = REGISTRY.register("powder_snow_walk", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> YIJI_TIMER = REGISTRY.register("yiji_timer", () -> AttachmentType.builder(() -> 0).build());
}
