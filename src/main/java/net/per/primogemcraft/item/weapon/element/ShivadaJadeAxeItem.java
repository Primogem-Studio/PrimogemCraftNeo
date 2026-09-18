package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponAxeItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class ShivadaJadeAxeItem extends WishWeaponAxeItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 20, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.SHIVADA_JADE_SLIVER);

    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double RADIUS = 4.5D;
    private static final int MIN_DAMAGE = 3;
    private static final int MAX_DAMAGE = 24;
    private static final double CONSUME_CHANCE = 0.5D;
    private static final int COOLDOWN = 200;
    private static final int DURABILITY_LOSS = 5;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String CONSUME_TEXT = "consume";
    private static final String WAX_SEAL_TEXT = "wax_seal";

    public ShivadaJadeAxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.CRYO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(WAX_SEAL_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, CONSUME_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, CONSUME_CHANCE, false, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !hasFragment(player)) return InteractionResultHolder.pass(stack);
        if (server.getRandom().nextDouble() < ElementWeapons.scaled(player, stack, Element.CRYO, CONSUME_CHANCE, false))
            player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(PGCItems.VAJRADA_AMETHYST_SLIVER.get()), 1, player.inventoryMenu.getCraftSlots());
        var center = player.position();
        for (var target : server.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(RADIUS), candidate -> candidate != player))
            target.hurt(ElementDamage.of(Element.CRYO, server.damageSources().generic()), Mth.nextInt(server.getRandom(), MIN_DAMAGE, MAX_DAMAGE));
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.CRYO, COOLDOWN, false));
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        return InteractionResultHolder.success(stack);
    }

    private static boolean hasFragment(Player player) {
        return player.getInventory().contains(candidate -> candidate.is(PGCItems.VAJRADA_AMETHYST_SLIVER.get()));
    }
}
