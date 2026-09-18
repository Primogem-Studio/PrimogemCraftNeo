package net.per.primogemcraft.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.component.PastDelay;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.component.WeaponRecovery;
import net.per.primogemcraft.item.curio.FootprintsOfFateItem;
import net.per.primogemcraft.system.weapon.WeaponState;

import java.util.List;
import java.util.UUID;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCDataComponents {
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WISH_VALUE = REGISTRY.registerComponentType("wish_value", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ELEMENT_TYPE = REGISTRY.registerComponentType("element_type", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomBar>> CUSTOM_BAR = REGISTRY.registerComponentType("custom_bar", builder -> builder.persistent(CustomBar.CODEC).networkSynchronized(CustomBar.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ResourceLocation>>> CURIO_MARKS = REGISTRY.registerComponentType("curio_marks", builder -> builder.persistent(ResourceLocation.CODEC.listOf()).networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<UUID>>> REWARD_BOOK_LEARNERS = REGISTRY.registerComponentType("reward_book_learners", builder -> builder.persistent(UUIDUtil.CODEC.listOf()).networkSynchronized(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CURIO_COUNTER = REGISTRY.registerComponentType("curio_counter", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LOTTO_COUNT = REGISTRY.registerComponentType("lotto_count", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TICKET_PROGRESS = REGISTRY.registerComponentType("ticket_progress", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> RECALL_POSITION = REGISTRY.registerComponentType("recall_position", builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> ENCOUNTER_POSITION = REGISTRY.registerComponentType("encounter_position", builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> COMPASS_TARGET = REGISTRY.registerComponentType("compass_target", builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BANKBOOK_FRAGMENTS = REGISTRY.registerComponentType("bankbook_fragments", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BANKBOOK_MODE = REGISTRY.registerComponentType("bankbook_mode", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponState>> WEAPON_STATE = REGISTRY.registerComponentType("weapon_state", builder -> builder.persistent(WeaponState.CODEC).networkSynchronized(WeaponState.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponRecovery>> WEAPON_RECOVERY = REGISTRY.registerComponentType("weapon_recovery", builder -> builder.persistent(WeaponRecovery.CODEC).networkSynchronized(WeaponRecovery.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponCharge>> WEAPON_CHARGE = REGISTRY.registerComponentType("weapon_charge", builder -> builder.persistent(WeaponCharge.CODEC).networkSynchronized(WeaponCharge.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PastDelay>> PAST_DELAY = REGISTRY.registerComponentType("past_delay", builder -> builder.persistent(PastDelay.CODEC).networkSynchronized(PastDelay.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FootprintsOfFateItem.Roll>> FOOTPRINTS_ROLL = REGISTRY.registerComponentType("footprints_roll", builder -> builder.persistent(FootprintsOfFateItem.Roll.CODEC).networkSynchronized(FootprintsOfFateItem.Roll.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> POUCH_CONTENTS = REGISTRY.registerComponentType("pouch_contents", builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
}
