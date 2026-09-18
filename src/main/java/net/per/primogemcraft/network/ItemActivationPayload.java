package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ItemActivationPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<ItemActivationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "item_activation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemActivationPayload> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, ItemActivationPayload::stack, ItemActivationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
