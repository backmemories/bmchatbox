package net.bmcb.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;

// Este mixin ya no hace nada — el chat vanilla se reemplaza via HudElementRegistry en BMChatBoxClient
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
}