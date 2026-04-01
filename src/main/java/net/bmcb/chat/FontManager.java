package net.bmcb.chat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private static final Map<String, String> fontMap = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Path SAVE_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("bmchatbox_fonts.json");

    // fuentes disponibles
    public static final String FONT_DEFAULT = "minecraft:default";
    public static final String FONT_EARTHBOUND  = "bmchatbox:font_earthbound";

    // devuelve la fuente de un jugador, vanilla por defecto
    public static String getFont(String playerName) {
        String font = fontMap.getOrDefault(playerName, "default");
        switch (font) {
            case "font_earthbound": return FONT_EARTHBOUND;
            default: return FONT_DEFAULT;
        }
    }

    public static void setFont(String playerName, String font) {
        fontMap.put(playerName, font);
        save();
    }

    public static void load() {
        try {
            File file = SAVE_FILE.toFile();
            if (!file.exists()) return;

            Reader reader = new FileReader(file);
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> loaded = gson.fromJson(reader, type);
            reader.close();

            if (loaded != null) {
                fontMap.clear();
                fontMap.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("bmchatbox: error cargando fuentes: " + e.getMessage());
        }
    }

    private static void save() {
        try {
            Writer writer = new FileWriter(SAVE_FILE.toFile());
            gson.toJson(fontMap, writer);
            writer.close();
        } catch (Exception e) {
            System.err.println("bmchatbox: error guardando fuentes: " + e.getMessage());
        }
    }
}