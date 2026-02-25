package com.example.lootpl.managers;

import com.example.lootpl.LootPlugin;
import com.example.lootpl.models.LootTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class LootManager {

    private final Map<String, LootTable> containerTables = new HashMap<>();
    private final Map<String, LootTable> frameTables = new HashMap<>();
    private final Gson gson = new Gson();

    public LootManager() {
        loadTables("containers.json", containerTables);
        loadTables("frames.json", frameTables);
    }

    private void loadTables(String fileName, Map<String, LootTable> map) {
        File file = new File(LootPlugin.getInstance().getDataFolder(), fileName);
        if (!file.exists()) {
            LootPlugin.getInstance().saveResource(fileName, false);
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, LootTable>>(){}.getType();
            Map<String, LootTable> loaded = gson.fromJson(reader, type);
            if (loaded != null) map.putAll(loaded);
        } catch (Exception e) {
            LootPlugin.getInstance().getLogger().severe("Failed to load " + fileName);
            e.printStackTrace();
        }
    }

    public int getContainerSize(String type) {
        LootTable table = containerTables.get(type);
        return (table != null && table.size > 0) ? table.size : 27;
    }

    public List<String> getContainerTypes() {
        return new ArrayList<>(containerTables.keySet());
    }

    public List<String> getFrameTypes() {
        return new ArrayList<>(frameTables.keySet());
    }

    public LootTable getContainerTable(String type) {
        return containerTables.get(type);
    }

    public List<ItemStack> generateLoot(String type, boolean isFrame) {
        List<ItemStack> items = new ArrayList<>();
        LootTable table = isFrame ? frameTables.get(type) : containerTables.get(type);
        if (table == null) return items;

        for (LootTable.LootPool pool : table.pools) {
            for (int i = 0; i < pool.rolls; i++) {
                int totalWeight = pool.entries.stream().mapToInt(e -> e.weight).sum();
                if (totalWeight == 0) continue;

                int roll = ThreadLocalRandom.current().nextInt(totalWeight);
                int currentWeight = 0;

                for (LootTable.LootEntry entry : pool.entries) {
                    currentWeight += entry.weight;
                    if (roll < currentWeight) {
                        
                        Material mat = Material.matchMaterial(entry.id);
                        if (mat == null && entry.id.contains(":")) {
                            mat = Material.matchMaterial(entry.id.split(":")[1].toUpperCase());
                        }

                        boolean isGhostItem = false;
                        if (mat == null) {
                            mat = Material.BEDROCK; 
                            isGhostItem = true;
                        }

                        if (mat != Material.AIR) {
                            int amount = ThreadLocalRandom.current().nextInt(entry.min, entry.max + 1);
                            ItemStack item = new ItemStack(mat, amount);

                            if (entry.nbt != null && !entry.nbt.isEmpty()) {
                                try {
                                    item = Bukkit.getUnsafe().modifyItemStack(item, entry.nbt);
                                } catch (Exception e) {
                                    LootPlugin.getInstance().getLogger().warning("Syntax error in NBT for item: " + entry.id);
                                }
                            }

                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                if (entry.name != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', entry.name));
                                if (entry.lore != null) {
                                    List<String> coloredLore = new ArrayList<>();
                                    for (String line : entry.lore) coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                                    meta.setLore(coloredLore);
                                }
                                if (entry.customModelData != null) meta.setCustomModelData(entry.customModelData);
                                if (entry.enchantments != null) {
                                    for (Map.Entry<String, Integer> ench : entry.enchantments.entrySet()) {
                                        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(ench.getKey().toLowerCase()));
                                        if (e != null) meta.addEnchant(e, ench.getValue(), true);
                                    }
                                }
                                
                                if (entry.nbt != null && !entry.nbt.isEmpty()) {
                                    NamespacedKey nbtKey = new NamespacedKey(LootPlugin.getInstance(), "raw_nbt");
                                    meta.getPersistentDataContainer().set(nbtKey, org.bukkit.persistence.PersistentDataType.STRING, entry.nbt);
                                }

                                if (isGhostItem) {
                                    NamespacedKey ghostKey = new NamespacedKey(LootPlugin.getInstance(), "ghost_id");
                                    meta.getPersistentDataContainer().set(ghostKey, org.bukkit.persistence.PersistentDataType.STRING, entry.id);
                                }
                                
                                item.setItemMeta(meta);
                            }

                            items.add(item);
                        }
                        break;
                    }
                }
            }
        }
        return items;
    }
}