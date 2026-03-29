package net.bmcb.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {

    private static final int MAX_CHARS = 90;
    private static final int MAX_LINES = 3;

    private final String senderName;
    private final String fullText;
    private int visibleCharacters = 0;
    private long lastUpdateTime = 0;
    private boolean fullyRevealed = false;
    private ChatMessage overflow = null;

    public ChatMessage(String rawText) {
        String parsedName;
        String parsedMessage;

        if (rawText.startsWith("<") && rawText.contains("> ")) {
            int closeAngle = rawText.indexOf("> ");
            parsedName = rawText.substring(1, closeAngle);
            parsedMessage = rawText.substring(closeAngle + 2);
        } else if (rawText.startsWith("[") && rawText.contains("] ")) {
            int closeBracket = rawText.indexOf("] ");
            parsedName = rawText.substring(1, closeBracket);
            parsedMessage = rawText.substring(closeBracket + 2);
        } else {
            parsedName = "";
            parsedMessage = rawText;
        }

        this.senderName = parsedName;

        // 🔥 aplicar doble espacio → salto
        parsedMessage = parsedMessage.replaceAll(" {2,}", "\n");

        // 🔥 cortar por líneas reales
        SplitResult result = splitByLines(parsedMessage);

        this.fullText = result.current.trim();

        if (!result.rest.isEmpty()) {
            this.overflow = new ChatMessage(result.rest, parsedName);
        }
    }

    // Constructor interno para overflow
    private ChatMessage(String text, String senderName) {
        this.senderName = senderName;

        text = text.replaceAll(" {2,}", "\n");

        SplitResult result = splitByLines(text);

        this.fullText = result.current.trim();

        if (!result.rest.isEmpty()) {
            this.overflow = new ChatMessage(result.rest, senderName);
        }
    }

    // 🔥 NUEVO: divide el texto según lo que entra en la caja
    private SplitResult splitByLines(String text) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return splitByChars(text);
        }

        TextRenderer textRenderer = client.textRenderer;

        float chatScale = ((Double) client.options.getChatScale().getValue()).floatValue();
        if (chatScale <= 0) chatScale = 1.0f;

        final int BOX_WIDTH = 280;
        final int PADDING = 10;
        final float TEXT_SCALE = 1.5f;

        int wrapWidth = (int)((BOX_WIDTH - PADDING * 2) / (chatScale * TEXT_SCALE));

        // 🔥 convertir a líneas respetando \n
        List<String> logicalLines = new ArrayList<>();
        for (String part : text.split("\n", -1)) {
            List<OrderedText> wrapped = textRenderer.wrapLines(Text.literal(part), wrapWidth);

            // cada línea wrapeada cuenta como una línea visual
            for (int i = 0; i < wrapped.size(); i++) {
                logicalLines.add(part);
            }
        }

        if (logicalLines.size() <= MAX_LINES) {
            return new SplitResult(text, "");
        }

        // 🔥 encontrar el índice exacto donde cortar
        int lineCount = 0;
        int cutIndex = 0;

        outer:
        for (int i = 0; i < text.length(); i++) {

            if (text.charAt(i) == '\n') {
                lineCount++;
                if (lineCount >= MAX_LINES) {
                    cutIndex = i;
                    break;
                }
            }

            // simular wrap por longitud aproximada
            // (no perfecto, pero mantiene coherencia sin romper \n)
            if ((i - cutIndex) > MAX_CHARS) {
                lineCount++;
                if (lineCount >= MAX_LINES) {
                    cutIndex = i;
                    break;
                }
            }
        }

        if (cutIndex <= 0 || cutIndex >= text.length()) {
            return splitByChars(text);
        }

        String current = text.substring(0, cutIndex).trim();
        String rest = text.substring(cutIndex).trim();

        return new SplitResult(current, rest);
    }

    // 🔧 fallback original por caracteres
    private SplitResult splitByChars(String text) {
        if (text.length() <= MAX_CHARS) {
            return new SplitResult(text, "");
        }

        int cutAt = MAX_CHARS;
        int lastSpace = text.lastIndexOf(' ', MAX_CHARS);
        if (lastSpace > MAX_CHARS / 2) cutAt = lastSpace;

        return new SplitResult(
                text.substring(0, cutAt).trim(),
                text.substring(cutAt).trim()
        );
    }

    private static class SplitResult {
        String current;
        String rest;

        SplitResult(String current, String rest) {
            this.current = current;
            this.rest = rest;
        }
    }

    public void update() {
        if (fullyRevealed) return;
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime > 40) {
            if (visibleCharacters < fullText.length()) {
                visibleCharacters++;
                lastUpdateTime = now;
            } else {
                fullyRevealed = true;
            }
        }
    }

    public void skipToEnd() {
        visibleCharacters = fullText.length();
        fullyRevealed = true;
    }

    public String getVisibleText() {
        return fullText.substring(0, visibleCharacters);
    }

    public String getSenderName() {
        return senderName;
    }

    public boolean hasSender() {
        return !senderName.isEmpty();
    }

    public boolean isTypewriterDone() {
        return fullyRevealed;
    }

    public boolean hasOverflow() {
        return overflow != null;
    }

    public ChatMessage getOverflow() {
        return overflow;
    }

    public String getFullText() {
        return fullText;
    }
}