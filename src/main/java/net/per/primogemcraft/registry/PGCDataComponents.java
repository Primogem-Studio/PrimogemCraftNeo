package net.per.primogemcraft.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.compat.component.DataComponentType;
import net.per.primogemcraft.compat.component.ItemContainerContents;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.component.PastDelay;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.component.WeaponRecovery;
import net.per.primogemcraft.item.curio.FootprintsOfFateItem;
import net.per.primogemcraft.system.weapon.WeaponState;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 物品数据组件注册表（1.20.1 兼容版）。
 *
 * <p>1.21 把这些组件注册进 {@code Registries.DATA_COMPONENT_TYPE}；1.20.1 没有
 * 数据组件系统，组件数据统一落在物品 NBT 的 {@code primogemcraft_components}
 * 子标签下（见 {@link net.per.primogemcraft.compat.component.StackComponents}）。
 * 因此这里不再进入真实注册表，而是用「本地持有者」承载各组件的名称与 Codec，
 * 各组件原有的 {@code Codec} 定义原样复用。
 *
 * <p>{@link #REGISTRY} 保留为一个空操作的注册入口，使模组构造器里的
 * {@code PGCDataComponents.REGISTRY.register(modBus)} 调用无需改写。
 */
public class PGCDataComponents {

    /** 空操作注册入口：1.20.1 没有数据组件注册表，组件在类加载时即构建完成。 */
    public static final Registry REGISTRY = new Registry();

    public static final class Registry {
        public void register(net.minecraftforge.eventbus.api.IEventBus bus) {
            // 组件不进入注册表，无需注册动作。
        }
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> component(
            String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        var type = builder.apply(DataComponentType.<T>builder().name(name)).build();
        return DeferredHolder.local(new ResourceLocation(MOD_ID, name), type);
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WISH_VALUE =
            component("wish_value", b -> b.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ELEMENT_TYPE =
            component("element_type", b -> b.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomBar>> CUSTOM_BAR =
            component("custom_bar", b -> b.persistent(CustomBar.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ResourceLocation>>> CURIO_MARKS =
            component("curio_marks", b -> b.persistent(ResourceLocation.CODEC.listOf()));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<UUID>>> REWARD_BOOK_LEARNERS =
            component("reward_book_learners", b -> b.persistent(UUIDUtil.CODEC.listOf()));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CURIO_COUNTER =
            component("curio_counter", b -> b.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LOTTO_COUNT =
            component("lotto_count", b -> b.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TICKET_PROGRESS =
            component("ticket_progress", b -> b.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> RECALL_POSITION =
            component("recall_position", b -> b.persistent(BlockPos.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> ENCOUNTER_POSITION =
            component("encounter_position", b -> b.persistent(BlockPos.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> COMPASS_TARGET =
            component("compass_target", b -> b.persistent(ResourceLocation.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BANKBOOK_FRAGMENTS =
            component("bankbook_fragments", b -> b.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BANKBOOK_MODE =
            component("bankbook_mode", b -> b.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponState>> WEAPON_STATE =
            component("weapon_state", b -> b.persistent(WeaponState.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponRecovery>> WEAPON_RECOVERY =
            component("weapon_recovery", b -> b.persistent(WeaponRecovery.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponCharge>> WEAPON_CHARGE =
            component("weapon_charge", b -> b.persistent(WeaponCharge.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PastDelay>> PAST_DELAY =
            component("past_delay", b -> b.persistent(PastDelay.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FootprintsOfFateItem.Roll>> FOOTPRINTS_ROLL =
            component("footprints_roll", b -> b.persistent(FootprintsOfFateItem.Roll.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> POUCH_CONTENTS =
            component("pouch_contents", b -> b.persistent(ItemContainerContents.CODEC));
}
