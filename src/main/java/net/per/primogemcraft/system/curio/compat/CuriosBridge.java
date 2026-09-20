package net.per.primogemcraft.system.curio.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class CuriosBridge {
    private static final ResourceLocation PREDICATE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio");
    private static final String TIMER_PREFIX = "curio_equipped_";

    private CuriosBridge() {
    }

    public static void register() {
        CuriosApi.registerCurioPredicate(PREDICATE, result -> result.stack().is(CurioForm.ANY) || OtherworldBankbook.isBankbook(result.stack()) || result.stack().is(PGCItems.VIOLANE.get()));
    }

    public static List<ItemStack> equipped(Player player) {
        var stacks = new ArrayList<ItemStack>();
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var equipped = handler.getEquippedCurios();
            for (var slot = 0; slot < equipped.getSlots(); slot++) {
                var stack = equipped.getStackInSlot(slot);
                if (!stack.isEmpty()) stacks.add(stack);
            }
        });
        return stacks;
    }

    public static boolean equip(ServerPlayer player, ItemStack stack) {
        var handler = CuriosApi.getCuriosInventory(player).orElse(null);
        if (handler == null) return false;
        var accepted = CuriosApi.getItemStackSlots(stack, player.level()).keySet();
        for (var entry : handler.getCurios().entrySet()) {
            if (!accepted.contains(entry.getKey())) continue;
            var slots = entry.getValue().getStacks();
            for (var index = 0; index < slots.getSlots(); index++) {
                if (!slots.getStackInSlot(index).isEmpty()) continue;
                handler.setEquippedCurio(entry.getKey(), index, stack.copy());
                return true;
            }
        }
        return false;
    }

    public static int consume(ServerPlayer player, Set<Item> pending) {
        var consumed = 0;
        var handler = CuriosApi.getCuriosInventory(player).orElse(null);
        if (handler == null) return 0;
        var equipped = handler.getEquippedCurios();
        for (var slot = 0; slot < equipped.getSlots() && !pending.isEmpty(); slot++) {
            var stack = equipped.getStackInSlot(slot);
            if (stack.isEmpty() || !pending.contains(stack.getItem())) continue;
            if (equipped.extractItem(slot, 1, false).isEmpty()) continue;
            pending.remove(stack.getItem());
            consumed++;
        }
        return consumed;
    }

    public static void tick(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var equipped = handler.getEquippedCurios();
            for (var slot = 0; slot < equipped.getSlots(); slot++) {
                if (!(equipped.getStackInSlot(slot).getItem() instanceof CurioItem curio)) continue;
                var stack = equipped.getStackInSlot(slot);
                var context = CurioContext.of(player, stack, curio.form());
                if (context.ready(TIMER_PREFIX + slot, curio.presenceInterval())) curio.presence(context);
            }
        });
    }
}
