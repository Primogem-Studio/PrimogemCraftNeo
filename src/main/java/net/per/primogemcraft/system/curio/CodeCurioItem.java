package net.per.primogemcraft.system.curio;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerFlags;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class CodeCurioItem extends CurioItem {
    private static final int REFRESH_TICKS = 400;
    private static final int BASE_STACKS = 10;
    private static final int NEARBY_PLAYERS = 3;
    private static final double RANGE = 8.0D;
    private static final double RATE = 0.02D;

    private final List<EntityAttribute> attributes;
    private final boolean integrated;

    public CodeCurioItem(List<EntityAttribute> attributes, boolean integrated, Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
        this.attributes = attributes;
        this.integrated = integrated;
    }

    @Override
    public int barCapacity() {
        return BASE_STACKS * (1 + NEARBY_PLAYERS);
    }

    @Override
    public boolean barFillsUp() {
        return true;
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        if (!integrated && carriesIntegratedCode(player)) return;
        var stamps = stamps(context);
        var flags = PlayerFlags.of(player);
        if (PGCTimer.isDone(player, stamps.timer())) {
            flags.set(stamps.counter(), 0);
            context.progress(0, BASE_STACKS);
            return;
        }
        var stacks = flags.counter(stamps.counter());
        context.progress(stacks, limit(player));
        if (stacks <= 0) return;
        for (var index = 0; index < attributes.size(); index++)
            CurioAttributes.push(player, context.stack(), index, attributes.get(index), RATE * stacks, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.KILL) return;
        var player = context.player();
        if (!integrated && carriesIntegratedCode(player)) return;
        var stamps = stamps(context);
        var flags = PlayerFlags.of(player);
        var limit = limit(player);
        if (flags.counter(stamps.counter()) < limit) flags.advance(stamps.counter());
        PGCTimer.set(player, stamps.timer(), REFRESH_TICKS);
        context.progress(flags.counter(stamps.counter()), limit);
    }

    private static int limit(ServerPlayerEntity player) {
        return BASE_STACKS + BASE_STACKS * Math.min(NEARBY_PLAYERS, nearbyPlayers(player));
    }

    private Stamps stamps(CurioContext context) {
        var id = Curios.idOf(context.stack().getItem()).getPath();
        return new Stamps(Identifier.of(MOD_ID, "code_stacks/" + id), "curio/code/" + id);
    }

    private static int nearbyPlayers(ServerPlayerEntity player) {
        return player.getServerWorld().getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(RANGE), e -> true).size();
    }

    private static boolean carriesIntegratedCode(ServerPlayerEntity player) {
        return net.per.primogemcraft.compat.item.Inventories120.any(player.getInventory(), stack -> !stack.isEmpty() && stack.isOf(PGCItems.INTEGRATED_CODE.get()));
    }

    private record Stamps(Identifier counter, String timer) {
    }
}
