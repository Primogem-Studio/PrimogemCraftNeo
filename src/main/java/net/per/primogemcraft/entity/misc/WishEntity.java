package net.per.primogemcraft.entity.misc;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
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
    private static final TrackedData<Integer> DATA_BANNER = DataTracker.registerData(WishEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DATA_RARITY = DataTracker.registerData(WishEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> DATA_CAPTURING_RADIANCE = DataTracker.registerData(WishEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_COLORFUL = DataTracker.registerData(WishEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_SUNGLASSES = DataTracker.registerData(WishEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> DATA_CAPTURE_STATE = DataTracker.registerData(WishEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> DATA_RADIANCE_VISIBLE = DataTracker.registerData(WishEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    private boolean waitingForChoice;
    private boolean choiceAnswered;
    private int selectedChoice = NO_CHOICE;
    private List<ItemStack> previews = List.of();
    private List<ItemStack> starglitter = List.of();

    public WishEntity(EntityType<? extends WishEntity> entityType, World level) {
        super(entityType, level);
    }

    public static void spawn(ServerWorld level, ServerPlayerEntity player, WishResult result, double x, double y, double z) {
        var wish = new WishEntity(PGCEntities.WISH_ENTITY.get(), level);
        wish.owner = player.getUuid();
        wish.dataTracker.set(DATA_BANNER, result.banner().ordinal());
        wish.dataTracker.set(DATA_RARITY, result.rarity().ordinal());
        wish.dataTracker.set(DATA_CAPTURING_RADIANCE, result.capturingRadiance());
        wish.dataTracker.set(DATA_COLORFUL, result.colorful());
        wish.dataTracker.set(DATA_SUNGLASSES, result.sunglasses());
        wish.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
        var dir = player.getPos().subtract(wish.getPos()).normalize();
        wish.setYaw((float) (MathHelper.atan2(-dir.x, dir.z) * MathHelper.DEGREES_PER_RADIAN));
        level.spawnEntity(wish);
    }

    public static void spawnRing(ServerWorld level, ServerPlayerEntity player, List<WishResult> results) {
        var count = results.size();
        if (count == 1) {
            spawn(level, player, results.get(0), player.getX(), player.getY() + SINGLE_SPAWN_HEIGHT, player.getZ());
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

    private static void spawnDisc(ServerWorld level, ServerPlayerEntity player, List<WishResult> results) {
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

    private static void spawnOnCircle(ServerWorld level, ServerPlayerEntity player, List<WishResult> results, int first, int points, double radius, double stagger) {
        for (var index = 0; index < points; index++) {
            var angle = 2.0D * Math.PI * index / points + stagger;
            var x = player.getX() + radius * Math.sin(angle);
            var z = player.getZ() + radius * Math.cos(angle);
            spawn(level, player, results.get(first + index), x, player.getY() + RING_SPAWN_HEIGHT, z);
        }
    }

    public WishBanner banner() {
        return WishBanner.values()[dataTracker.get(DATA_BANNER)];
    }

    public WishRarity rarity() {
        return WishRarity.values()[dataTracker.get(DATA_RARITY)];
    }

    public boolean isCapturingRadiance() {
        return dataTracker.get(DATA_CAPTURING_RADIANCE);
    }

    public boolean isRadianceVisible() {
        return dataTracker.get(DATA_RADIANCE_VISIBLE);
    }

    public boolean isColorful() {
        return dataTracker.get(DATA_COLORFUL);
    }

    public WishRarity displayRarity() {
        if (!dataTracker.get(DATA_SUNGLASSES)) return rarity();
        return switch (rarity()) {
            case BLUE -> WishRarity.PURPLE;
            case PURPLE, GOLD -> WishRarity.GOLD;
        };
    }

    public CaptureState captureState() {
        return CaptureState.values()[dataTracker.get(DATA_CAPTURE_STATE)];
    }

    public boolean isChoosing() {
        return captureState() == CaptureState.CHOOSING;
    }

    public boolean isOwnedBy(Entity entity) {
        return owner != null && owner.equals(entity.getUuid());
    }

    public void clear() {
        resolved = true;
        waitingForChoice = false;
        choiceAnswered = false;
        selectedChoice = NO_CHOICE;
        previews = List.of();
        starglitter = List.of();
        dataTracker.set(DATA_CAPTURE_STATE, CaptureState.NONE.ordinal());
        if (owner != null) ChoiceRegistry.cancel(owner, NO_CHOICE);
        discard();
    }

    public void resolve(ServerWorld level) {
        resolve(level, true);
    }

    public boolean resolve(ServerWorld level, boolean playRevealSound) {
        if (resolved) return false;
        var table = level.getServer().getLootManager().getLootTable(lootTable());
        var params = new LootContextParameterSet.Builder(level).add(LootContextParameters.ORIGIN, getPos()).addOptional(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.CHEST);
        for (var stack : table.generateLoot(params)) dropStack(stack);
        if (playRevealSound)
            level.playSound(null, getX(), getY(), getZ(), rarity().sound(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
        clear();
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        applyDescent();
        if (!(getWorld() instanceof ServerWorld serverLevel)) return;
        age++;
        if (isCapturingRadiance()) {
            tickCapture(serverLevel);
            return;
        }
        cullWhenStranded(serverLevel);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!(getWorld() instanceof ServerWorld serverLevel)) return ActionResult.SUCCESS;

        if (!isOwnedBy(player)) {
            player.sendMessage(WishReports.notOwner(), true);
            return ActionResult.SUCCESS;
        }

        if (isChoosing()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                giveUp(serverLevel, serverPlayer);
            }
            return ActionResult.SUCCESS;
        }

        if (isCapturingRadiance()) {
            if (captureState() == CaptureState.READY) {
                dataTracker.set(DATA_CAPTURE_STATE, CaptureState.CHOOSING.ordinal());
                openChoice(serverLevel);
                if (player instanceof ServerPlayerEntity serverPlayer) Advancements.grant(serverPlayer, "capture_tomorrows_starlight");
            }
            return ActionResult.SUCCESS;
        }

        resolve(serverLevel);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public Box getVisibilityBoundingBox() {
        return super.getVisibilityBoundingBox().expand(CULL_MARGIN, 0.0D, CULL_MARGIN).expand(0.0D, CULL_MARGIN, 0.0D);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DATA_BANNER, WishBanner.ACQUAINT.ordinal());
        this.dataTracker.startTracking(DATA_RARITY, WishRarity.BLUE.ordinal());
        this.dataTracker.startTracking(DATA_CAPTURING_RADIANCE, false);
        this.dataTracker.startTracking(DATA_COLORFUL, false);
        this.dataTracker.startTracking(DATA_SUNGLASSES, false);
        this.dataTracker.startTracking(DATA_CAPTURE_STATE, CaptureState.NONE.ordinal());
        this.dataTracker.startTracking(DATA_RADIANCE_VISIBLE, false);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
        if (compound.containsUuid("owner")) owner = compound.getUuid("owner");
        dataTracker.set(DATA_BANNER, compound.getInt("banner"));
        dataTracker.set(DATA_RARITY, compound.getInt("rarity"));
        dataTracker.set(DATA_CAPTURING_RADIANCE, compound.getBoolean("capturing_radiance"));
        dataTracker.set(DATA_COLORFUL, compound.getBoolean("colorful"));
        dataTracker.set(DATA_SUNGLASSES, compound.getBoolean("sunglasses"));
        age = compound.getInt("age");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
        if (owner != null) compound.putUuid("owner", owner);
        compound.putInt("banner", dataTracker.get(DATA_BANNER));
        compound.putInt("rarity", dataTracker.get(DATA_RARITY));
        compound.putBoolean("capturing_radiance", dataTracker.get(DATA_CAPTURING_RADIANCE));
        compound.putBoolean("colorful", dataTracker.get(DATA_COLORFUL));
        compound.putBoolean("sunglasses", dataTracker.get(DATA_SUNGLASSES));
        compound.putInt("age", age);
    }

    public void applyDisplay(WishRarity rarity) {
        dataTracker.set(DATA_RARITY, rarity.ordinal());
    }

    private void tickCapture(ServerWorld level) {
        switch (captureState()) {
            case NONE -> dataTracker.set(DATA_CAPTURE_STATE, CaptureState.PREPARING.ordinal());
            case PREPARING -> {
                if (age >= CAPTURE_CONVERT_DELAY) convert(level);
            }
            case CHOOSING -> tickChoice(level);
        }
        if (resolved) return;
        cullWhenStranded(level);
    }

    private void convert(ServerWorld level) {
        dataTracker.set(DATA_CAPTURE_STATE, CaptureState.READY.ordinal());
        dataTracker.set(DATA_RADIANCE_VISIBLE, true);
        PayloadRegistrar.sendToPlayersNear(level, null, getX(), getY(), getZ(), CULL_RADIUS, ParticleBurstPayload.at(CAPTURE_PARTICLE_KIND, this));
        level.playSound(null, getX(), getY(), getZ(), PGCSounds.CAPTURING_RADIANCE.get(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    private void tickChoice(ServerWorld level) {
        if (choiceAnswered) {
            dataTracker.set(DATA_CAPTURE_STATE, CaptureState.NONE.ordinal());
            grantChoice(level, level.getPlayerByUuid(owner) instanceof ServerPlayerEntity player ? player : null);
            return;
        }

        if (!waitingForChoice || age % 5 != 0) return;

        if (!(level.getPlayerByUuid(owner) instanceof ServerPlayerEntity player) || !player.isAlive()) {
            giveUp(level, null);
        }
    }

    private void openChoice(ServerWorld level) {
        if (!(level.getPlayerByUuid(owner) instanceof ServerPlayerEntity player)) return;

        rollRewards(level);
        if (previews.size() < CAPTURE_CHOICE_COUNT) {
            grantStarglitter(player);
            level.playSound(null, getX(), getY(), getZ(), PGCSounds.WISH_GOLD.get(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            clear();
            return;
        }

        waitingForChoice = true;
        choiceAnswered = false;
        ChoiceRegistry.open(player, Text.translatable("wish.primogemcraft.capturing_radiance"), Text.translatable("wish.primogemcraft.capturing_radiance.choice_hint"), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.DEFAULT_CARDS, ChoiceSupport.spin(ChoiceSpinSpeed.FAST), cards(), index -> {
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

    private void giveUp(ServerWorld level, ServerPlayerEntity player) {
        if (player != null) ChoiceRegistry.cancel(player.getUuid(), NO_CHOICE);
        resolve(level, false);
    }

    private void grantChoice(ServerWorld level, ServerPlayerEntity player) {
        if (player != null) {
            if (selectedChoice >= 0 && selectedChoice < previews.size()) {
                var prize = previews.get(selectedChoice);
                player.getInventory().insertStack(prize.copy());
                player.sendMessage(Text.translatable("message.primogemcraft.wish.capturing_radiance_choice", prize.getName()), false);
            }
            grantStarglitter(player);
        }
        level.playSound(null, getX(), getY(), getZ(), PGCSounds.WISH_GOLD.get(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
        clear();
    }

    private void grantStarglitter(ServerPlayerEntity player) {
        for (var stack : starglitter) player.getInventory().insertStack(stack.copy());
    }

    private void rollRewards(ServerWorld level) {
        var table = level.getServer().getLootManager().getLootTable(lootTable());
        var params = new LootContextParameterSet.Builder(level).add(LootContextParameters.ORIGIN, getPos()).addOptional(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.CHEST);

        var ordinary = new ArrayList<ItemStack>();
        var glitter = new ArrayList<ItemStack>();
        for (var stack : table.generateLoot(params)) {
            if (isStarglitter(stack)) glitter.add(stack);
            else ordinary.add(stack);
        }

        previews = ChoiceSupport.distinct(ordinary, () -> ordinaryRolls(table, params), CAPTURE_CHOICE_COUNT, PREVIEW_ROLL_ATTEMPTS);
        starglitter = List.copyOf(glitter);
    }

    private static List<ItemStack> ordinaryRolls(LootTable table, LootContextParameterSet params) {
        var ordinary = new ArrayList<ItemStack>();
        for (var stack : table.generateLoot(params)) {
            if (!isStarglitter(stack)) ordinary.add(stack);
        }
        return ordinary;
    }

    private static boolean isStarglitter(ItemStack stack) {
        return stack.isOf(PGCItems.MASTERLESS_STARGLITTER.get());
    }

    private void applyDescent() {
        var movement = getVelocity();
        setVelocity(movement.x, -DESCENT_SPEED, movement.z);
        move(MovementType.SELF, getVelocity());
        if (isOnGround()) setVelocity(Vec3d.ZERO);
    }

    private void cullWhenStranded(ServerWorld level) {
        if (getY() < level.getBottomY() - CULL_MARGIN) clear();
    }

    private Identifier lootTable() {
        return rarity().lootTable();
    }

    public enum CaptureState {
        NONE, PREPARING, READY, CHOOSING
    }
}
