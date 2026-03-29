package net.bmcb.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BMChatScreen extends Screen {

    private static final int BOX_WIDTH        = 280;
    private static final int BOX_HEIGHT       = 80;
    private static final int BOX_MARGIN_RIGHT = 20;
    private static final int BOX_MARGIN_BOT   = 20;
    private static final int PADDING          = 10;
    private static final int LINE_HEIGHT      = 14;
    private static final float TEXT_SCALE     = 1.5f;
    private static final int MAX_CHARS        = 95;

    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_BG     = 0xEE000000;
    private static final int COLOR_BORDER = 0xFFFFFFFF;

    private long lastCursorBlink = 0;
    private boolean cursorVisible = true;
    private static final long CURSOR_BLINK_MS = 500;

    private TextFieldWidget textField;

    public BMChatScreen() {
        super(Text.literal("BMChatBox Input"));
    }

    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        this.textField = new TextFieldWidget(
                this.textRenderer,
                0, 0, // posición irrelevante (no lo usamos visualmente)
                10, 10,
                Text.literal("")
        );

        this.textField.setMaxLength(MAX_CHARS);
        this.textField.setFocused(true);

        this.addDrawableChild(this.textField);
    }

    public boolean charTyped(net.minecraft.client.input.CharInput input) {
        return this.textField.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {

        if (this.textField.keyPressed(input)) {
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_ENTER) {
            sendMessage(this.textField.getText());
            this.textField.setText(""); // limpiar
            this.client.setScreen(null); // cerrar
            return true;
        }

        return super.keyPressed(input);
    }

    private void sendMessage(String message) {
        if (client != null && client.player != null && !message.isEmpty()) {
            client.player.networkHandler.sendChatMessage(message);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = this.client.textRenderer;

        float chatScale = ((Double) this.client.options.getChatScale().getValue()).floatValue();
        if (chatScale <= 0) chatScale = 1.0f;

        int screenWidth  = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(chatScale, chatScale);
        matrices.translate(4.0F, 0.0F);

        int scaledW = (int)(screenWidth / chatScale);
        int scaledH = (int)(screenHeight / chatScale);
        int boxW    = (int)(BOX_WIDTH / chatScale);
        int boxH    = (int)(BOX_HEIGHT / chatScale);

        int x = scaledW - boxW - (int)(BOX_MARGIN_RIGHT / chatScale);
        int y = scaledH - boxH - (int)(BOX_MARGIN_BOT / chatScale);

        // Caja
        context.fill(x, y, x + boxW, y + boxH, COLOR_BG);

        // Borde
        context.fill(x,          y,          x + boxW,     y + 1,        COLOR_BORDER);
        context.fill(x,          y + boxH-1, x + boxW,     y + boxH,     COLOR_BORDER);
        context.fill(x,          y,          x + 1,        y + boxH,     COLOR_BORDER);
        context.fill(x + boxW-1, y,          x + boxW,     y + boxH,     COLOR_BORDER);

        // Cursor
        long now = System.currentTimeMillis();
        if (now - lastCursorBlink >= CURSOR_BLINK_MS) {
            cursorVisible = !cursorVisible;
            lastCursorBlink = now;
        }

        String displayText = textField.getText() + (cursorVisible ? "|" : " ");

        int wrapWidth = (int)((BOX_WIDTH - PADDING * 2) / (chatScale * TEXT_SCALE));
        List<OrderedText> lines = textRenderer.wrapLines(Text.literal(displayText), wrapWidth);

        int maxLines = 3;
        int startLine = Math.max(0, lines.size() - maxLines);

        int offsetY = y + PADDING;

        for (int i = startLine; i < lines.size(); i++) {
            matrices.pushMatrix();
            matrices.translate(x + PADDING, offsetY);
            matrices.scale(TEXT_SCALE, TEXT_SCALE);

            context.drawTextWithShadow(textRenderer, lines.get(i), 0, 0, COLOR_WHITE);

            matrices.popMatrix();
            offsetY += (int)(LINE_HEIGHT * TEXT_SCALE);
        }

        matrices.popMatrix();
    }
}