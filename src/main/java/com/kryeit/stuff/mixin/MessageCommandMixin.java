package com.kryeit.stuff.mixin;

import com.kryeit.stuff.command.Reply;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    private static void onExecute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message, CallbackInfo ci) {
        ServerPlayerEntity sender = source.getPlayer();
        if (sender != null) {
            for (ServerPlayerEntity target : targets)
                Reply.onMessageReceived(sender.getUuid(), target.getUuid());
        }
    }
}
