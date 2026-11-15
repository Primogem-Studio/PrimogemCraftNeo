package net.per.primogemcraft.item.curio;

import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ComicBookItem extends CurioItem {
    private static final int COOLDOWN_TICKS = 6000;
    private static final int DURATION_TICKS = 3600;
    private static final double RADIUS = 20.0D;
    private static final int MAX_SCALE_LEVEL = 9;
    private static final int MAX_EFFECT_LEVEL = 5;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 0.5F;

    public ComicBookItem(Settings settings) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 10, settings);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var item = context.stack().getItem();
        if (player.getItemCooldownManager().isCoolingDown(item)) return;
        player.getItemCooldownManager().set(item, COOLDOWN_TICKS);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, VOLUME, PITCH);
        for (var target : nearby(player)) {
            var scale = MathHelper.nextInt(context.random(), 0, MAX_SCALE_LEVEL) + 1;
            // 1.20.1 没有 SCALE/BLOCK_REACH/ENTITY_REACH 属性，体型缩放与触及距离无法表达，予以移除；增益效果保留。
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DURATION_TICKS, MathHelper.nextInt(context.random(), 0, MAX_EFFECT_LEVEL), false, false));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, DURATION_TICKS, MathHelper.nextInt(context.random(), 0, MAX_EFFECT_LEVEL), false, false));
        }
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) return false;
        CurioContext.of(player, stack, form()).damage(1);
        return false;
    }

    private static List<LivingEntity> nearby(ServerPlayerEntity player) {
        return player.getServerWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(RADIUS), e -> true);
    }
}
