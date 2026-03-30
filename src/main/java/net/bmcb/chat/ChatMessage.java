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
    private boolean currentlyPausing = false;
    private boolean currentlySlowDot = false;
    private ChatMessage overflow = null;

    private static final char PAUSE_CHAR         = '\uE000';
    private static final char NEWLINE_PAUSE_CHAR  = '\uE001';
    private static final char SLOW_DOT_CHAR       = '\uE002';

    private static final long PAUSE_DURATION_MS  = 1000;
    private static final long NEWLINE_DURATION_MS = 400;
    private static final long SLOW_DOT_MS         = 400;

    private long currentPauseDuration = PAUSE_DURATION_MS;

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

        // pausa --
        parsedMessage = parsedMessage.replace("--", String.valueOf(PAUSE_CHAR));

        // puntos lentos
        parsedMessage = parsedMessage.replace("...",
                String.valueOf(SLOW_DOT_CHAR) +
                        String.valueOf(SLOW_DOT_CHAR) +
                        String.valueOf(SLOW_DOT_CHAR));

        // saltos de línea manuales con pausa
        String np = String.valueOf(NEWLINE_PAUSE_CHAR);
        parsedMessage = parsedMessage.replace("  ", "\n" + np);
        parsedMessage = parsedMessage.replace("/n ", "\n" + np);
        parsedMessage = parsedMessage.replace("| ", "\n" + np);

        // cortar por líneas reales
        SplitResult result = splitByLines(parsedMessage);

        this.fullText = result.current;

        if (!result.rest.isEmpty()) {
            this.overflow = new ChatMessage(result.rest, parsedName);
        }
    }

    // Constructor interno para overflow
    private ChatMessage(String text, String senderName) {
        this.senderName = senderName;

        text = text.replaceAll(" {2,}", "\n");

        SplitResult result = splitByLines(text);

        this.fullText = result.current;

        if (!result.rest.isEmpty()) {
            this.overflow = new ChatMessage(result.rest, senderName);
        }
    }

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

        List<String> logicalLines = new ArrayList<>();
        for (String part : text.split("\n", -1)) {
            List<OrderedText> wrapped = textRenderer.wrapLines(Text.literal(part), wrapWidth);
            for (int i = 0; i < wrapped.size(); i++) {
                logicalLines.add(part);
            }
        }

        if (logicalLines.size() <= MAX_LINES) {
            return new SplitResult(text, "");
        }

        int lineCount = 0;
        int cutIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCount++;
                if (lineCount >= MAX_LINES) {
                    cutIndex = i;
                    break;
                }
            }
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

        String current = text.substring(0, cutIndex);
        String rest = text.substring(cutIndex);

        return new SplitResult(current, rest);
    }

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

        long interval;
        if (currentlyPausing) {
            interval = currentPauseDuration;
        } else if (currentlySlowDot) {
            interval = SLOW_DOT_MS;
        } else {
            interval = 40;
        }

        if (now - lastUpdateTime < interval) return;

        currentlyPausing = false;
        currentlySlowDot = false;

        if (visibleCharacters < fullText.length()) {
            char next = fullText.charAt(visibleCharacters);

            if (next == PAUSE_CHAR) {
                currentlyPausing = true;
                currentPauseDuration = PAUSE_DURATION_MS;
                lastUpdateTime = now;
                visibleCharacters++;
                return;
            }

            if (next == NEWLINE_PAUSE_CHAR) {
                currentlyPausing = true;
                currentPauseDuration = NEWLINE_DURATION_MS;
                lastUpdateTime = now;
                visibleCharacters++;
                return;
            }

            visibleCharacters++;
            lastUpdateTime = now;

            if (next == SLOW_DOT_CHAR) {
                currentlySlowDot = true;
                ChatSound.playBlip(senderName, '.');
            } else {
                ChatSound.playBlip(senderName, next);
            }

        } else {
            fullyRevealed = true;
        }
    }

    public void skipToEnd() {
        visibleCharacters = fullText.length();
        fullyRevealed = true;
    }

    public String getVisibleText() {
        return fullText.substring(0, visibleCharacters)
                .replace(String.valueOf(PAUSE_CHAR), "")
                .replace(String.valueOf(NEWLINE_PAUSE_CHAR), "")
                .replace(String.valueOf(SLOW_DOT_CHAR), ".");
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