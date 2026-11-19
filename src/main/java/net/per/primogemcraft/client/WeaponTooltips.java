package net.per.primogemcraft.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.per.primogemcraft.system.weapon.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class WeaponTooltips {
    private static final int NAME_INDEX = 1;

    private WeaponTooltips() {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Factories {
        private Factories() {
        }

        @SubscribeEvent
        public static void registerFactories(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(WeaponTooltip.class, WeaponTooltipRenderer::new);
            event.register(WeaponTutorialRenderer.Content.class, WeaponTutorialRenderer::new);
        }
    }

    @SubscribeEvent
    public static void gatherComponents(RenderTooltipEvent.GatherComponents event) {
        var stack = event.getItemStack();
        if (!(stack.getItem() instanceof WishWeapon weapon)) return;
        var state = WeaponState.of(stack);
        var extra = WeaponEnhancement.extraRefinement(Minecraft.getInstance().player, stack);
        var stacks = weapon.stacks(stack);
        var permanent = stacks.permanent() && stacks.visible() ? stacks : WeaponStacks.NONE;
        event.getTooltipElements().add(NAME_INDEX, Either.right(
                new WeaponTooltip(state.level(), state.refinements(), extra, permanent.value(), permanent.capacity())));
        if (net.per.primogemcraft.system.wish.WishTooltips.showsTutorial()) event.getTooltipElements().add(Either.right(
                new WeaponTutorialRenderer.Content(stack.getItem().getDefaultInstance())));
    }
}
