package net.bmcb.render;

import net.bmcb.chat.ChatManager;
import net.bmcb.chat.ChatMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;

import java.util.List;

public class ChatRenderer {

    private static final int BOX_WIDTH = 300;
    private static final int BOX_HEIGHT = 70;
    private static final int BOX_X = 20;
    private static final int BOX_MARGIN_BOTTOM = 30;
    private static final int PADDING = 10;
    private static final int LINE_HEIGHT = 12;

    public static void render(DrawContext context) {
        if (!ChatManager.hasMessage()) return;

        ChatMessage msg = ChatManager.getCurrentMessage();
        if (msg == null) return;

        // Actualizar efecto typewriter
        msg.update();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;

        TextRenderer textRenderer = client.textRenderer;

        int screenHeight = context.getScaledWindowHeight();
        int x = BOX_X;
        int y = screenHeight - BOX_HEIGHT - BOX_MARGIN_BOTTOM;

        // Fondo negro semitransparente
        context.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, 0xEE000000);

        // Borde blanco simple (1px)
        context.fill(x,                 y,                  x + BOX_WIDTH,     y + 1,              0xFFFFFFFF); // top
        context.fill(x,                 y + BOX_HEIGHT - 1, x + BOX_WIDTH,     y + BOX_HEIGHT,     0xFFFFFFFF); // bottom
        context.fill(x,                 y,                  x + 1,             y + BOX_HEIGHT,     0xFFFFFFFF); // left
        context.fill(x + BOX_WIDTH - 1, y,                  x + BOX_WIDTH,     y + BOX_HEIGHT,     0xFFFFFFFF); // right

        // Texto visible con wrap
        String visibleText = msg.getVisibleText();
        if (!visibleText.isEmpty()) {
            List<OrderedText> lines = textRenderer.wrapLines(
                    Text.literal(visibleText),
                    BOX_WIDTH - (PADDING * 2)
            );

            int offsetY = PADDING;
            for (OrderedText line : lines) {
                context.drawTextWithShadow(
                        textRenderer,
                        line,
                        x + PADDING,
                        y + offsetY,
                        0xFFFFFF
                );
                offsetY += LINE_HEIGHT;
            }
        }

        // Indicador "▼" cuando el typewriter terminó y hay más mensajes o se puede cerrar
        if (msg.isTypewriterDone()) {
            String indicator = ChatManager.getQueueSize() > 0 ? "▼" : "■";
            context.drawTextWithShadow(
                    textRenderer,
                    Text.literal(indicator),
                    x + BOX_WIDTH - PADDING - textRenderer.getWidth(indicator),
                    y + BOX_HEIGHT - PADDING - LINE_HEIGHT,
                    0xFFFFFF
            );
        }
    }
}