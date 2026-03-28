package net.bmcb.chat;

public class ChatMessage {

    private static final int MAX_CHARS = 90;

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
            // Formato chat normal: "<Player123> mensaje"
            int closeAngle = rawText.indexOf("> ");
            parsedName = rawText.substring(1, closeAngle);
            parsedMessage = rawText.substring(closeAngle + 2);
        } else if (rawText.startsWith("[") && rawText.contains("] ")) {
            // Formato /say: "[Player456] mensaje"
            int closeBracket = rawText.indexOf("] ");
            parsedName = rawText.substring(1, closeBracket);
            parsedMessage = rawText.substring(closeBracket + 2);
        } else {
            // Mensaje de sistema sin nombre
            parsedName = "";
            parsedMessage = rawText;
        }

        this.senderName = parsedName;

        if (parsedMessage.length() > MAX_CHARS) {
            int cutAt = MAX_CHARS;
            int lastSpace = parsedMessage.lastIndexOf(' ', MAX_CHARS);
            if (lastSpace > MAX_CHARS / 2) cutAt = lastSpace;

            this.fullText = parsedMessage.substring(0, cutAt).trim();
            String rest = parsedMessage.substring(cutAt).trim();
            if (!rest.isEmpty()) {
                this.overflow = new ChatMessage(rest, parsedName);
            }
        } else {
            this.fullText = parsedMessage;
        }
    }

    // Constructor interno para overflow
    private ChatMessage(String text, String senderName) {
        this.senderName = senderName;

        if (text.length() > MAX_CHARS) {
            int cutAt = MAX_CHARS;
            int lastSpace = text.lastIndexOf(' ', MAX_CHARS);
            if (lastSpace > MAX_CHARS / 2) cutAt = lastSpace;

            this.fullText = text.substring(0, cutAt).trim();
            String rest = text.substring(cutAt).trim();
            if (!rest.isEmpty()) {
                this.overflow = new ChatMessage(rest, senderName);
            }
        } else {
            this.fullText = text;
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