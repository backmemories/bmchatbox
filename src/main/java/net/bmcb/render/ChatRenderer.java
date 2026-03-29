package net.bmcb.render;

import net.bmcb.chat.ChatManager;
import net.bmcb.chat.ChatMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.util.List;

public class ChatRenderer {

    private static final int BOX_WIDTH        = 280;
    private static final int BOX_HEIGHT       = 80;
    private static final int BOX_MARGIN_RIGHT = 20;
    private static final int BOX_MARGIN_TOP   = 20;
    private static final int PADDING          = 10;
    private static final int LINE_HEIGHT      = 14;
    private static final float TEXT_SCALE     = 1.3f;

    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFFFF00;
    private static final int COLOR_BG     = 0xEE000000;
    private static final int COLOR_BORDER = 0xFFFFFFFF;

    // 🌿 SABORES
    private static String currentFlavor = "dia_nublado";

    // 🌿 TEXTURAS (todas 32x32)
    private static final Identifier TEX_MENTA =
            Identifier.of("bmchatbox", "textures/gui/sabor_menta.png");

    private static final Identifier TEX_DIA_NUBLADO =
            Identifier.of("bmchatbox", "textures/gui/sabor_dia_nublado.png");

    private static final Identifier TEX_FRESA =
            Identifier.of("bmchatbox", "textures/gui/fresa_francesa.png");

    private static final Identifier TEX_VAINILLA =
            Identifier.of("bmchatbox", "textures/gui/vainilla_vanilla.png");

    private static final Identifier TEX_MORADO =
            Identifier.of("bmchatbox", "textures/gui/bonito_morado.png");

    private static final int TEX_SIZE = 32;
    private static final int CORNER   = 8;

    private static final long BLINK_INTERVAL_MS = 345;
    private static long lastBlinkTime = 0;
    private static boolean blinkState = false;

    public static void setFlavor(String flavor) {
        currentFlavor = flavor;
    }

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

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(chatScale, chatScale);
        context.getMatrices().translate(4.0F, 0.0F);

        int scaledW = (int)(screenWidth / chatScale);
        int boxW = (int)(BOX_WIDTH / chatScale);
        int boxH = (int)(BOX_HEIGHT / chatScale);

        int x = scaledW - boxW - (int)(BOX_MARGIN_RIGHT / chatScale);
        int y = (int)(BOX_MARGIN_TOP / chatScale);

        // Nombre
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

        // 🧩 CAJA
        drawBox(context, x, y, boxW, boxH);

        // Texto
        String visibleText = msg.getVisibleText();
        if (!visibleText.isEmpty()) {
            int wrapWidth = (int)((BOX_WIDTH - PADDING * 2) / (chatScale * TEXT_SCALE));
            List<OrderedText> lines = new java.util.ArrayList<>();

            String[] splitLines = visibleText.split("\n");

            for (String part : splitLines) {
                lines.addAll(textRenderer.wrapLines(Text.literal(part), wrapWidth));
            }

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

        // Indicador
        if (msg.isTypewriterDone()) {
            String indicator;

            if (ChatManager.hasMore()) {
                long now = System.currentTimeMillis();
                if (now - lastBlinkTime >= BLINK_INTERVAL_MS) {
                    blinkState = !blinkState;
                    lastBlinkTime = now;
                }
                indicator = blinkState ? "●" : "▼";
            } else {
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

    // 🎨 Selector
    private static void drawBox(DrawContext ctx, int x, int y, int w, int h) {
        if (currentFlavor.equals("demo")) {
            drawDemo(ctx, x, y, w, h);
        } else {
            drawNineSlice(ctx, x, y, w, h, getTexture());
        }
    }

    private static Identifier getTexture() {
        switch (currentFlavor) {
            case "menta": return TEX_MENTA;
            case "dia_nublado": return TEX_DIA_NUBLADO;
            case "fresa_francesa": return TEX_FRESA;
            case "vainilla_vanilla": return TEX_VAINILLA;
            case "bonito_morado": return TEX_MORADO;
            default: return TEX_MENTA;
        }
    }

    // 🔲 DEMO
    private static void drawDemo(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, COLOR_BG);

        ctx.fill(x, y, x + w, y + 1, COLOR_BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, COLOR_BORDER);
        ctx.fill(x, y, x + 1, y + h, COLOR_BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, COLOR_BORDER);
    }

    // 🌿 9-SLICE GENÉRICO
    private static void drawNineSlice(DrawContext ctx, int x, int y, int w, int h, Identifier texture) {
        int cs = CORNER;
        int ts = TEX_SIZE;

        // Esquinas
        drawPart(ctx, texture, x, y, 0, 0, cs, cs);
        drawPart(ctx, texture, x + w - cs, y, ts - cs, 0, cs, cs);
        drawPart(ctx, texture, x, y + h - cs, 0, ts - cs, cs, cs);
        drawPart(ctx, texture, x + w - cs, y + h - cs, ts - cs, ts - cs, cs, cs);

        // Bordes
        for (int i = x + cs; i < x + w - cs; i += cs) {
            int segW = Math.min(cs, (x + w - cs) - i);
            drawPart(ctx, texture, i, y, cs, 0, segW, cs);
            drawPart(ctx, texture, i, y + h - cs, cs, ts - cs, segW, cs);
        }

        for (int j = y + cs; j < y + h - cs; j += cs) {
            int segH = Math.min(cs, (y + h - cs) - j);
            drawPart(ctx, texture, x, j, 0, cs, cs, segH);
            drawPart(ctx, texture, x + w - cs, j, ts - cs, cs, cs, segH);
        }

        // Centro
        for (int i = x + cs; i < x + w - cs; i += cs) {
            for (int j = y + cs; j < y + h - cs; j += cs) {
                int segW = Math.min(cs, (x + w - cs) - i);
                int segH = Math.min(cs, (y + h - cs) - j);
                drawPart(ctx, texture, i, j, cs, cs, segW, segH);
            }
        }
    }

    private static void drawPart(DrawContext ctx, Identifier texture, int x, int y, int u, int v, int w, int h) {
        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x, y,
                u, v,
                w, h,
                TEX_SIZE, TEX_SIZE
        );
    }
}