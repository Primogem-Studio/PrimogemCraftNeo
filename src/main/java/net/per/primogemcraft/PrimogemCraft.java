package net.per.primogemcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.per.primogemcraft.compat.attachment.Attachments;
import net.per.primogemcraft.compat.attribute.FlightCompat;
import net.per.primogemcraft.compat.event.EffectCallbacks;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCCreativeTabs;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCFeatures;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCLootConditions;
import net.per.primogemcraft.registry.PGCMenus;
import net.per.primogemcraft.registry.PGCParticles;
import net.per.primogemcraft.registry.PGCPointsOfInterest;
import net.per.primogemcraft.registry.PGCPotions;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.registry.PGCRecipeTypes;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.registry.PGCVillagerProfessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimogemCraft implements ModInitializer {
    public static final String MOD_ID = "primogemcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinecraftServer currentServer;

    public static MinecraftServer server() {
        return currentServer;
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
        Attachments.register();
        EffectCallbacks.register();
        FlightCompat.register();
        PGCBlocks.REGISTRY.register(null);
        PGCBlockEntities.REGISTRY.register(null);
        PGCItems.REGISTRY.register(null);
        PGCSounds.REGISTRY.register(null);
        PGCDataComponents.REGISTRY.register(null);
        PGCEffects.REGISTRY.register(null);
        PGCParticles.REGISTRY.register(null);
        PGCPotions.REGISTRY.register(null);
        PGCAttachments.REGISTRY.register(null);
        PGCEntities.REGISTRY.register(null);
        PGCFeatures.REGISTRY.register(null);
        PGCPointsOfInterest.REGISTRY.register(null);
        PGCVillagerProfessions.REGISTRY.register(null);
        PGCMenus.REGISTRY.register(null);
        PGCRecipeSerializers.REGISTRY.register(null);
        PGCRecipeTypes.REGISTRY.register(null);
        PGCLootConditions.REGISTRY.register(null);
        PGCCreativeTabs.REGISTRY.register(null);
        net.per.primogemcraft.compat.worldgen.PGCBiomeModifications.register();
        net.per.primogemcraft.compat.loot.PGCLootInjection.register();

        // 网络：双端注册编解码器与 C2S 收包器（S2C 收包器在客户端入口注册）
        net.per.primogemcraft.network.PGCNetwork.registerCommon();

        // 命令
        net.per.primogemcraft.command.PGCCommands.register();

        // 实体与属性
        net.per.primogemcraft.entity.EntityEvents.register();
        net.per.primogemcraft.util.PGCTimer.register();
        net.per.primogemcraft.util.TemporaryAttributes.register();

        // 奇物
        net.per.primogemcraft.system.choice.ChoiceRegistry.register();
        net.per.primogemcraft.system.curio.CurioEvents.register();
        net.per.primogemcraft.system.curio.CuckooClockDuplicates.register();
        net.per.primogemcraft.system.curio.Parasitism.register();
        net.per.primogemcraft.system.curio.effect.CurioEffects.register();

        // 元素与效果
        net.per.primogemcraft.system.element.ElementSets.register();
        net.per.primogemcraft.system.element.effect.BurningRecoveryEffect.register();
        net.per.primogemcraft.system.element.effect.CleansingBubbleEffect.register();
        net.per.primogemcraft.system.element.effect.ThePastEffect.register();

        // 武器与工具
        net.per.primogemcraft.system.weapon.WeaponEvents.register();
        net.per.primogemcraft.system.weapon.ThrustDash.register();
        net.per.primogemcraft.system.weapon.TrashCanTenacityEffect.register();
        net.per.primogemcraft.system.tool.ThreeMoonsBlessingEvents.register();
        net.per.primogemcraft.item.weapon.PhilosophiesOfGoldItem.register();
        net.per.primogemcraft.enchantment.EnchantmentEvents.register();

        // 祈愿与随机事件
        net.per.primogemcraft.system.wish.WishEvents.register();
        net.per.primogemcraft.system.event.EventEvents.register();
        net.per.primogemcraft.system.abundance.AbundanceEvents.register();
        net.per.primogemcraft.system.living.LivingItemEvents.register();
        net.per.primogemcraft.system.zipline.ZiplineEvents.register();

        // 物品事件与村民交易、酿造
        net.per.primogemcraft.item.misc.DreamSakuraEvents.register();
        net.per.primogemcraft.item.misc.OtherworldBankbookEvents.register();
        net.per.primogemcraft.item.misc.TrueGalaxyLottoEvents.register();
        net.per.primogemcraft.system.village.PrimogemScholarTrades.register();
        net.per.primogemcraft.recipe.AbundanceElixirBrewing.register();

        // 联动模组（各自内部判断是否加载）
        net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration.register();
        net.per.primogemcraft.collab.tacz.TaczIntegration.register();
        net.per.primogemcraft.collab.teyvatdelight.TeyvatDelightIntegration.register();

        LOGGER.info("PrimogemCraft initializing");
    }
}