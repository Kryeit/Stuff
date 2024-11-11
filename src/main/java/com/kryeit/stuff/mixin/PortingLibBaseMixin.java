package com.kryeit.stuff.mixin;

import io.github.fabricators_of_create.porting_lib.PortingLibBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PortingLibBase.class, remap = false)
public class PortingLibBaseMixin {
    @Inject(method = "onInitialize", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/command/v2/ArgumentTypeRegistry;registerArgumentType(Lnet/minecraft/util/Identifier;Ljava/lang/Class;Lnet/minecraft/command/argument/serialize/ArgumentSerializer;)V", remap = true), cancellable = true)
    public void unregisterPortingLibArgumentTypes(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onInitialize", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V"), cancellable = true)
    public void unregisterPortingLibConfigCommand(CallbackInfo ci) {
        ci.cancel();
    }
}
