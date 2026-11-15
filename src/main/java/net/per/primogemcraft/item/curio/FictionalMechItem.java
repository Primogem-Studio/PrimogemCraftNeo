package net.per.primogemcraft.item.curio;

import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class FictionalMechItem extends CurioItem {
    private static final double HEAL_RATIO = 0.1D;
    private static final double HEALTH_PER_TICK = 20.0D;
    private static final float VOLUME = 0.5F;
    private static final float PITCH = 20.0F;

    public FictionalMechItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (victim.getHealth() < victim.getMaxHealth()) return;
        var player = context.player();
        var item = context.stack().getItem();
        if (player.getItemCooldownManager().isCoolingDown(item)) return;
        var health = player.getHealth();
        player.getItemCooldownManager().set(item, (int) Math.ceil(health / HEALTH_PER_TICK));
        player.setHealth((float) (health + player.getMaxHealth() * HEAL_RATIO));
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, VOLUME, PITCH);
    }
}
