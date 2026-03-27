package net.bmcb.chat;

import java.util.LinkedList;
import java.util.Queue;

public class ChatManager {

    private static final Queue<ChatMessage> queue = new LinkedList<>();
    private static ChatMessage currentMessage = null;

    public static void addMessage(String text) {
        queue.add(new ChatMessage(text));

        // Si no hay mensaje activo, cargar el primero de una vez
        if (currentMessage == null) {
            currentMessage = queue.poll();
        }
    }

    /**
     * Devuelve el mensaje actual (puede estar escribiéndose o ya completo).
     * NO avanza automáticamente — el jugador debe llamar a advance().
     */
    public static ChatMessage getCurrentMessage() {
        return currentMessage;
    }

    /**
     * El jugador presiona el botón para avanzar al siguiente mensaje.
     * Si el typewriter no terminó todavía, revela el texto completo de golpe.
     * Si ya terminó, pasa al siguiente mensaje de la cola.
     */
    public static void advance() {
        if (currentMessage == null) return;

        if (!currentMessage.isTypewriterDone()) {
            // Primer press: revela todo el texto de una vez (clásico RPG)
            currentMessage.skipToEnd();
        } else {
            // Segundo press: avanza al siguiente mensaje
            currentMessage = queue.poll(); // null si no hay más
        }
    }

    public static boolean hasMessage() {
        return currentMessage != null;
    }

    /**
     * Cuántos mensajes quedan en la cola (sin contar el actual).
     */
    public static int getQueueSize() {
        return queue.size();
    }
}