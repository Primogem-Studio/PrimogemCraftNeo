package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.component.WeaponRecovery;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponAttributes;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WishWeapon;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PlayerItems;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class Weapon {
    private static final String LEVEL = "level";
    private static final String REFINEMENT = "refinement";
    private static final String WEAPON = "weapon";
    private static final String AMOUNT = "amount";
    private static final String SUPERIMPOSER = "superimposer";
    private static final String SET_KEY = "command.primogemcraft.weapon.set";
    private static final String MAX_KEY = "command.primogemcraft.weapon.max";
    private static final String GIVE_KEY = "command.primogemcraft.weapon.give";
    private static final String NOT_WEAPON_KEY = "command.primogemcraft.weapon.not_weapon";
    private static final String INVALID_ITEM_KEY = "command.primogemcraft.weapon.invalid_item";
    private static final int PERMISSION = 2;
    private static final int MIN_LEVEL = 1;
    private static final int MIN_REFINEMENT = 1;
    private static final int MIN_TEMPORARY = 0;
    private static final int ORE_AMOUNT = 64;
    private static final int SUPERIMPOSER_AMOUNT = 5;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext build) {
        dispatcher.register(Commands.literal(MOD_ID).then(Commands.literal("weapon").requires(source -> source.hasPermission(PERMISSION))
                .executes(context -> info(context.getSource()))
                .then(Commands.literal("info").executes(context -> info(context.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument(LEVEL, IntegerArgumentType.integer(MIN_LEVEL)
                        ).then(Commands.argument(REFINEMENT, IntegerArgumentType.integer(MIN_REFINEMENT)
                        ).executes(context -> set(context.getSource(), level(context), refinement(context), false)
                        ).then(Commands.literal(SUPERIMPOSER
                        ).executes(context -> set(context.getSource(), level(context), refinement(context), true))))))
                .then(Commands.literal("max").executes(context -> max(context.getSource())))
                .then(Commands.literal("temp")
                        .then(Commands.argument(AMOUNT, IntegerArgumentType.integer(MIN_TEMPORARY, WeaponEnhancement.MAX_TEMPORARY_REFINEMENT)
                        ).executes(context -> temporary(context.getSource(), IntegerArgumentType.getInteger(context, AMOUNT)))))
                .then(Commands.literal("give")
                        .then(Commands.argument(WEAPON, ItemArgument.item(build)
                        ).then(Commands.argument(LEVEL, IntegerArgumentType.integer(MIN_LEVEL)
                        ).then(Commands.argument(REFINEMENT, IntegerArgumentType.integer(MIN_REFINEMENT)
                        ).executes(context -> give(context, level(context), refinement(context)))))))
                .then(Commands.literal("recovery").executes(context -> recovery(context.getSource())))
                .then(Commands.literal("materials").executes(context -> giveMaterials(context.getSource())))
        ));
    }

    private static int level(CommandContext<CommandSourceStack> context) {
        return IntegerArgumentType.getInteger(context, LEVEL);
    }

    private static int refinement(CommandContext<CommandSourceStack> context) {
        return IntegerArgumentType.getInteger(context, REFINEMENT);
    }

    private static int info(CommandSourceStack source) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof WishWeapon)) return notWeapon(source);
        var state = WeaponState.of(weapon);
        var cap = WeaponEnhancement.maxLevel(state.refinements());
        message(source, "command.primogemcraft.weapon.info.header", weapon.getHoverName());
        message(source, "command.primogemcraft.weapon.info.level", state.level(), cap, state.xp(), requirement(state.level(), cap));
        message(source, "command.primogemcraft.weapon.info.refinement", state.refinements(),
                WeaponEnhancement.refinementOf(player, weapon) - state.refinements(),
                state.duplicateLayers(), state.superimposerLayers());
        message(source, "command.primogemcraft.weapon.info.bonus",
                WishReports.number(WeaponAttributes.levelAttackBonus(weapon, player, state.level()), ChatFormatting.AQUA));
        return state.level();
    }

    private static int set(CommandSourceStack source, int level, int refinement, boolean superimposer) throws CommandSyntaxException {
        return apply(source, level, refinement, superimposer, SET_KEY);
    }

    private static int max(CommandSourceStack source) throws CommandSyntaxException {
        return apply(source, WeaponEnhancement.maxLevel(WeaponEnhancement.MAX_REFINEMENT), WeaponEnhancement.MAX_REFINEMENT, false, MAX_KEY);
    }

    private static int apply(CommandSourceStack source, int level, int refinement, boolean superimposer, String key) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof WishWeapon)) return notWeapon(source);
        var state = state(level, refinement, superimposer);
        WeaponState.set(weapon, state);
        refresh(player, weapon);
        message(source, key, state.level(), state.refinements());
        return state.level();
    }

    private static int temporary(CommandSourceStack source, int amount) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof WishWeapon)) return notWeapon(source);
        var state = WeaponState.of(weapon);
        var applied = state.temporarilyBy(amount - state.temporaryRefinements());
        WeaponState.set(weapon, applied);
        refresh(player, weapon);
        message(source, "command.primogemcraft.weapon.temp", applied.temporaryRefinements());
        return applied.temporaryRefinements();
    }

    private static int give(CommandContext<CommandSourceStack> context, int level, int refinement) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();
        var weapon = ItemArgument.getItem(context, WEAPON).createItemStack(1, false);
        if (!(weapon.getItem() instanceof WishWeapon)) {
            message(source, INVALID_ITEM_KEY, weapon.getHoverName());
            return 0;
        }
        var state = state(level, refinement, false);
        WeaponState.set(weapon, state);
        WeaponAttributes.refreshLevel(weapon, player);
        PlayerItems.give(player, weapon);
        message(source, GIVE_KEY, weapon.getHoverName(), state.level(), state.refinements());
        return state.level();
    }

    private static int recovery(CommandSourceStack source) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof WishWeapon)) return notWeapon(source);
        var recovery = new WeaponRecovery(WeaponState.of(weapon), WishWeapon.isFiveStar(weapon));
        var ore = new ItemStack(PGCItems.SPECIALLY_TREATED_FINE_ORE.get());
        ore.set(PGCDataComponents.WEAPON_RECOVERY.get(), recovery);
        PlayerItems.give(player, ore);
        message(source, "command.primogemcraft.weapon.recovery");
        for (var refund : WeaponEnhancement.refund(recovery))
            message(source, "command.primogemcraft.weapon.recovery.refund", refund.getCount(), refund.getHoverName());
        return 1;
    }

    private static int giveMaterials(CommandSourceStack source) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var materials = materials();
        for (var material : materials) PlayerItems.give(player, material);
        message(source, "command.primogemcraft.weapon.materials");
        return materials.size();
    }

    private static List<ItemStack> materials() {
        var stacks = new ArrayList<ItemStack>();
        stacks.add(new ItemStack(PGCItems.ENHANCEMENT_ORE.get(), ORE_AMOUNT));
        stacks.add(new ItemStack(PGCItems.FINE_ENHANCEMENT_ORE.get(), ORE_AMOUNT));
        stacks.add(new ItemStack(PGCItems.MYSTIC_ENHANCEMENT_ORE.get(), ORE_AMOUNT));
        stacks.add(new ItemStack(PGCItems.SPECIALLY_TREATED_FINE_ORE.get()));
        for (var index = 0; index < SUPERIMPOSER_AMOUNT; index++) stacks.add(new ItemStack(PGCItems.CUSTOM_SUPERIMPOSER.get()));
        return stacks;
    }

    private static WeaponState state(int level, int refinement, boolean superimposer) {
        var base = WeaponState.INITIAL.leveledTo(level, 0);
        return superimposer ? base.superimposedBy(refinement - 1) : base.duplicatedBy(refinement - 1);
    }

    private static Component requirement(int level, int cap) {
        return level >= cap ? Component.translatable("weapon.primogemcraft.level.max")
                : WishReports.number(WeaponEnhancement.requirement(level), ChatFormatting.AQUA);
    }

    private static void refresh(ServerPlayer player, ItemStack weapon) {
        WeaponAttributes.refreshLevel(weapon, player);
        WeaponAttributes.refreshPassive(weapon, player, player.getInventory().selected);
        player.containerMenu.broadcastChanges();
    }

    private static int notWeapon(CommandSourceStack source) {
        message(source, NOT_WEAPON_KEY);
        return 0;
    }

    private static void message(CommandSourceStack source, String key, Object... arguments) {
        source.sendSuccess(() -> Component.translatable(key, arguments), false);
    }
}
