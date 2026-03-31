package net.bmcb.chat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FlavorManager {

    // sabor de cada jugador: nombre → sabor
    private static final Map<String, String> flavorMap = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Path SAVE_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("bmchatbox_flavors.json");

    // Devuelve el sabor de un jugador
    public static String getFlavor(String playerName) {
        return flavorMap.getOrDefault(playerName, "dia_nublado");
    }

    // Asigna un sabor a un jugador y guarda
    public static void setFlavor(String playerName, String flavor) {
        flavorMap.put(playerName, flavor);
        save();
    }

    // Carga el archivo al iniciar el juego
    public static void load() {
        try {
            File file = SAVE_FILE.toFile();
            if (!file.exists()) return;

            Reader reader = new FileReader(file);
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> loaded = gson.fromJson(reader, type);
            reader.close();

            if (loaded != null) {
                flavorMap.clear();
                flavorMap.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("bmchatbox: error cargando sabores: " + e.getMessage());
        }
    }

    // Guarda el archivo
    private static void save() {
        try {
            Writer writer = new FileWriter(SAVE_FILE.toFile());
            gson.toJson(flavorMap, writer);
            writer.close();
        } catch (Exception e) {
            System.err.println("bmchatbox: error guardando sabores: " + e.getMessage());
        }
    }
}