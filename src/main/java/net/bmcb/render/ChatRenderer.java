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

    private static final int BOX_WIDTH    = 300;
    private static final int BOX_HEIGHT   = 70;
    private static final int PADDING      = 10;
    private static final int LINE_HEIGHT  = 12;

    // Colores con alpha correcto (0xFF en el byte más significativo)
    private static final int COLOR_WHITE       = 0xFFFFFFFF; // texto blanco opaco
    private static final int COLOR_BG          = 0xEE000000; // fondo negro semitransparente
    private static final int COLOR_BORDER      = 0xFFFFFFFF; // borde blanco

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

        int scaledHeight = context.getScaledWindowHeight();

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(chatScale, chatScale);
        context.getMatrices().translate(4.0F, 0.0F);

        int m = (int) Math.floor((scaledHeight - 40) / chatScale);
        int boxW = (int)(BOX_WIDTH / chatScale);
        int boxH = (int)(BOX_HEIGHT / chatScale);
        int x = 0;
        int y = m - boxH;

        // Fondo negro (fill usa int normal, el alpha ya está en 0xEE)
        context.fill(x, y, x + boxW, y + boxH, COLOR_BG);

        // Borde blanco — fill usa el int completo, 0xFFFFFFFF está bien como int con signo
        context.fill(x,          y,          x + boxW,     y + 1,        COLOR_BORDER);
        context.fill(x,          y + boxH-1, x + boxW,     y + boxH,     COLOR_BORDER);
        context.fill(x,          y,          x + 1,        y + boxH,     COLOR_BORDER);
        context.fill(x + boxW-1, y,          x + boxW,     y + boxH,     COLOR_BORDER);

        // Texto — color con alpha 0xFF para pasar la guarda de DrawContext
        String visibleText = msg.getVisibleText();
        if (!visibleText.isEmpty()) {
            int wrapWidth = (int)((BOX_WIDTH - PADDING * 2) / chatScale);
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(visibleText), wrapWidth);

            int offsetY = y + PADDING;
            for (OrderedText line : lines) {
                context.drawTextWithShadow(textRenderer, line, x + PADDING, offsetY, COLOR_WHITE);
                offsetY += LINE_HEIGHT;
            }
        }

        if (msg.isTypewriterDone()) {
            String indicator = ChatManager.getQueueSize() > 0 ? "▼" : "■";
            context.drawTextWithShadow(textRenderer, Text.literal(indicator),
                    x + boxW - PADDING - textRenderer.getWidth(indicator),
                    y + boxH - PADDING - LINE_HEIGHT,
                    COLOR_WHITE);
        }

        context.getMatrices().popMatrix();
    }
}