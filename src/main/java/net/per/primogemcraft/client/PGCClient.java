package net.per.primogemcraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.per.primogemcraft.client.gui.ContainerWindowScreen;
import net.per.primogemcraft.client.gui.GorgeousSmithingTableScreen;
import net.per.primogemcraft.client.gui.StellarConverterScreen;
import net.per.primogemcraft.registry.PGCMenus;

@Environment(EnvType.CLIENT)
public class PGCClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // S2C 收包器（依赖客户端网络 API，只能在客户端注册）
        net.per.primogemcraft.network.PGCNetwork.registerClient();

        // 断线时清空服务端同步过来的祈愿缓存
        WishLootItems.register();
        WishMaterialValues.register();

        // 实体渲染器与模型层（缺失会导致渲染线程 NPE）
        PGCClientRenderers.register();

        // 容器界面绑定：缺失时服务端开菜单后客户端没有界面，玩家会卡在无 GUI 状态
        HandledScreens.register(PGCMenus.GORGEOUS_SMITHING_TABLE.get(), GorgeousSmithingTableScreen::new);
        HandledScreens.register(PGCMenus.STELLAR_CONVERTER.get(), StellarConverterScreen::new);
        HandledScreens.register(PGCMenus.CONTAINER_WINDOW.get(), ContainerWindowScreen::new);
    }
}