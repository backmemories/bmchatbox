package net.bmcb.mixin;

import net.bmcb.screen.BMChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class ChatScreenMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void interceptChatScreen(Screen screen, CallbackInfo ci) {

        if (screen instanceof ChatScreen) {

            MinecraftClient client = MinecraftClient.getInstance();

            // Detectar si se presionó "/" directamente
            long window = client.getWindow().getHandle();
            boolean slashPressed = org.lwjgl.glfw.GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Y) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

            // Si está presionando "/", dejar chat vanilla
            if (slashPressed) {
                return;
            }

            // Si no, usar su chat
            client.execute(() -> client.setScreen(new BMChatScreen()));
            ci.cancel();
        }
    }
}