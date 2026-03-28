package net.bmcb;

import net.bmcb.chat.ChatManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BMChatBoxClient implements ClientModInitializer {

    private static KeyBinding advanceKey;

    @Override
    public void onInitializeClient() {
        BMChatBox.LOGGER.info("BMChatBox CLIENT iniciado");

        advanceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bmchatbox.advance",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                Category.MISC
        ));

        // El render ahora lo hace el mixin directamente — no necesitamos HudRenderCallback
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> {
            ChatManager.addMessage(message.getString());
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                ChatManager.addMessage(message.getString());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (advanceKey.wasPressed()) {
                ChatManager.advance();
            }
        });
    }
}