package net.per.primogemcraft.entity.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.network.ParticleBurstPayload;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.choice.*;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.system.wish.WishRarity;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishResult;
import net.per.primogemcraft.util.Advancements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WishEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_BANNER = SynchedEntityData.defineId(WishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RARITY = SynchedEntityData.defineId(WishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CAPTURING_RADIANCE = SynchedEntityData.defineId(WishEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_COLORFUL = SynchedEntityData.defineId(WishEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_CAPTURE_STATE = SynchedEntityData.defineId(WishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RADIANCE_VISIBLE = SynchedEntityData.defineId(WishEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double SINGLE_SPAWN_HEIGHT = 10.0D;
    private static final double RING_SPAWN_HEIGHT = 6.0D;
    private static final double RING_SPAWN_RADIUS = 3.0D;
    private static final int TEN_PULL = 10;
    private static final int TEN_PULL_RING = 8;
    private static final double TEN_PULL_RADIUS = 5.0D;
    private static final double DISC_RADIUS_PER_WISH = 0.1D;
    private static final double DISC_RING_CAPACITY = 4.0D;
    private static final int MIN_DISC_RINGS = 2;
    private static final double DESCENT_SPEED = 0.06D;
    private static final double CULL_MARGIN = 1.0D;
    private static final double CULL_RADIUS = 64.0D;
    private static final int CAPTURE_CONVERT_DELAY = 40;
    private static final int CAPTURE_CHOICE_COUNT = 3;
    private static final int PREVIEW_ROLL_ATTEMPTS = 16;
    private static final int CAPTURE_PARTICLE_KIND = 0;
    private static final int NO_CHOICE = -1;
    private UUID owner;
    private boolean resolved;
    private int age;
    private boolean waitingForChoice;
    private boolean choiceAnswered;
    private int selectedChoice = NO_CHOICE;
    private List<ItemStack> previews = List.of();
    private List<ItemStack> starglitter = List.of();

    public WishEntity(EntityType<? extends WishEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static void spawn(ServerLevel level, ServerPlayer player, WishResult result, double x, double y, double z) {
        var wish = new WishEntity(PGCEntities.WISH_ENTITY.get(), level);
        wish.owner = player.getUUID();
        wish.entityData.set(DATA_BANNER, result.banner().ordinal());
        wish.entityData.set(DATA_RARITY, result.rarity().ordinal());
        wish.entityData.set(DATA_CAPTURING_RADIANCE, result.capturingRadiance());
        wish.entityData.set(DATA_COLORFUL, result.colorful());
        wish.moveTo(x, y, z, 0.0F, 0.0F);
        var dir = player.position().subtract(wish.position()).normalize();
        wish.setYRot((float) (Mth.atan2(-dir.x, dir.z) * Mth.RAD_TO_DEG));
        level.addFreshEntity(wish);
    }

    public static void spawnRing(ServerLevel level, ServerPlayer player, List<WishResult> results) {
        var count = results.size();
        if (count == 1) {
            spawn(level, player, results.getFirst(), player.getX(), player.getY() + SINGLE_SPAWN_HEIGHT, player.getZ());
            return;
        }

        if (count == TEN_PULL) {
            spawnOnCircle(level, player, results, 0, TEN_PULL_RING, TEN_PULL_RADIUS, 0.0D);
            spawn(level, player, results.get(TEN_PULL_RING), player.getX(), player.getY() + RING_SPAWN_HEIGHT, player.getZ());
            spawn(level, player, results.get(TEN_PULL_RING + 1), player.getX(), player.getY() + RING_SPAWN_HEIGHT + 1.0D, player.getZ());
            return;
        }

        if (count > TEN_PULL) {
            spawnDisc(level, player, results);
            return;
        }

        spawnOnCircle(level, player, results, 0, count, RING_SPAWN_RADIUS, 0.0D);
    }

    private static void spawnDisc(ServerLevel level, ServerPlayer player, List<WishResult> results) {
        var count = results.size();
        var rings = Math.max(MIN_DISC_RINGS, (int) Math.round(Math.sqrt(count / DISC_RING_CAPACITY)));
        var radius = Math.max(TEN_PULL_RADIUS, RING_SPAWN_RADIUS + (count - TEN_PULL) * DISC_RADIUS_PER_WISH);
        var first = 0;

        for (var ring = 1; ring <= rings; ring++) {
            var remainingRings = rings - ring + 1;
            var points = (count - first + remainingRings - 1) / remainingRings;
            var ringRadius = radius * Math.sqrt((double) ring / rings);
            var stagger = ring % 2 == 0 ? 0.0D : Math.PI / points;
            spawnOnCircle(level, player, results, first, points, ringRadius, stagger);
            first += points;
        }
    }

    private static void spawnOnCircle(ServerLevel level, ServerPlayer player, List<WishResult> results, int first, int points, double radius, double stagger) {
        for (var index = 0; index < points; index++) {
            var angle = 2.0D * Math.PI * index / points + stagger;
            var x = player.getX() + radius * Math.sin(angle);
            var z = player.getZ() + radius * Math.cos(angle);
            spawn(level, player, results.get(first + index), x, player.getY() + RING_SPAWN_HEIGHT, z);
        }
    }

    public WishBanner banner() {
        return WishBanner.values()[entityData.get(DATA_BANNER)];
    }

    public WishRarity rarity() {
        return WishRarity.values()[entityData.get(DATA_RARITY)];
    }

    public boolean isCapturingRadiance() {
        return entityData.get(DATA_CAPTURING_RADIANCE);
    }

    public boolean isRadianceVisible() {
        return entityData.get(DATA_RADIANCE_VISIBLE);
    }

    public boolean isColorful() {
        return entityData.get(DATA_COLORFUL);
    }

    public CaptureState captureState() {
        return CaptureState.values()[entityData.get(DATA_CAPTURE_STATE)];
    }

    public boolean isChoosing() {
        return captureState() == CaptureState.CHOOSING;
    }

    public boolean isOwnedBy(Entity entity) {
        return owner != null && owner.equals(entity.getUUID());
    }

    public void clear() {
        resolved = true;
        waitingForChoice = false;
        choiceAnswered = false;
        selectedChoice = NO_CHOICE;
        previews = List.of();
        starglitter = List.of();
        entityData.set(DATA_CAPTURE_STATE, CaptureState.NONE.ordinal());
        if (owner != null) ChoiceRegistry.cancel(owner, NO_CHOICE);
        discard();
    }

    public void resolve(ServerLevel level) {
        resolve(level, true);
    }

    public boolean resolve(ServerLevel level, boolean playRevealSound) {
        if (resolved) return false;
        var table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable()));
        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, position()).withOptionalParameter(LootContextParams.THIS_ENTITY, this).create(LootContextParamSets.CHEST);
        for (var stack : table.getRandomItems(params)) spawnAtLocation(stack);
        if (playRevealSound)
            level.playSound(null, getX(), getY(), getZ(), rarity().sound(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        clear();
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        applyDescent();
        if (!(level() instanceof ServerLevel serverLevel)) return;
        age++;
        if (isCapturingRadiance()) {
            tickCapture(serverLevel);
            return;
        }
        cullWhenStranded(serverLevel);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!(level() instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        if (!isOwnedBy(player)) {
            player.displayClientMessage(WishReports.notOwner(), true);
            return InteractionResult.SUCCESS;
        }

        if (isChoosing()) {
            if (player instanceof ServerPlayer serverPlayer) {
                giveUp(serverLevel, serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

        if (isCapturingRadiance()) {
            if (captureState() == CaptureState.READY) {
                entityData.set(DATA_CAPTURE_STATE, CaptureState.CHOOSING.ordinal());
                openChoice(serverLevel);
                if (player instanceof ServerPlayer serverPlayer) Advancements.grant(serverPlayer, "capture_tomorrows_starlight");
            }
            return InteractionResult.SUCCESS;
        }

        resolve(serverLevel);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(CULL_MARGIN, 0.0D, CULL_MARGIN).expandTowards(0.0D, CULL_MARGIN, 0.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BANNER, WishBanner.ACQUAINT.ordinal());
        builder.define(DATA_RARITY, WishRarity.BLUE.ordinal());
        builder.define(DATA_CAPTURING_RADIANCE, false);
        builder.define(DATA_COLORFUL, false);
        builder.define(DATA_CAPTURE_STATE, CaptureState.NONE.ordinal());
        builder.define(DATA_RADIANCE_VISIBLE, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("owner")) owner = compound.getUUID("owner");
        entityData.set(DATA_BANNER, compound.getInt("banner"));
        entityData.set(DATA_RARITY, compound.getInt("rarity"));
        entityData.set(DATA_CAPTURING_RADIANCE, compound.getBoolean("capturing_radiance"));
        entityData.set(DATA_COLORFUL, compound.getBoolean("colorful"));
        age = compound.getInt("age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (owner != null) compound.putUUID("owner", owner);
        compound.putInt("banner", entityData.get(DATA_BANNER));
        compound.putInt("rarity", entityData.get(DATA_RARITY));
        compound.putBoolean("capturing_radiance", entityData.get(DATA_CAPTURING_RADIANCE));
        compound.putBoolean("colorful", entityData.get(DATA_COLORFUL));
        compound.putInt("age", age);
    }

    public void applyDisplay(WishRarity rarity) {
        entityData.set(DATA_RARITY, rarity.ordinal());
    }

    private void tickCapture(ServerLevel level) {
        switch (captureState()) {
            case NONE -> entityData.set(DATA_CAPTURE_STATE, CaptureState.PREPARING.ordinal());
            case PREPARING -> {
                if (age >= CAPTURE_CONVERT_DELAY) convert(level);
            }
            case CHOOSING -> tickChoice(level);
        }
        if (resolved) return;
        cullWhenStranded(level);
    }

    private void convert(ServerLevel level) {
        entityData.set(DATA_CAPTURE_STATE, CaptureState.READY.ordinal());
        entityData.set(DATA_RADIANCE_VISIBLE, true);
        PacketDistributor.sendToPlayersNear(level, null, getX(), getY(), getZ(), CULL_RADIUS, ParticleBurstPayload.at(CAPTURE_PARTICLE_KIND, this));
        level.playSound(null, getX(), getY(), getZ(), PGCSounds.CAPTURING_RADIANCE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void tickChoice(ServerLevel level) {
        if (choiceAnswered) {
            entityData.set(DATA_CAPTURE_STATE, CaptureState.NONE.ordinal());
            grantChoice(level, level.getEntity(owner) instanceof ServerPlayer player ? player : null);
            return;
        }

        if (!waitingForChoice || age % 5 != 0) return;

        if (!(level.getEntity(owner) instanceof ServerPlayer player) || !player.isAlive()) {
            giveUp(level, null);
        }
    }

    private void openChoice(ServerLevel level) {
        if (!(level.getEntity(owner) instanceof ServerPlayer player)) return;

        rollRewards(level);
        if (previews.size() < CAPTURE_CHOICE_COUNT) {
            grantStarglitter(player);
            level.playSound(null, getX(), getY(), getZ(), PGCSounds.WISH_GOLD.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            clear();
            return;
        }

        waitingForChoice = true;
        choiceAnswered = false;
        ChoiceRegistry.open(player, Component.translatable("wish.primogemcraft.capturing_radiance"), Component.translatable("wish.primogemcraft.capturing_radiance.choice_hint"), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.DEFAULT_CARDS, ChoiceSupport.spin(ChoiceSpinSpeed.FAST), cards(), index -> {
            selectedChoice = index;
            choiceAnswered = true;
            waitingForChoice = false;
            return true;
        });
    }

    private List<ChoiceCard> cards() {
        var cards = new ArrayList<ChoiceCard>(previews.size());
        for (var stack : previews) cards.add(ChoiceSupport.card(stack));
        return List.copyOf(cards);
    }

    private void giveUp(ServerLevel level, ServerPlayer player) {
        if (player != null) ChoiceRegistry.cancel(player.getUUID(), NO_CHOICE);
        resolve(level, false);
    }

    private void grantChoice(ServerLevel level, ServerPlayer player) {
        if (player != null) {
            if (selectedChoice >= 0 && selectedChoice < previews.size()) {
                var prize = previews.get(selectedChoice);
                player.getInventory().placeItemBackInInventory(prize.copy());
                player.displayClientMessage(Component.translatable("message.primogemcraft.wish.capturing_radiance_choice", prize.getHoverName()), false);
            }
            grantStarglitter(player);
        }
        level.playSound(null, getX(), getY(), getZ(), PGCSounds.WISH_GOLD.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        clear();
    }

    private void grantStarglitter(ServerPlayer player) {
        for (var stack : starglitter) player.getInventory().placeItemBackInInventory(stack.copy());
    }

    private void rollRewards(ServerLevel level) {
        var table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable()));
        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, position()).withOptionalParameter(LootContextParams.THIS_ENTITY, this).create(LootContextParamSets.CHEST);

        var ordinary = new ArrayList<ItemStack>();
        var glitter = new ArrayList<ItemStack>();
        for (var stack : table.getRandomItems(params)) {
            if (isStarglitter(stack)) glitter.add(stack);
            else ordinary.add(stack);
        }

        previews = ChoiceSupport.distinct(ordinary, () -> ordinaryRolls(table, params), CAPTURE_CHOICE_COUNT, PREVIEW_ROLL_ATTEMPTS);
        starglitter = List.copyOf(glitter);
    }

    private static List<ItemStack> ordinaryRolls(LootTable table, LootParams params) {
        var ordinary = new ArrayList<ItemStack>();
        for (var stack : table.getRandomItems(params)) {
            if (!isStarglitter(stack)) ordinary.add(stack);
        }
        return ordinary;
    }

    private static boolean isStarglitter(ItemStack stack) {
        return stack.is(PGCItems.MASTERLESS_STARGLITTER.get());
    }

    private void applyDescent() {
        var movement = getDeltaMovement();
        setDeltaMovement(movement.x, -DESCENT_SPEED, movement.z);
        move(MoverType.SELF, getDeltaMovement());
        if (onGround()) setDeltaMovement(Vec3.ZERO);
    }

    private void cullWhenStranded(ServerLevel level) {
        if (getY() < level.getMinBuildHeight() - CULL_MARGIN) clear();
    }

    private ResourceLocation lootTable() {
        return rarity().lootTable();
    }

    public enum CaptureState {
        NONE, PREPARING, READY, CHOOSING
    }
}
