package net.bmcb;

import net.bmcb.chat.ChatManager;
import net.bmcb.render.ChatRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BMChatBoxClient implements ClientModInitializer {

    private static KeyBinding advanceKey;

    @Override
    public void onInitializeClient() {
        BMChatBox.LOGGER.info("BMChatBox CLIENT iniciado");

        // Registrar tecla de avance (Z, estilo Earthbound)
        advanceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bmchatbox.advance",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                Category.MISC
        ));

        // Reemplazar el chat vanilla con el nuestro.
        // En tu versión de Fabric API, replaceElement recibe (Identifier, Function<HudElement, HudElement>).
        // Ignoramos el elemento vanilla (_ignored) y devolvemos nuestro propio HudElement.
        HudElementRegistry.replaceElement(
                VanillaHudElements.CHAT,
                _ignored -> (context, tickCounter) -> ChatRenderer.render(context)
        );

        // Capturar mensajes del chat de jugadores
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> {
            ChatManager.addMessage(message.getString());
        });

        // Capturar mensajes del sistema (comandos, muerte, etc.)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                ChatManager.addMessage(message.getString());
            }
        });

        // Detectar pulsación de la tecla de avance
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (advanceKey.wasPressed()) {
                ChatManager.advance();
            }
        });
    }
}