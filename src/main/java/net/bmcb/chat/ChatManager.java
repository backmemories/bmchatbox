package net.bmcb.chat;

import java.util.LinkedList;
import java.util.Queue;

public class ChatManager {

    private static final Queue<ChatMessage> queue = new LinkedList<>();
    private static ChatMessage currentMessage = null;

    public static void addMessage(String rawText) {
        // Solo aceptar mensajes que tengan un nombre reconocible
        // Formato <Player> mensaje  o  [Player] mensaje
        boolean hasSender = (rawText.startsWith("<") && rawText.contains("> "))
                || (rawText.startsWith("[") && rawText.contains("] "));
        if (!hasSender) return;

        ChatMessage msg = new ChatMessage(rawText);
        queue.add(msg);

        if (currentMessage == null) {
            currentMessage = queue.poll();
        }
    }

    public static ChatMessage getCurrentMessage() {
        return currentMessage;
    }

    public static void advance() {
        if (currentMessage == null) return;

        if (!currentMessage.isTypewriterDone()) {
            // Primer press: revela todo de golpe
            currentMessage.skipToEnd();
        } else {
            // Segundo press: si hay overflow, va al overflow primero
            if (currentMessage.hasOverflow()) {
                ChatMessage overflow = currentMessage.getOverflow();
                // Insertar el overflow al frente de la cola
                Queue<ChatMessage> temp = new LinkedList<>();
                temp.add(overflow);
                temp.addAll(queue);
                queue.clear();
                queue.addAll(temp);
            }
            currentMessage = queue.poll();
        }
    }

    public static boolean hasMessage() {
        return currentMessage != null;
    }

    public static int getQueueSize() {
        return queue.size();
    }

    // Cuenta overflow + cola para saber si hay "más"
    public static boolean hasMore() {
        return currentMessage != null &&
                (currentMessage.hasOverflow() || !queue.isEmpty());
    }
}