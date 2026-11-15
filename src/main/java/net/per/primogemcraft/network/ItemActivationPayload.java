package net.per.primogemcraft.network;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ItemActivationPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<ItemActivationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "item_activation"), ItemActivationPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemActivationPayload> STREAM_CODEC = StreamCodec.composite(
            net.per.primogemcraft.compat.network.StreamCodecs.ITEM_STACK, ItemActivationPayload::stack, ItemActivationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
