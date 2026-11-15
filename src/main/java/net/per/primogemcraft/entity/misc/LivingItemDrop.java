package net.per.primogemcraft.entity.misc;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class LivingItemDrop extends ItemEntity {
    private final UUID ownerUuid;

    public LivingItemDrop(World world, double x, double y, double z, ItemStack stack, @Nullable UUID owner) {
        super(world, x, y, z, stack);
        ownerUuid = owner;
        setInvulnerable(true);
        setNeverDespawn();
        setPickupDelay(0);
        setNoGravity(false);
        if (owner != null) setOwner(owner);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (ownerUuid != null && !ownerUuid.equals(player.getUuid())) return;
        super.onPlayerCollision(player);
    }

    @Override
    public void tick() {
        // 不检查掉出世界
        super.tick();
    }
}
