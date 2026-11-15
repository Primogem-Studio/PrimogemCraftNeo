package net.per.primogemcraft.registry;

import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.compat.attachment.AttachmentType;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.system.weapon.Equilibrium;
import net.per.primogemcraft.system.wish.WishPity;
import net.per.primogemcraft.util.PlayerFlags;
import net.per.primogemcraft.util.TemporalRecord;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 附加数据注册表（1.20.1 兼容版）。
 *
 * <p>1.21 把这些附件注册进 {@code NeoForgeRegistries.ATTACHMENT_TYPES}；1.20.1 没有
 * 附件系统，数据统一落在实体的 Forge 持久化 NBT（见
 * {@link net.per.primogemcraft.compat.attachment.Attachments}）。因此这里不再进入
 * 真实注册表，而是用「本地持有者」承载各附件的名称与 Codec。
 *
 * <p>{@link #REGISTRY} 保留为一个空操作的注册入口，使模组构造器里的
 * {@code PGCAttachments.REGISTRY.register(modBus)} 调用无需改写。
 */
public class PGCAttachments {

    /** 空操作注册入口：1.20.1 没有附件注册表，附件在类加载时即构建完成。 */
    public static final Registry REGISTRY = new Registry();

    public static final class Registry {
        public void register(net.minecraftforge.eventbus.api.IEventBus bus) {
            // 附件不进入注册表，无需注册动作。
        }
    }

    private static <T> DeferredHolder<AttachmentType<?>, AttachmentType<T>> attachment(
            String name, Supplier<T> defaultValue, UnaryOperator<AttachmentType.Builder<T>> builder) {
        var type = builder.apply(AttachmentType.builder(defaultValue)).build().named(name);
        return DeferredHolder.local(new ResourceLocation(MOD_ID, name), type);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WishPity>> WISH_PITY =
            attachment("wish_pity", () -> WishPity.EMPTY, b -> b.serialize(WishPity.CODEC).copyOnDeath());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerFlags>> PLAYER_FLAGS =
            attachment("player_flags", PlayerFlags::new, b -> b.serialize(PlayerFlags.CODEC).copyOnDeath());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Equilibrium>> EQUILIBRIUM =
            attachment("equilibrium", Equilibrium::new, b -> b.serialize(Equilibrium.CODEC).copyOnDeath());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TemporalRecord>> TEMPORAL_RECORD =
            attachment("temporal_record", () -> TemporalRecord.EMPTY, b -> b.serialize(TemporalRecord.CODEC));
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> POWDER_SNOW_WALK =
            attachment("powder_snow_walk", () -> false, b -> b.sync(ByteBufCodecs.BOOL));
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> YIJI_TIMER =
            attachment("yiji_timer", () -> 0, b -> b);
}