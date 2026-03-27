package net.bmcb.chat;

public class ChatMessage {

    private final String fullText;
    private int visibleCharacters = 0;
    private long lastUpdateTime = 0;
    private boolean fullyRevealed = false;

    public ChatMessage(String text) {
        this.fullText = text;
    }

    public void update() {
        if (fullyRevealed) return;

        long now = System.currentTimeMillis();
        if (now - lastUpdateTime > 40) { // ~40ms por letra = ritmo cómodo tipo Earthbound
            if (visibleCharacters < fullText.length()) {
                visibleCharacters++;
                lastUpdateTime = now;
            } else {
                fullyRevealed = true;
            }
        }
    }

    public String getVisibleText() {
        return fullText.substring(0, visibleCharacters);
    }

    /**
     * El typewriter terminó de escribir, pero el mensaje NO desaparece solo.
     * ChatManager decide cuándo avanzar al siguiente (cuando el jugador lo pide).
     */
    /**
     * Revela todo el texto de golpe (cuando el jugador presiona avanzar antes de que termine).
     */
    public void skipToEnd() {
        visibleCharacters = fullText.length();
        fullyRevealed = true;
    }

    public boolean isTypewriterDone() {
        return fullyRevealed;
    }

    public String getFullText() {
        return fullText;
    }
}