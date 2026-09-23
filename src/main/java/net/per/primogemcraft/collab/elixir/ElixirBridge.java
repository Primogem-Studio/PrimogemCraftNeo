package net.per.primogemcraft.collab.elixir;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.elixir.registry.ElixirRegistries;
import net.per.elixir.util.IElixirAction;
import net.per.elixir.util.IElixirCalc;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.item.misc.ViolaneItem;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.living.LivingItemAPI;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.system.wish.WishRarity;
import net.per.primogemcraft.system.wish.WishResult;
import net.per.primogemcraft.system.wish.WishRoller;

import java.util.ArrayList;
import java.util.Set;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * The Elixir Dan linkage. Registered on the mod event bus only while Elixir Dan is installed, so nothing in
 * here is ever loaded — and no Elixir Dan type is ever resolved — without it.
 */
public final class ElixirBridge {
    private static final int PRIMOGEM_WISH_THRESHOLD = 1;
    private static final int DURATION_DIVISOR = 8;
    private static final int LAG_MINIMUM_DURATION = 60;
    private static final int LAG_MAXIMUM_DURATION = 160;
    private static final int DURATION_PER_PHARM = 4;
    private static final int MAXIMUM_CRYSTALLIZATION_DURATION = 400;

    private static final DeferredRegister<IElixirAction> ACTIONS = DeferredRegister.create(ElixirRegistries.ACTION, MOD_ID);
    private static final DeferredRegister<IElixirCalc> CALCULATORS = DeferredRegister.create(ElixirRegistries.CALCULATOR, MOD_ID);

    static {
        ACTIONS.register("primogem", () -> (pharm, time, stack, level, entity) -> {
            if (pharm <= PRIMOGEM_WISH_THRESHOLD) return;
            if (entity instanceof ServerPlayer player && level instanceof ServerLevel serverLevel) {
                var results = new ArrayList<WishResult>();
                var random = player.getRandom();
                var sunglasses = WishRoller.isWearingColorfulSunglasses(player);
                var violane = ViolaneItem.isActive(player);
                var pity = player.getData(PGCAttachments.WISH_PITY.get());
                for (var remaining = Math.min(pharm, 100); remaining > 0; remaining -= 25) {
                    var goldChance = Math.min(1.0D, remaining / 25.0D);
                    var rarity = random.nextDouble() < goldChance ? WishRarity.GOLD
                            : random.nextDouble() < WishRoller.purpleChance(WishBanner.ACQUAINT, 0) ? WishRarity.PURPLE : WishRarity.BLUE;
                    pity = pity.count(rarity, false);
                    results.add(new WishResult(WishBanner.ACQUAINT, rarity, false,
                            violane && rarity == WishRarity.BLUE || sunglasses && rarity == WishRarity.GOLD, sunglasses));
                }
                player.setData(PGCAttachments.WISH_PITY.get(), pity);
                WishEntity.spawnRing(serverLevel, player, results);
                return;
            }
            if (level.isClientSide) return;
            level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(PGCItems.PRIMOGEM.get(), level.random.nextInt(PRIMOGEM_WISH_THRESHOLD, pharm))));
        });
        ACTIONS.register("time_lag", () -> (pharm, time, stack, level, entity) -> {
            if (level.isClientSide) return;
            var duration = Mth.clamp(time / DURATION_DIVISOR, LAG_MINIMUM_DURATION, LAG_MAXIMUM_DURATION);
            if (entity instanceof LivingItemEntity living) {
                applyTimeLag(living, duration);
                return;
            }
            if (!(entity instanceof Player player)) return;
            for (var living : LivingItemAPI.collectAll(player)) {
                applyTimeLag(living, duration);
            }
        });
        ACTIONS.register("transmutation", () -> (pharm, time, stack, level, entity) -> {
            if (level.isClientSide) return;
            if (entity instanceof LivingItemEntity living) {
                if (!living.isInfinite()) living.setRemainingTicks(pharm > 0 ? time / DURATION_DIVISOR : 0);
                return;
            }
            if (!(entity instanceof Player player)) return;
            if (pharm > 0) {
                LivingItemAPI.summon(player, time / DURATION_DIVISOR);
                return;
            }
            for (var living : LivingItemAPI.collectAll(player)) {
                if (!living.isInfinite()) living.setRemainingTicks(0);
            }
        });
        ACTIONS.register("crystallization", () -> (pharm, time, stack, level, entity) -> {
            if (level.isClientSide) return;
            var duration = Math.min(Math.abs(pharm) * DURATION_PER_PHARM, MAXIMUM_CRYSTALLIZATION_DURATION);
            if (entity instanceof LivingItemEntity living) {
                if (!living.isInfinite()) living.setRemainingTicks(duration);
                if (pharm < 0) stack.shrink(1);
                return;
            }
            if (!(entity instanceof Player player)) return;
            if (pharm >= 0) {
                LivingItemAPI.summonMainSlots(player, duration);
                return;
            }
            stack.shrink(1);
            LivingItemAPI.summonAllItems(player, duration);
        });
        ACTIONS.register("permanence", () -> (pharm, time, stack, level, entity) -> {
            if (level.isClientSide) return;
            if (entity instanceof LivingItemEntity living) {
                living.setInfinite();
                return;
            }
            if (!(entity instanceof Player player)) return;
            LivingItemAPI.summonInfinite(player);
        });
        ACTIONS.register("recall", () -> (pharm, time, stack, level, entity) -> {
            if (level.isClientSide) return;
            if (entity instanceof LivingItemEntity living) {
                var owner = living.getOwner();
                if (owner != null && owner.level() instanceof ServerLevel ownerLevel)
                    living.teleportTo(ownerLevel, owner.getX(), owner.getY() + 1.5, owner.getZ(), Set.of(), owner.getYRot(), 0);
                return;
            }
            if (!(entity instanceof Player player)) return;
            LivingItemAPI.recallDrops(player);
        });
        CALCULATORS.register("diminishing", () -> (sum, base) -> Math.max(0, (int) base - Math.abs(sum)));
    }

    private ElixirBridge() {
    }

    private static void applyTimeLag(LivingItemEntity living, int duration) {
        if (living.isInfinite()) living.addInfiniteHealthFromDuration(duration);
        else living.addRemainingTicks(duration);
    }

    static void register(IEventBus modBus) {
        ACTIONS.register(modBus);
        CALCULATORS.register(modBus);
    }
}
