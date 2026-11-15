package net.per.primogemcraft.registry;

import net.minecraft.registry.Registries;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.entity.misc.DendroCoreEntity;
import net.per.primogemcraft.entity.misc.FallingMoraPileEntity;
import net.per.primogemcraft.entity.misc.HertaOtherworldBranchTowerEntity;
import net.per.primogemcraft.entity.misc.RandomEventEntity;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;
import net.minecraft.registry.RegistryKeys;

public class PGCEntities {
    public static final String WISH_ENTITY_NAME = "wish_entity";
    public static final String DENDRO_CORE_NAME = "dendro_core";
    public static final String ABUNDANCE_BLIGHT_ZOMBIE_NAME = "abundance_blight_zombie";
    public static final String XIAO_LANTERN_NAME = "xiao_lantern";
    public static final String HERTA_OTHERWORLD_BRANCH_TOWER_NAME = "herta_otherworld_branch_tower";
    public static final String RANDOM_EVENT_NAME = "random_event";
    public static final String LIVING_ITEM_NAME = "living_item";
    public static final String FALLING_MORA_PILE_NAME = "falling_mora_pile";

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(RegistryKeys.ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ZiplineAnchorEntity>> ZIPLINE_ANCHOR =
            REGISTRY.register("zipline_anchor", () -> EntityType.Builder.create(ZiplineAnchorEntity::new, SpawnGroup.MISC)
                    .setDimensions(3.0F, 6.5F).makeFireImmune().maxTrackingRange(10).trackingTickInterval(20).build("zipline_anchor"));

    public static final DeferredHolder<EntityType<?>, EntityType<ZiplineCarrierEntity>> ZIPLINE_CARRIER =
            REGISTRY.register("zipline_carrier", () -> EntityType.Builder.create(ZiplineCarrierEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.1F, 0.1F).makeFireImmune().disableSaving().disableSummon().maxTrackingRange(10).trackingTickInterval(1).build("zipline_carrier"));

    public static final DeferredHolder<EntityType<?>, EntityType<FallingMoraPileEntity>> FALLING_MORA_PILE =
            REGISTRY.register(FALLING_MORA_PILE_NAME, () -> EntityType.Builder.<FallingMoraPileEntity>create(FallingMoraPileEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.98F, 0.98F)
                    .maxTrackingRange(10)
                    .trackingTickInterval(20)
                    .build(FALLING_MORA_PILE_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<WishEntity>> WISH_ENTITY =
            REGISTRY.register(WISH_ENTITY_NAME, () -> EntityType.Builder.create(WishEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.75F, 0.75F)
                    .disableSummon()
                    .makeFireImmune()
                    .build(WISH_ENTITY_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<DendroCoreEntity>> DENDRO_CORE =
            REGISTRY.register(DENDRO_CORE_NAME, () -> EntityType.Builder.create(DendroCoreEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.2F, 0.2F)
                    .maxTrackingRange(8)
                    .build(DENDRO_CORE_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<AbundanceBlightZombieEntity>> ABUNDANCE_BLIGHT_ZOMBIE =
            REGISTRY.register(ABUNDANCE_BLIGHT_ZOMBIE_NAME, () -> EntityType.Builder.create(AbundanceBlightZombieEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6F, 1.8F)
                    .makeFireImmune()
                    .maxTrackingRange(8)
                    .build(ABUNDANCE_BLIGHT_ZOMBIE_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<XiaoLanternEntity>> XIAO_LANTERN =
            REGISTRY.register(XIAO_LANTERN_NAME, () -> EntityType.Builder.create(XiaoLanternEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.6F, 1.8F)
                    .makeFireImmune()
                    .maxTrackingRange(10)
                    .build(XIAO_LANTERN_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<HertaOtherworldBranchTowerEntity>> HERTA_OTHERWORLD_BRANCH_TOWER =
            REGISTRY.register(HERTA_OTHERWORLD_BRANCH_TOWER_NAME, () -> EntityType.Builder.create(HertaOtherworldBranchTowerEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.6F, 1.8F)
                    .makeFireImmune()
                    .maxTrackingRange(8)
                    .build(HERTA_OTHERWORLD_BRANCH_TOWER_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<RandomEventEntity>> RANDOM_EVENT =
            REGISTRY.register(RANDOM_EVENT_NAME, () -> EntityType.Builder.create(RandomEventEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.6F, 0.6F)
                    .makeFireImmune()
                    .disableSaving()
                    .maxTrackingRange(10)
                    .build(RANDOM_EVENT_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<LivingItemEntity>> LIVING_ITEM =
            REGISTRY.register(LIVING_ITEM_NAME, () -> EntityType.Builder.create(LivingItemEntity::new, SpawnGroup.CREATURE)
                    .setDimensions(0.6F, 0.6F)
                    .maxTrackingRange(64)
                    .build(LIVING_ITEM_NAME));
}
