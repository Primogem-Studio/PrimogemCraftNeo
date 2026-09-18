package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.choice.*;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ChoiceOpenPayload(ChoiceRequest request) implements CustomPacketPayload {
    private static final int NO_CARD_SPIN = 0;

    public static final Type<ChoiceOpenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "choice_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceOpenPayload> STREAM_CODEC = StreamCodec.of(ChoiceOpenPayload::encode, ChoiceOpenPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, ChoiceOpenPayload payload) {
        var request = payload.request();
        ByteBufCodecs.VAR_INT.encode(buffer, request.id());
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, request.title());
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, request.subtitle());
        ByteBufCodecs.VAR_INT.encode(buffer, request.visual().ordinal());
        ResourceLocation.STREAM_CODEC.encode(buffer, request.texture());
        ResourceLocation.STREAM_CODEC.encode(buffer, request.textures().sheet());
        ByteBufCodecs.VAR_INT.encode(buffer, request.textures().columns());
        ByteBufCodecs.VAR_INT.encode(buffer, request.textures().tileWidth());
        ByteBufCodecs.VAR_INT.encode(buffer, request.textures().tileHeight());
        ByteBufCodecs.VAR_INT.encode(buffer, request.textures().idle());
        ByteBufCodecs.VAR_INT.encode(buffer, request.textures().hovered());
        ByteBufCodecs.VAR_INT.encode(buffer, request.textures().chosen());
        ByteBufCodecs.VAR_INT.encode(buffer, request.textures().dimmed());
        ResourceLocation.STREAM_CODEC.encode(buffer, request.textures().backSheet());
        ByteBufCodecs.VAR_INT.encode(buffer, request.spin().turns());
        ByteBufCodecs.VAR_INT.encode(buffer, request.spin().speed().ordinal());
        buffer.writeFloat(request.spin().tilt());
        ByteBufCodecs.VAR_INT.encode(buffer, request.settleTicks());
        ByteBufCodecs.VAR_INT.encode(buffer, request.options().size());
        for (var card : request.options()) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, card.title());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, card.item());
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, card.description());
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, card.footnote());
            var spin = card.spin();
            ByteBufCodecs.VAR_INT.encode(buffer, spin == null ? NO_CARD_SPIN : spin.turns());
            ByteBufCodecs.VAR_INT.encode(buffer, spin == null ? 0 : spin.speed().ordinal());
            var icons = card.icons();
            buffer.writeBoolean(icons != null);
            if (icons != null) {
                ResourceLocation.STREAM_CODEC.encode(buffer, icons.texture());
                ByteBufCodecs.VAR_INT.encode(buffer, icons.count());
            }
            var overlay = card.overlay();
            buffer.writeBoolean(overlay != null);
            if (overlay != null) {
                ResourceLocation.STREAM_CODEC.encode(buffer, overlay.sheet());
                ByteBufCodecs.VAR_INT.encode(buffer, overlay.columns());
                ByteBufCodecs.VAR_INT.encode(buffer, overlay.column());
            }
            buffer.writeBoolean(card.textTooltip());
            buffer.writeBoolean(card.itemTooltip());
            buffer.writeBoolean(card.enabled());
            ByteBufCodecs.VAR_INT.encode(buffer, card.quality());
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, card.badge());
            ByteBufCodecs.VAR_INT.encode(buffer, card.unmet().size());
            for (var line : card.unmet()) ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, line);
        }
        ByteBufCodecs.VAR_LONG.encode(buffer, request.expireTime());
        ByteBufCodecs.VAR_INT.encode(buffer, request.mode().ordinal());
        ByteBufCodecs.VAR_INT.encode(buffer, request.leaveIndex());
    }

    private static ChoiceOpenPayload decode(RegistryFriendlyByteBuf buffer) {
        var id = ByteBufCodecs.VAR_INT.decode(buffer);
        var title = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
        var subtitle = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
        var visual = ChoiceVisual.values()[ByteBufCodecs.VAR_INT.decode(buffer)];
        var texture = ResourceLocation.STREAM_CODEC.decode(buffer);
        var textures = new ChoiceCardTextures(ResourceLocation.STREAM_CODEC.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ResourceLocation.STREAM_CODEC.decode(buffer));
        var spin = ChoiceSpin.of(ByteBufCodecs.VAR_INT.decode(buffer), ChoiceSpinSpeed.values()[ByteBufCodecs.VAR_INT.decode(buffer)], buffer.readFloat());
        var settleTicks = ByteBufCodecs.VAR_INT.decode(buffer);
        int size = ByteBufCodecs.VAR_INT.decode(buffer);
        var options = new ArrayList<ChoiceCard>(size);
        for (var index = 0; index < size; index++) {
            var cardTitle = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            var cardItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            var cardDescription = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            var cardFootnote = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            int cardTurns = ByteBufCodecs.VAR_INT.decode(buffer);
            int cardSpeed = ByteBufCodecs.VAR_INT.decode(buffer);
            var cardSpin = cardTurns <= NO_CARD_SPIN ? null : ChoiceCardSpin.of(cardTurns, ChoiceSpinSpeed.values()[cardSpeed]);
            var icons = buffer.readBoolean() ? ChoiceCardIcons.of(ResourceLocation.STREAM_CODEC.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer)) : null;
            var overlay = buffer.readBoolean() ? ChoiceCardOverlay.of(ResourceLocation.STREAM_CODEC.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer)) : null;
            var textTooltip = buffer.readBoolean();
            var itemTooltip = buffer.readBoolean();
            var enabled = buffer.readBoolean();
            var quality = ByteBufCodecs.VAR_INT.decode(buffer);
            var badge = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            int unmetSize = ByteBufCodecs.VAR_INT.decode(buffer);
            var unmet = new ArrayList<Component>(unmetSize);
            for (var line = 0; line < unmetSize; line++) unmet.add(ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer));
            options.add(new ChoiceCard(cardTitle, cardItem, cardDescription, cardFootnote, cardSpin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, List.copyOf(unmet)));
        }
        var expireTime = ByteBufCodecs.VAR_LONG.decode(buffer);
        var mode = ChoiceMode.values()[ByteBufCodecs.VAR_INT.decode(buffer)];
        var leaveIndex = ByteBufCodecs.VAR_INT.decode(buffer);
        return new ChoiceOpenPayload(new ChoiceRequest(id, title, subtitle, visual, texture, textures, spin, settleTicks, List.copyOf(options), expireTime, mode, leaveIndex));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
