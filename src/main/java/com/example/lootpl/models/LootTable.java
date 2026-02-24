package com.example.lootpl.models;

import java.util.List;
import java.util.Map;

public class LootTable {
    public int size;
    public List<LootPool> pools;

    public static class LootPool {
        public int rolls;
        public List<LootEntry> entries;
    }

    public static class LootEntry {
        public String id; 
        public int weight;
        public int min;
        public int max;

        public String name;
        public List<String> lore;
        public Integer customModelData;
        public Map<String, Integer> enchantments;
        public String nbt; 
    }
}