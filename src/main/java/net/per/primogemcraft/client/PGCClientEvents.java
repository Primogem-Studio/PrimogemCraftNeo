package net.per.primogemcraft.client;

import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.per.primogemcraft.client.gui.ContainerWindowScreen;
import net.per.primogemcraft.client.gui.GorgeousSmithingTableScreen;
import net.per.primogemcraft.client.gui.StellarConverterScreen;
import net.per.primogemcraft.item.misc.DendroCoreSpawnEggItem;
import net.per.primogemcraft.item.misc.StackOfCosmicBigLottoItem;
import net.per.primogemcraft.registry.*;
import net.per.primogemcraft.render.entity.*;
import net.per.primogemcraft.render.entity.model.DendroCoreModel;
import net.per.primogemcraft.render.entity.model.HertaOtherworldBranchTowerModel;
import net.per.primogemcraft.render.entity.model.WishEntityModel;
import net.per.primogemcraft.render.entity.model.XiaoLanternModel;
import net.per.primogemcraft.system.weapon.WishWeapon;
import net.per.primogemcraft.system.wish.WishDrops;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class PGCClientEvents {
    private static final String CLOTH_CONFIG_ID = "cloth_config";
    private static final ResourceLocation WEAPON_RECOVERY_PROPERTY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon_recovery");
    private static final ResourceLocation ELEMENT_TYPE_PROPERTY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "element_type");
    private static final ResourceLocation LOTTO_COUNT_PROPERTY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lotto_count");
    private static final ResourceLocation COUNT_PROPERTY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "count");
    private static final ResourceLocation BLOCKING_PROPERTY = ResourceLocation.withDefaultNamespace("blocking");
    private static final ResourceLocation SPEAR_CHARGE_LAYER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "jade_winged_spear_charge");
    private static final float ELEMENT_TYPE_STEP = 0.125F;
    private static final IItemDecorator STACKS_DECORATOR = new WeaponStacksDecorator();

    @SubscribeEvent
    public static void registerConfigScreen(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded(CLOTH_CONFIG_ID)) return;
        ModList.get().getModContainerById(MOD_ID).orElseThrow().registerExtensionPoint(IConfigScreenFactory.class, PGCConfigScreen::create);
    }

    @SubscribeEvent
    public static void registerModelProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.registerGeneric(
                WEAPON_RECOVERY_PROPERTY,
                (ClampedItemPropertyFunction) (stack, level, entity, seed) -> stack.get(PGCDataComponents.WEAPON_RECOVERY.get()) == null ? 0.0F : 1.0F));
        event.enqueueWork(() -> ItemProperties.registerGeneric(
                ELEMENT_TYPE_PROPERTY,
                (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
                    var type = stack.get(PGCDataComponents.ELEMENT_TYPE.get());
                    return type == null ? 0.0F : type * ELEMENT_TYPE_STEP;
                }));
        event.enqueueWork(() -> ItemProperties.register(
                PGCItems.STACK_OF_COSMIC_BIG_LOTTO.get(),
                LOTTO_COUNT_PROPERTY,
                (stack, level, entity, seed) -> StackOfCosmicBigLottoItem.lottos(stack)));
        event.enqueueWork(() -> {
            ItemPropertyFunction count = (stack, level, entity, seed) -> stack.getCount();
            for (var item : List.of(PGCItems.MORA.get(), PGCItems.PRIMOGEM_SHARD.get(), PGCItems.ELEMENTAL_MOLTEN_BEAD_FRAGMENT.get()))
                ItemProperties.register(item, COUNT_PROPERTY, count);
        });
        event.enqueueWork(() -> {
            for (var shield : List.of(PGCItems.SHIELD_OF_RADIANT_WILL.get(), PGCItems.IRON_TRASH_CAN_LID.get(), PGCItems.SHIELD_OF_WEALTH_AND_HOPE.get()))
                ItemProperties.register(shield, BLOCKING_PROPERTY, (ClampedItemPropertyFunction) (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WishEntityModel.LAYER_LOCATION, WishEntityModel::createBodyLayer);
        event.registerLayerDefinition(DendroCoreModel.LAYER, DendroCoreModel::createBodyLayer);
        event.registerLayerDefinition(XiaoLanternModel.LAYER, XiaoLanternModel::createBodyLayer);
        event.registerLayerDefinition(HertaOtherworldBranchTowerModel.LAYER, HertaOtherworldBranchTowerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PGCEntities.ZIPLINE_ANCHOR.get(), ZiplineAnchorRenderer::new);
        event.registerEntityRenderer(PGCEntities.ZIPLINE_CARRIER.get(), ZiplineCarrierRenderer::new);
        event.registerEntityRenderer(PGCEntities.FALLING_MORA_PILE.get(), FallingBlockRenderer::new);
        event.registerEntityRenderer(PGCEntities.WISH_ENTITY.get(), WishEntityRenderer::new);
        event.registerEntityRenderer(PGCEntities.DENDRO_CORE.get(), DendroCoreRenderer::new);
        event.registerEntityRenderer(PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get(), AbundanceBlightZombieRenderer::new);
        event.registerEntityRenderer(PGCEntities.XIAO_LANTERN.get(), XiaoLanternRenderer::new);
        event.registerEntityRenderer(PGCEntities.HERTA_OTHERWORLD_BRANCH_TOWER.get(), HertaOtherworldBranchTowerRenderer::new);
        event.registerEntityRenderer(PGCEntities.RANDOM_EVENT.get(), RandomEventRenderer::new);
        event.registerEntityRenderer(PGCEntities.LIVING_ITEM.get(), LivingItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PGCParticles.MARA.get(), MaraParticle::provider);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, SPEAR_CHARGE_LAYER, new JadeWingedSpearOverlay());
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        for (var entry : PGCItemBars.entries())
            event.register(entry.item(), new CustomBarDecorator(entry.color(), entry.depleting()));
        for (var holder : PGCItems.REGISTRY.getEntries())
            if (holder.get() instanceof WishWeapon) event.register(holder.get(), STACKS_DECORATOR);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 ? DendroCoreSpawnEggItem.baseColor() : DendroCoreSpawnEggItem.dotColor(),
                PGCItems.DENDRO_CORE_SPAWN_EGG.get());
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(PGCMenus.GORGEOUS_SMITHING_TABLE.get(), GorgeousSmithingTableScreen::new);
        event.register(PGCMenus.STELLAR_CONVERTER.get(), StellarConverterScreen::new);
        event.register(PGCMenus.CONTAINER_WINDOW.get(), ContainerWindowScreen::new);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ZiplineModel.INSTANCE);
        event.registerReloadListener(new WishDrops());
    }
}
