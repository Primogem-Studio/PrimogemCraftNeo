package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.entity.misc.SkywardHarpVortexEntity;
import net.per.primogemcraft.system.weapon.BowAttackCycle;
import net.per.primogemcraft.system.weapon.ViridescentHuntStats;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class TheViridescentHuntItem extends WishWeaponBowItem {
    private static final String COOLDOWN = "the_viridescent_hunt_cyclone";

    public TheViridescentHuntItem(Properties properties) {
        super(properties.fireResistant().rarity(Rarity.EPIC), new BowAttackCycle(8, 16, 5), 3.0F,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/item/the_viridescent_hunt.png"),
                ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png"), SoundEvents.ARROW_SHOOT);
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
        return List.of(WeaponDescription.of("passive", "cyclone",
                WishReports.percent(ViridescentHuntStats.damageRatio(refinement), ChatFormatting.AQUA),
                WishReports.number(ViridescentHuntStats.cooldown(refinement) / 20.0D, ChatFormatting.AQUA)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) trySpawnCyclone(player, stack, target.getBoundingBox().getCenter());
        return super.hurtEnemy(stack, target, attacker);
    }

    public static void trySpawnCyclone(Player owner, ItemStack weapon, Vec3 position) {
        if (!(owner.level() instanceof ServerLevel level) || !owner.isAlive() || owner.isSpectator()
                || !PGCTimer.isDone(owner, COOLDOWN)
                || owner.getRandom().nextDouble() >= ViridescentHuntStats.TRIGGER_CHANCE) return;
        var refinement = WeaponEnhancement.refinementOf(owner, weapon);
        PGCTimer.set(owner, COOLDOWN, ViridescentHuntStats.cooldown(refinement));
        SkywardHarpVortexEntity.spawnWindEye(level, owner, position, ViridescentHuntStats.damageRatio(refinement));
    }
}
