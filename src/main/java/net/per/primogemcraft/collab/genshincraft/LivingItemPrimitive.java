package net.per.primogemcraft.collab.genshincraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hackermdch.genshincraft.entity.misc.SpellStub;
import net.hackermdch.genshincraft.spell.EffectSpec;
import net.hackermdch.genshincraft.spell.PrimitiveInstance;
import net.hackermdch.genshincraft.spell.PrimitiveType;
import net.hackermdch.genshincraft.spell.SpellBehavior;
import net.hackermdch.genshincraft.spell.SpellRuntimeContext;
import net.hackermdch.genshincraft.spell.Stats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.registry.PGCSpellPrimitiveTypes;
import net.per.primogemcraft.system.living.LivingItemAPI;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class LivingItemPrimitive extends PrimitiveInstance<LivingItemPrimitive> {
    public static final MapCodec<LivingItemPrimitive> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(Action::values).fieldOf("action").forGetter(primitive -> primitive.action),
            Codec.intRange(20, 12000).optionalFieldOf("duration", 1200).forGetter(primitive -> primitive.duration)
    ).apply(instance, LivingItemPrimitive::new));

    private final Action action;
    private final int duration;

    private LivingItemPrimitive(Action action, int duration) {
        this.action = action;
        this.duration = duration;
    }

    @Override
    public void init(SpellRuntimeContext context, SpellStub stub, EffectSpec spec) {
        if (!(stub.level() instanceof ServerLevel level)) return;
        if (!(context.owner instanceof ServerPlayer owner) || owner instanceof FakePlayer || !owner.isAlive()
                || owner.isSpectator() || owner.level() != level || owner.distanceToSqr(stub) > 64 * 64) {
            stub.discard();
            return;
        }
        var items = LivingItemAPI.collectAll(owner);
        if (action == Action.CREATE) {
            var item = items.size() < 64 ? LivingItemAPI.summonOneAt(owner, duration, stub.position()) : null;
            spec.addBehavior(new Targets(item == null ? List.of() : List.of(item), true));
        } else {
            var size = spec.getStat(Stats.SIZE);
            if (!Double.isFinite(size) || size <= 0) {
                stub.discard();
                return;
            }
            var radius = Math.min(16 * size, 64);
            items.removeIf(item -> item.distanceToSqr(stub) > radius * radius);
            items.sort(Comparator.comparing(LivingItemEntity::getUUID));
            spec.addBehavior(new Targets(List.copyOf(items), false));
        }
    }

    @Override
    public PrimitiveType<LivingItemPrimitive> type() {
        return PGCSpellPrimitiveTypes.LIVING_ITEM.get();
    }

    record Targets(List<LivingItemEntity> items, boolean created) implements SpellBehavior {
        @Override
        public void onAttach(SpellRuntimeContext context, SpellStub stub) {
            stub.discard();
        }
    }

    private enum Action implements StringRepresentable {
        CREATE, SELECT;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
