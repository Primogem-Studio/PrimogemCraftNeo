package net.per.primogemcraft.system.curio.effect;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Prescription {
    private final String id;
    private final String label;
    private final List<Modifier> modifiers = new ArrayList<>();
    private Consumer<ServerPlayer> started;
    private int periodTicks;
    private Consumer<ServerPlayer> period;
    private int hurtCooldown;
    private Consumer<ServerPlayer> hurt;
    private int duration;

    public Prescription(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public Prescription modifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        modifiers.add(new Modifier(attribute, amount, operation));
        return this;
    }

    public Prescription started(Consumer<ServerPlayer> action) {
        started = action;
        return this;
    }

    public Prescription every(int ticks, Consumer<ServerPlayer> action) {
        periodTicks = ticks;
        period = action;
        return this;
    }

    public Prescription whenHurt(int cooldownTicks, Consumer<ServerPlayer> action) {
        hurtCooldown = cooldownTicks;
        hurt = action;
        return this;
    }

    public Prescription lasting(int ticks) {
        duration = ticks;
        return this;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public List<Modifier> modifiers() {
        return List.copyOf(modifiers);
    }

    public Consumer<ServerPlayer> started() {
        return started;
    }

    public int periodTicks() {
        return periodTicks;
    }

    public Consumer<ServerPlayer> period() {
        return period;
    }

    public int hurtCooldown() {
        return hurtCooldown;
    }

    public Consumer<ServerPlayer> hurt() {
        return hurt;
    }

    public int duration() {
        return duration;
    }

    public record Modifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
    }
}
