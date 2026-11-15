package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
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
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 20, WeaponTier.NETHERITE_INCORRECT,
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

    public ShivadaJadeAxeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.CRYO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(WAX_SEAL_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, CONSUME_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, CONSUME_CHANCE, false, sealed), Formatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld server) || !hasFragment(player)) return TypedActionResult.pass(stack);
        if (server.getRandom().nextDouble() < ElementWeapons.scaled(player, stack, Element.CRYO, CONSUME_CHANCE, false))
            player.getInventory().remove(candidate -> candidate.isOf(PGCItems.VAJRADA_AMETHYST_SLIVER.get()), 1, player.playerScreenHandler.getCraftingInput());
        var center = player.getPos();
        for (var target : server.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(RADIUS), candidate -> candidate != player))
            target.damage(ElementDamage.of(Element.CRYO, server.getDamageSources().generic()), MathHelper.nextInt(server.getRandom(), MIN_DAMAGE, MAX_DAMAGE));
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.CRYO, COOLDOWN, false));
        level.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        server.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        return TypedActionResult.success(stack);
    }

    private static boolean hasFragment(PlayerEntity player) {
        return net.per.primogemcraft.compat.item.Inventories120.any(player.getInventory(), candidate -> candidate.isOf(PGCItems.VAJRADA_AMETHYST_SLIVER.get()));
    }
}
