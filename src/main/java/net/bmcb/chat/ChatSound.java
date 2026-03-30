package net.bmcb.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatSound {

    // -------------------------
    // 🔊 REGISTRO DE SONIDOS
    // -------------------------
    public static final SoundEvent BLIP_BACKMEMORIES    = register("blip_backmemories");

    public static final SoundEvent BLIP_DEFAULT = register("blip_default");
    public static final SoundEvent BLIP_JOHN    = register("blip_john");
    public static final SoundEvent BLIP_JUAN    = register("blip_juan");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of("bmchatbox", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {}

    // -------------------------
    // BLIP PLAYER
    // -------------------------
    private static final Random random = new Random();
    private static final String SILENT_CHARS = " *;:-_\n\"'()[]{}";

    private static final Map<String, SoundEvent> VOICE_MAP   = new HashMap<>();
    private static final Map<String, float[]>    PITCH_RANGES = new HashMap<>();

    static {
        VOICE_MAP.put("BackMemories", BLIP_BACKMEMORIES);
        PITCH_RANGES.put("BackMemories", new float[]{1.0f, 1.0f});

        // John: voz grave
        VOICE_MAP.put("John", BLIP_JOHN);
        PITCH_RANGES.put("John", new float[]{0.6f, 0.8f});

        // Juan: voz aguda
        VOICE_MAP.put("Juan", BLIP_JUAN);
        PITCH_RANGES.put("Juan", new float[]{1.3f, 1.6f});

        // Default
        PITCH_RANGES.put("default", new float[]{1.0f, 1.0f});
    }

    public static void playBlip(String senderName, char character) {
        if (SILENT_CHARS.indexOf(character) >= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        SoundEvent sound = VOICE_MAP.getOrDefault(senderName, BLIP_DEFAULT);
        float[] range = PITCH_RANGES.getOrDefault(senderName,
                PITCH_RANGES.getOrDefault("default", new float[]{0.9f, 1.1f}));

        float pitch = range[0] + random.nextFloat() * (range[1] - range[0]);

        client.getSoundManager().play(
                new PositionedSoundInstance(
                        sound.id(),
                        net.minecraft.sound.SoundCategory.UI, //usar el slider de UI para el volumen
                        1.0f, pitch, net.minecraft.util.math.random.Random.create(), false, 0,
                        net.minecraft.client.sound.SoundInstance.AttenuationType.NONE,
                        0, 0, 0, true
                )
        );
    }
}