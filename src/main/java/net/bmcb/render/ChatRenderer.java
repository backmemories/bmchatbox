package net.bmcb.render;

import net.bmcb.chat.ChatManager;
import net.bmcb.chat.ChatMessage;
import net.bmcb.chat.FlavorManager;
import net.bmcb.chat.FontManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.util.List;

public class ChatRenderer {

    private static final int BOX_WIDTH        = 300;
    private static final int BOX_HEIGHT       = 90;
    private static final int BOX_MARGIN_RIGHT = 25;
    private static final int BOX_MARGIN_TOP   = 20;
    private static final int PADDING          = 10;
    private static final int LINE_HEIGHT      = 14;
    private static final float TEXT_SCALE     = 1.3f;

    public static final int COLOR_WHITE  = 0xFFFFFFFF;
    public static final int COLOR_BG     = 0xEE000000;
    public static final int COLOR_BORDER = 0xFFFFFFFF;

    private static final int TEX_SIZE = 32;
    private static final int CORNER   = 8;

    private static final long BLINK_INTERVAL_MS = 200;
    private static long lastBlinkTime = 0;
    private static boolean blinkState = false;

    // sabor seleccionado / demo, menta_melina, dia_nublado, fresa francesa, vainilla vanilla, bonito morado
    // //// sobreescrito en cada render por FlavorManager ////
    private static String currentFlavor = "dia_nublado";

    // cajas saborizadas
    private static final Identifier SABOR_MENTA_MELINA =
            Identifier.of("bmchatbox", "textures/gui/sabor_menta.png");
    private static final Identifier SABOR_DIA_NUBLADO =
            Identifier.of("bmchatbox", "textures/gui/sabor_dia_nublado.png");
    private static final Identifier SABOR_FRESA_FRANCESA =
            Identifier.of("bmchatbox", "textures/gui/sabor_fresa_francesa.png");
    private static final Identifier SABOR_VAINILLA_VANILLA =
            Identifier.of("bmchatbox", "textures/gui/sabor_vainilla_vanilla.png");
    private static final Identifier SABOR_BONITO_MORADO =
            Identifier.of("bmchatbox", "textures/gui/sabor_bonito_morado.png");

    // indicadores saborizados
    private static final Identifier IND_MENTA =
            Identifier.of("bmchatbox", "textures/gui/ind_menta.png");
    private static final Identifier IND_FRESA =
            Identifier.of("bmchatbox", "textures/gui/ind_fresa_francesa.png");
    private static final Identifier IND_VAINILLA =
            Identifier.of("bmchatbox", "textures/gui/ind_vainilla.png");
    private static final Identifier IND_NUBLADO =
            Identifier.of("bmchatbox", "textures/gui/ind_dia_nublado.png");
    private static final Identifier IND_MORADO =
            Identifier.of("bmchatbox", "textures/gui/ind_bonito_morado.png");

    //fondo saborizado
    private static int SABOR_BG() {
        switch (currentFlavor) {
            case "menta_melina": return 0xFF393152;
            case "fresa_francesa": return 0xFF42214a;
            case "vainilla_vanilla": return 0xEE003322;
            case "dia_nublado": return 0xFF0f0f0f;
            case "bonito_morado": return 0xFF003322;
            default: return COLOR_BG;
        }
    }

    //letra saborizada
    private static int SABOR_TXT() {
        switch (currentFlavor) {
            case "menta_melina": return 0xFFe8e6b3;
            case "fresa_francesa": return 0xFFe8e6b3;
            case "vainilla_vanilla": return 0xFFe8e6b3;
            case "dia_nublado": return 0xFFf0f0f0;
            case "bonito_morado": return 0xFF003322;
            default: return COLOR_WHITE;
        }
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

        // Nombre del jugador / titulo de la caja
        if (msg.hasSender()) {
            String name = msg.getSenderName();
            int nameX = x + PADDING;
            int nameY = y - (int)(9 * TEXT_SCALE); //altura
            int nameWidth = (int)(textRenderer.getWidth(name) * TEXT_SCALE) + PADDING;

            context.fill(nameX - 2, nameY - 1, nameX + nameWidth, nameY + (int)(9 * TEXT_SCALE) + 1, SABOR_BG());

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(nameX, nameY);
            context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
            context.drawTextWithShadow(textRenderer, Text.literal(name), 0, 0, COLOR_WHITE);
            context.getMatrices().popMatrix();
        }

        // 🎨 aplicar sabor del jugador local (quien está viendo)
        MinecraftClient client2 = MinecraftClient.getInstance();
        if (client2 != null && client2.player != null) {
            currentFlavor = FlavorManager.getFlavor(client2.player.getGameProfile().name());
        }
        // 🧩 CAJA
        drawBox(context, x, y, boxW, boxH);

        // Texto
        String visibleText = msg.getVisibleText();
        if (!visibleText.isEmpty()) {
//desplazamiento del texto horizontal
            final int TEXT_OFFSET_X = 10;
            int wrapWidth = (int)(((BOX_WIDTH - PADDING * 2 - TEXT_OFFSET_X) / (chatScale * TEXT_SCALE)) - 8);

            List<OrderedText> lines = new java.util.ArrayList<>();
            String[] splitLines = visibleText.split("\n");

            net.minecraft.text.StyleSpriteSource fontSource = new net.minecraft.text.StyleSpriteSource.Font(
                    net.minecraft.util.Identifier.of(FontManager.getFont(msg.getSenderName()))
            );
            net.minecraft.text.Style fontStyle = net.minecraft.text.Style.EMPTY.withFont(fontSource);

            for (String part : splitLines) {
                lines.addAll(textRenderer.wrapLines(
                        Text.literal(part).setStyle(fontStyle),
                        wrapWidth
                ));
            }
//desplazamiento del texto en vertical
            final int TEXT_OFFSET_Y = 12; // desplazamiento extra hacia abajo
            int offsetY = y + PADDING + TEXT_OFFSET_Y;

            for (OrderedText line : lines) {
                context.getMatrices().pushMatrix();
                context.getMatrices().translate(x + PADDING + TEXT_OFFSET_X, offsetY);
                context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
                context.drawTextWithShadow(textRenderer, line, 0, 0, SABOR_TXT());
                context.getMatrices().popMatrix();
                offsetY += (int)(LINE_HEIGHT * TEXT_SCALE);
            }
        }

        // Indicador
        if (msg.isTypewriterDone() && ChatManager.hasMore()) {
            Identifier tex = getIndicatorTexture();

// animación continua
            long now = System.currentTimeMillis();
            if (now - lastBlinkTime >= BLINK_INTERVAL_MS) {
                blinkState = !blinkState;
                lastBlinkTime = now;
            }

// frame (0 o 1)
            int frame = blinkState ? 0 : 1;

// tamaño en pantalla
            int size = (int)(10 * TEXT_SCALE);

// posición
            int drawX = x + boxW - PADDING - size - 5;
            int drawY = y + boxH - PADDING - size + 5;

// coordenada V (vertical en la textura)
            int v = frame * 5;

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(drawX, drawY);
            context.getMatrices().scale(size / 5f, size / 5f);
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    tex,
                    0, 0,
                    0, v,
                    5, 5,
                    5, 10
            );

            context.getMatrices().popMatrix();
        }

        context.getMatrices().popMatrix();
    }

    // demo o sabores ricos?
    private static void drawBox(DrawContext ctx, int x, int y, int w, int h) {
        if (currentFlavor.equals("demo")) {
            SABOR_DEMO(ctx, x, y, w, h); //dejé el sabor demo porque soy muy nostalgico con las cosas que funcionan bien u-u
        } else {
            drawNineSlice(ctx, x, y, w, h, getTexture());
        }
    }
    // sabor demo / por el recuerdo
    private static void SABOR_DEMO(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, COLOR_BG);

        ctx.fill(x, y, x + w, y + 1, COLOR_BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, COLOR_BORDER);
        ctx.fill(x, y, x + 1, y + h, COLOR_BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, COLOR_BORDER);
    }

    //lista de sabores
    private static Identifier getTexture() {
        switch (currentFlavor) {
            case "menta_melina": return SABOR_MENTA_MELINA;
            case "dia_nublado": return SABOR_DIA_NUBLADO;
            case "fresa_francesa": return SABOR_FRESA_FRANCESA;
            case "vainilla_vanilla": return SABOR_VAINILLA_VANILLA;
            case "bonito_morado": return SABOR_BONITO_MORADO;
            default: return SABOR_MENTA_MELINA;
        }
    }
    //lista de indicadores
    private static Identifier getIndicatorTexture() {
        switch (currentFlavor) {
            case "demo": return IND_NUBLADO; //en realidad la demo no tiene indicador pero bueno
            case "menta_melina": return IND_MENTA;
            case "fresa_francesa": return IND_FRESA;
            case "vainilla_vanilla": return IND_VAINILLA;
            case "dia_nublado": return IND_NUBLADO;
            case "bonito_morado": return IND_MORADO;
            default: return IND_MENTA;
        }
    }

    // 9slice para los sabores
    private static void drawNineSlice(DrawContext ctx, int x, int y, int w, int h, Identifier texture) {
        final int PS = 2; // pixel scale
        int cs = CORNER;
        int ts = TEX_SIZE;
        int scs = cs * PS; // esquina en pantalla = 8*2 = 16px

        // Esquinas
        drawPartScaled(ctx, texture, x,          y,          0,       0,       scs, scs, cs, cs);
        drawPartScaled(ctx, texture, x + w - scs, y,         ts - cs, 0,       scs, scs, cs, cs);
        drawPartScaled(ctx, texture, x,          y + h - scs, 0,      ts - cs, scs, scs, cs, cs);
        drawPartScaled(ctx, texture, x + w - scs, y + h - scs, ts-cs, ts - cs, scs, scs, cs, cs);

        // Bordes horizontales
        for (int i = x + scs; i < x + w - scs; i += scs) {
            int segW = Math.min(scs, (x + w - scs) - i);
            drawPartScaled(ctx, texture, i, y,           cs, 0,       segW, scs, segW/PS, cs);
            drawPartScaled(ctx, texture, i, y + h - scs, cs, ts - cs, segW, scs, segW/PS, cs);
        }

        // Bordes verticales
        for (int j = y + scs; j < y + h - scs; j += scs) {
            int segH = Math.min(scs, (y + h - scs) - j);
            drawPartScaled(ctx, texture, x,          j, 0,       cs, scs, segH, cs, segH/PS);
            drawPartScaled(ctx, texture, x + w - scs, j, ts - cs, cs, scs, segH, cs, segH/PS);
        }

        // Centro
        for (int i = x + scs; i < x + w - scs; i += scs) {
            for (int j = y + scs; j < y + h - scs; j += scs) {
                int segW = Math.min(scs, (x + w - scs) - i);
                int segH = Math.min(scs, (y + h - scs) - j);
                drawPartScaled(ctx, texture, i, j, cs, cs, segW, segH, segW/PS, segH/PS);
            }
        }
    }

    // x,y,u,v = posición y coord textura
// dw,dh = tamaño en pantalla (escalado)
// sw,sh = región de textura a leer
    private static void drawPartScaled(DrawContext ctx, Identifier texture,
                                       int x, int y, int u, int v,
                                       int dw, int dh, int sw, int sh) {
        ctx.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x, y,
                u, v,
                dw, dh,
                sw, sh,  // región de textura fuente
                TEX_SIZE, TEX_SIZE
        );
    }
}