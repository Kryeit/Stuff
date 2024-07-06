package com.kryeit.stuff.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// No-NaN-Mod ported to Fabric 1.20.1
@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"), index = 0)
    public double modifyPosX(double value) {
        return Double.isFinite(value) ? value : 0;
    }

    @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"), index = 1)
    public double modifyPosY(double value) {
        return Double.isFinite(value) ? value : 0;
    }

    @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"), index = 2)
    public double modifyPosZ(double value) {
        return Double.isFinite(value) ? value : 0;
    }
}
