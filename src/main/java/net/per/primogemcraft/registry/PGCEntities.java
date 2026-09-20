package net.per.primogemcraft.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
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

public class PGCEntities {
    public static final String WISH_ENTITY_NAME = "wish_entity";
    public static final String DENDRO_CORE_NAME = "dendro_core";
    public static final String ABUNDANCE_BLIGHT_ZOMBIE_NAME = "abundance_blight_zombie";
    public static final String XIAO_LANTERN_NAME = "xiao_lantern";
    public static final String HERTA_OTHERWORLD_BRANCH_TOWER_NAME = "herta_otherworld_branch_tower";
    public static final String RANDOM_EVENT_NAME = "random_event";
    public static final String LIVING_ITEM_NAME = "living_item";
    public static final String FALLING_MORA_PILE_NAME = "falling_mora_pile";

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ZiplineAnchorEntity>> ZIPLINE_ANCHOR =
            REGISTRY.register("zipline_anchor", () -> EntityType.Builder.of(ZiplineAnchorEntity::new, MobCategory.MISC)
                    .sized(3.0F, 6.5F).fireImmune().clientTrackingRange(10).updateInterval(20).build("zipline_anchor"));

    public static final DeferredHolder<EntityType<?>, EntityType<ZiplineCarrierEntity>> ZIPLINE_CARRIER =
            REGISTRY.register("zipline_carrier", () -> EntityType.Builder.of(ZiplineCarrierEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).fireImmune().noSave().noSummon().clientTrackingRange(10).updateInterval(1).build("zipline_carrier"));

    public static final DeferredHolder<EntityType<?>, EntityType<FallingMoraPileEntity>> FALLING_MORA_PILE =
            REGISTRY.register(FALLING_MORA_PILE_NAME, () -> EntityType.Builder.<FallingMoraPileEntity>of(FallingMoraPileEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(FALLING_MORA_PILE_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<WishEntity>> WISH_ENTITY =
            REGISTRY.register(WISH_ENTITY_NAME, () -> EntityType.Builder.of(WishEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .noSummon()
                    .fireImmune()
                    .build(WISH_ENTITY_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<DendroCoreEntity>> DENDRO_CORE =
            REGISTRY.register(DENDRO_CORE_NAME, () -> EntityType.Builder.of(DendroCoreEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(8)
                    .build(DENDRO_CORE_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<AbundanceBlightZombieEntity>> ABUNDANCE_BLIGHT_ZOMBIE =
            REGISTRY.register(ABUNDANCE_BLIGHT_ZOMBIE_NAME, () -> EntityType.Builder.of(AbundanceBlightZombieEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .ridingOffset(-0.6F)
                    .clientTrackingRange(8)
                    .build(ABUNDANCE_BLIGHT_ZOMBIE_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<XiaoLanternEntity>> XIAO_LANTERN =
            REGISTRY.register(XIAO_LANTERN_NAME, () -> EntityType.Builder.of(XiaoLanternEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build(XIAO_LANTERN_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<HertaOtherworldBranchTowerEntity>> HERTA_OTHERWORLD_BRANCH_TOWER =
            REGISTRY.register(HERTA_OTHERWORLD_BRANCH_TOWER_NAME, () -> EntityType.Builder.of(HertaOtherworldBranchTowerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .build(HERTA_OTHERWORLD_BRANCH_TOWER_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<RandomEventEntity>> RANDOM_EVENT =
            REGISTRY.register(RANDOM_EVENT_NAME, () -> EntityType.Builder.of(RandomEventEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .fireImmune()
                    .noSave()
                    .clientTrackingRange(10)
                    .build(RANDOM_EVENT_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<LivingItemEntity>> LIVING_ITEM =
            REGISTRY.register(LIVING_ITEM_NAME, () -> EntityType.Builder.of(LivingItemEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.6F)
                    .clientTrackingRange(64)
                    .build(LIVING_ITEM_NAME));
}
