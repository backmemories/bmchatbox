package net.bmcb.render;

import net.bmcb.chat.ChatManager;
import net.bmcb.chat.ChatMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;

import java.util.List;

public class ChatRenderer {

    private static final int BOX_WIDTH        = 280;
    private static final int BOX_HEIGHT       = 80;
    private static final int BOX_MARGIN_RIGHT = 20;
    private static final int BOX_MARGIN_TOP   = 20;
    private static final int PADDING          = 10;
    private static final int LINE_HEIGHT      = 14;
    private static final float TEXT_SCALE     = 1.5f;

    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFFFF00;
    private static final int COLOR_BG     = 0xEE000000;
    private static final int COLOR_BORDER = 0xFFFFFFFF;

    // Parpadeo del indicador ▼/●
    private static final long BLINK_INTERVAL_MS = 345; // cada 345ms cambia
    private static long lastBlinkTime = 0;
    private static boolean blinkState = false; // false=▼, true=●

    public static void render(DrawContext context) {
        if (!ChatManager.hasMessage()) return;

        ChatMessage msg = ChatManager.getCurrentMessage();
        if (msg == null) return;

        msg.update();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;

        TextRenderer textRenderer = client.textRenderer;

        float chatScale = ((Double) client.options.getChatScale().getValue()).floatValue();
        if (chatScale <= 0) chatScale = 1.0f;

        int screenWidth  = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(chatScale, chatScale);
        context.getMatrices().translate(4.0F, 0.0F);

        int scaledW = (int)(screenWidth / chatScale);
        int boxW = (int)(BOX_WIDTH / chatScale);
        int boxH = (int)(BOX_HEIGHT / chatScale);

        int x = scaledW - boxW - (int)(BOX_MARGIN_RIGHT / chatScale);
        int y = (int)(BOX_MARGIN_TOP / chatScale);

        // Nombre del jugador como título sobre el borde superior
        if (msg.hasSender()) {
            String name = msg.getSenderName();
            int nameX = x + PADDING;
            int nameY = y - (int)(10 * TEXT_SCALE);
            int nameWidth = (int)(textRenderer.getWidth(name) * TEXT_SCALE) + PADDING;

            context.fill(nameX - 2, nameY - 1, nameX + nameWidth, nameY + (int)(9 * TEXT_SCALE) + 1, COLOR_BG);

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(nameX, nameY);
            context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
            context.drawTextWithShadow(textRenderer, Text.literal(name), 0, 0, COLOR_YELLOW);
            context.getMatrices().popMatrix();
        }

        // Caja principal
        context.fill(x, y, x + boxW, y + boxH, COLOR_BG);

        // Borde blanco
        context.fill(x,          y,          x + boxW,     y + 1,        COLOR_BORDER);
        context.fill(x,          y + boxH-1, x + boxW,     y + boxH,     COLOR_BORDER);
        context.fill(x,          y,          x + 1,        y + boxH,     COLOR_BORDER);
        context.fill(x + boxW-1, y,          x + boxW,     y + boxH,     COLOR_BORDER);

        // Texto del mensaje
        String visibleText = msg.getVisibleText();
        if (!visibleText.isEmpty()) {
            int wrapWidth = (int)((BOX_WIDTH - PADDING * 2) / (chatScale * TEXT_SCALE));
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(visibleText), wrapWidth);

            int offsetY = y + PADDING;
            for (OrderedText line : lines) {
                context.getMatrices().pushMatrix();
                context.getMatrices().translate(x + PADDING, offsetY);
                context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
                context.drawTextWithShadow(textRenderer, line, 0, 0, COLOR_WHITE);
                context.getMatrices().popMatrix();
                offsetY += (int)(LINE_HEIGHT * TEXT_SCALE);
            }
        }

        // Indicador al terminar el typewriter
        if (msg.isTypewriterDone()) {
            String indicator;

            if (ChatManager.hasMore()) {
                // Parpadeo entre ▼ y ● cuando hay más mensajes
                long now = System.currentTimeMillis();
                if (now - lastBlinkTime >= BLINK_INTERVAL_MS) {
                    blinkState = !blinkState;
                    lastBlinkTime = now;
                }
                indicator = blinkState ? "●" : "▼";
            } else {
                // Sin más mensajes: ■ estático
                indicator = "■";
            }

            int indW = (int)(textRenderer.getWidth(indicator) * TEXT_SCALE);
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(
                    x + boxW - PADDING - indW,
                    y + boxH - PADDING - (int)(LINE_HEIGHT * TEXT_SCALE));
            context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
            context.drawTextWithShadow(textRenderer, Text.literal(indicator), 0, 0, COLOR_WHITE);
            context.getMatrices().popMatrix();
        }

        context.getMatrices().popMatrix();
    }
}