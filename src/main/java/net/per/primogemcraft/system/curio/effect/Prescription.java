package net.per.primogemcraft.system.curio.effect;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Prescription {
    private final String id;
    private final String label;
    private final List<Modifier> modifiers = new ArrayList<>();
    private Consumer<ServerPlayerEntity> started;
    private int periodTicks;
    private Consumer<ServerPlayerEntity> period;
    private int hurtCooldown;
    private Consumer<ServerPlayerEntity> hurt;
    private int duration;

    public Prescription(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public Prescription modifier(EntityAttribute attribute, double amount, EntityAttributeModifier.Operation operation) {
        modifiers.add(new Modifier(attribute, amount, operation));
        return this;
    }

    public Prescription started(Consumer<ServerPlayerEntity> action) {
        started = action;
        return this;
    }

    public Prescription every(int ticks, Consumer<ServerPlayerEntity> action) {
        periodTicks = ticks;
        period = action;
        return this;
    }

    public Prescription whenHurt(int cooldownTicks, Consumer<ServerPlayerEntity> action) {
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

    public Consumer<ServerPlayerEntity> started() {
        return started;
    }

    public int periodTicks() {
        return periodTicks;
    }

    public Consumer<ServerPlayerEntity> period() {
        return period;
    }

    public int hurtCooldown() {
        return hurtCooldown;
    }

    public Consumer<ServerPlayerEntity> damage() {
        return hurt;
    }

    public int duration() {
        return duration;
    }

    public record Modifier(EntityAttribute attribute, double amount, EntityAttributeModifier.Operation operation) {
    }
}
