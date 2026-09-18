package net.per.primogemcraft.item.curio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.util.TemporaryAttributes;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ComicBookItem extends CurioItem {
    private static final ResourceLocation SCALE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "comic_book_scale");
    private static final ResourceLocation BLOCK_REACH_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "comic_book_block_reach");
    private static final ResourceLocation ENTITY_REACH_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "comic_book_entity_reach");
    private static final int COOLDOWN_TICKS = 6000;
    private static final int DURATION_TICKS = 3600;
    private static final double RADIUS = 20.0D;
    private static final int MAX_SCALE_LEVEL = 9;
    private static final int MAX_EFFECT_LEVEL = 5;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 0.5F;

    public ComicBookItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 10, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var item = context.stack().getItem();
        if (player.getCooldowns().isOnCooldown(item)) return;
        player.getCooldowns().addCooldown(item, COOLDOWN_TICKS);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, VOLUME, PITCH);
        for (var target : nearby(player)) {
            var scale = Mth.nextInt(context.random(), 0, MAX_SCALE_LEVEL) + 1;
            var amount = context.chance(0.5D) ? scale : -scale;
            TemporaryAttributes.apply(target, SCALE_ID, Attributes.SCALE, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, DURATION_TICKS);
            TemporaryAttributes.apply(target, BLOCK_REACH_ID, Attributes.BLOCK_INTERACTION_RANGE, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, DURATION_TICKS);
            TemporaryAttributes.apply(target, ENTITY_REACH_ID, Attributes.ENTITY_INTERACTION_RANGE, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, DURATION_TICKS);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, Mth.nextInt(context.random(), 0, MAX_EFFECT_LEVEL), false, false));
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_TICKS, Mth.nextInt(context.random(), 0, MAX_EFFECT_LEVEL), false, false));
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof ServerPlayer player)) return false;
        for (var target : nearby(player)) {
            TemporaryAttributes.remove(target, SCALE_ID);
            TemporaryAttributes.remove(target, BLOCK_REACH_ID);
            TemporaryAttributes.remove(target, ENTITY_REACH_ID);
        }
        CurioContext.of(player, stack, form()).damage(1);
        return false;
    }

    private static List<LivingEntity> nearby(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS));
    }
}
