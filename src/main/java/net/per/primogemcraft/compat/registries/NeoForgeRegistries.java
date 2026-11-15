package net.per.primogemcraft.compat.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.compat.attachment.AttachmentType;
import net.per.primogemcraft.compat.component.DataComponentType;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 1.21 {@code NeoForgeRegistries} 中本项目用到的注册表键的兼容替代。
 *
 * <p>1.20.1 Forge 没有附加数据与数据组件注册表。这里提供仅供本项目内部使用的
 * 占位键：这些条目不会进入原版注册表，实际数据分别落在实体 NBT 与物品 NBT 上
 * （见 {@link net.per.primogemcraft.compat.attachment.Attachments} 与
 * {@link net.per.primogemcraft.compat.component.StackComponents}）。
 */
public final class NeoForgeRegistries {

    private NeoForgeRegistries() {
    }

    /** 附加数据「注册表」键：仅用于 DeferredRegister 的标识，不参与原版注册。 */
    public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES =
            ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "attachment_types"));

    /** 数据组件「注册表」键：同上，仅作标识。 */
    public static final ResourceKey<Registry<DataComponentType<?>>> DATA_COMPONENT_TYPES =
            ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "data_component_types"));
}
