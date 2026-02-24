package com.example.lootpl.managers;

import com.example.lootpl.LootPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    
    public Map<String, String> containers = new HashMap<>();
    public Map<String, String> frames = new HashMap<>();
    
    private final File file;
    private final Gson gson;

    public DataManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.file = new File(LootPlugin.getInstance().getDataFolder(), "data.json");
        loadData();
    }

    private void loadData() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<DataStorage>(){}.getType();
            DataStorage storage = gson.fromJson(reader, type);
            if (storage != null) {
                if (storage.containers != null) this.containers = storage.containers;
                if (storage.frames != null) this.frames = storage.frames;
            }
        } catch (IOException e) {
            LootPlugin.getInstance().getLogger().severe("Could not load data.json!");
            e.printStackTrace();
        }
    }

    // --- THE MISSING SAVE METHOD ---
    public void saveData() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                DataStorage storage = new DataStorage();
                storage.containers = this.containers;
                storage.frames = this.frames;
                gson.toJson(storage, writer);
            }
        } catch (IOException e) {
            LootPlugin.getInstance().getLogger().severe("Could not save to data.json!");
            e.printStackTrace();
        }
    }

    // A simple wrapper class to help Gson format the JSON neatly
    private static class DataStorage {
        Map<String, String> containers;
        Map<String, String> frames;
    }
}