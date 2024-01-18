package com.kryeit.stuff.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

//@Mixin(ChatScreen.class)
//public class ChatScreenMixin {
//    @Inject(method = "render", at = @At("HEAD"))
//    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//        ChatScreen chatScreen = (ChatScreen)(Object)this;
//
//        List<Text> customText = new ArrayList<>();
//        customText.add(new LiteralText("Test").formatted(Formatting.AQUA));
//        chatScreen.setTooltip()
//    }
//}
