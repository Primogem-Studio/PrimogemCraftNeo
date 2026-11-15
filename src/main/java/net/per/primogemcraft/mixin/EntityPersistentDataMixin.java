package net.per.primogemcraft.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.per.primogemcraft.compat.attachment.PersistentDataHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityPersistentDataMixin implements PersistentDataHolder {

    @Unique
    private NbtCompound primogemcraft$persistentData;

    @Override
    @Unique
    public NbtCompound primogemcraft$getPersistentData() {
        if (primogemcraft$persistentData == null) {
            primogemcraft$persistentData = new NbtCompound();
        }
        return primogemcraft$persistentData;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void primogemcraft$writePersistent(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (primogemcraft$persistentData != null && !primogemcraft$persistentData.isEmpty()) {
            nbt.put("primogemcraft_persistent", primogemcraft$persistentData.copy());
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void primogemcraft$readPersistent(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("primogemcraft_persistent", NbtCompound.COMPOUND_TYPE)) {
            primogemcraft$persistentData = nbt.getCompound("primogemcraft_persistent").copy();
        }
    }
}