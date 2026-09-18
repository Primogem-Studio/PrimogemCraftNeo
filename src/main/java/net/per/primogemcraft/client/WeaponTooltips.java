package net.per.primogemcraft.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.per.primogemcraft.system.weapon.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class WeaponTooltips {
    private static final int NAME_INDEX = 1;

    private WeaponTooltips() {
    }

    @SubscribeEvent
    public static void registerFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WeaponTooltip.class, WeaponTooltipRenderer::new);
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
    }
}
