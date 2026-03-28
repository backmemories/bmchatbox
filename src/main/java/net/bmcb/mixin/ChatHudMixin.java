package net.bmcb.mixin;

import net.bmcb.render.ChatRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class ChatHudMixin {

    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaChat(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Replicamos exactamente lo que hace renderChat antes de dibujar
        context.createNewRootLayer();
        ChatRenderer.render(context);
        ci.cancel();
    }
}