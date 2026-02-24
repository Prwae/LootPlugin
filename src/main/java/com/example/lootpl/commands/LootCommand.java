package com.example.lootpl.commands;

import com.example.lootpl.LootPlugin;
import com.example.lootpl.managers.DataManager;
import com.example.lootpl.managers.LootManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LootCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("lootpl.admin")) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            LootPlugin.getInstance().reloadLootManager();
            sender.sendMessage(ChatColor.GREEN + "LootPlugin JSON tables successfully reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("generate")) {
            sender.sendMessage(ChatColor.YELLOW + "Starting batched loot generation...");
            generateAll(sender);
            return true;
        }

        if (sender instanceof Player p && args.length == 2) {
            if (args[0].equalsIgnoreCase("assigner")) {
                giveItem(p, Material.BLAZE_ROD, args[1], "container");
                return true;
            } else if (args[0].equalsIgnoreCase("assignerframe")) {
                giveItem(p, Material.AMETHYST_SHARD, args[1], "frame");
                return true;
            }
        }
        return false;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "--- LootPlugin Help ---");
        s.sendMessage(ChatColor.YELLOW + "/lootpl generate " + ChatColor.WHITE + "- Refills all marked containers.");
        s.sendMessage(ChatColor.YELLOW + "/lootpl assigner <type> " + ChatColor.WHITE + "- Get Blaze Rod for blocks.");
        s.sendMessage(ChatColor.YELLOW + "/lootpl assignerframe <type> " + ChatColor.WHITE + "- Get Amethyst for frames.");
        s.sendMessage(ChatColor.YELLOW + "/lootpl reload " + ChatColor.WHITE + "- Reloads the JSON files from the disk.");
    }

    private void giveItem(Player p, Material mat, String typeName, String toolKind) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + typeName.toUpperCase() + " ASSIGNER");
        
        NamespacedKey key = new NamespacedKey(LootPlugin.getInstance(), "assigner_type");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, typeName);
        
        NamespacedKey kindKey = new NamespacedKey(LootPlugin.getInstance(), "tool_kind");
        meta.getPersistentDataContainer().set(kindKey, PersistentDataType.STRING, toolKind);

        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        item.setItemMeta(meta);
        p.getInventory().addItem(item);
        p.sendMessage(ChatColor.GREEN + "Received " + toolKind + " tool for: " + typeName);
    }

    private void generateAll(CommandSender sender) {
        DataManager data = LootPlugin.getInstance().getDataManager();
        LootManager loot = LootPlugin.getInstance().getLootManager();

        List<Map.Entry<String, String>> containerQueue = new ArrayList<>(data.containers.entrySet());
        List<Map.Entry<String, String>> frameQueue = new ArrayList<>(data.frames.entrySet());

        int totalTasks = containerQueue.size() + frameQueue.size();
        if (totalTasks == 0) {
            sender.sendMessage(ChatColor.RED + "No marked locations found.");
            return;
        }

        new BukkitRunnable() {
            int completedTasks = 0;

            @Override
            public void run() {
                // --- Process Containers ---
                for (int i = 0; i < 10 && !containerQueue.isEmpty(); i++) {
                    Map.Entry<String, String> entry = containerQueue.remove(0);
                    Location loc = parseLocation(entry.getKey());
                    
                    if (loc != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        BlockState state = loc.getBlock().getState(); 
                        
                        if (state instanceof Container container) {
                            container.getInventory().clear();
                        } else {
                            String clearCmd = String.format("data modify block %d %d %d Items set value []", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clearCmd);
                        }

                        List<ItemStack> items = loot.generateLoot(entry.getValue(), false);
                        if (!items.isEmpty()) {
                            
                            boolean containsGhost = false;
                            NamespacedKey ghostKey = new NamespacedKey(LootPlugin.getInstance(), "ghost_id");
                            for (ItemStack it : items) {
                                if (it != null && it.hasItemMeta() && it.getItemMeta().getPersistentDataContainer().has(ghostKey, PersistentDataType.STRING)) {
                                    containsGhost = true;
                                    break;
                                }
                            }

                            if (state instanceof Container container && !containsGhost) {
                                Inventory inv = container.getInventory();
                                List<Integer> emptySlots = new ArrayList<>();
                                for (int j = 0; j < inv.getSize(); j++) emptySlots.add(j);
                                
                                for (ItemStack item : items) {
                                    if (emptySlots.isEmpty()) break; 
                                    inv.setItem(emptySlots.remove(ThreadLocalRandom.current().nextInt(emptySlots.size())), item);
                                }
                            } else {
                                int customSize = loot.getContainerSize(entry.getValue());
                                injectNbtLoot(loc, items, customSize);
                            }
                        }
                    }
                    completedTasks++;
                }

                // --- Process Item Frames ---
                for (int i = 0; i < 10 && !frameQueue.isEmpty(); i++) {
                    Map.Entry<String, String> entry = frameQueue.remove(0);
                    Location loc = parseLocation(entry.getKey());
                    
                    if (loc != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        for (Entity ent : loc.getChunk().getEntities()) {
                            if (ent instanceof ItemFrame frame && ent.getLocation().getBlock().getLocation().equals(loc)) {
                                List<ItemStack> items = loot.generateLoot(entry.getValue(), true);
                                
                                if (!items.isEmpty()) {
                                    ItemStack item = items.get(0);
                                    NamespacedKey ghostKey = new NamespacedKey(LootPlugin.getInstance(), "ghost_id");
                                    NamespacedKey nbtKey = new NamespacedKey(LootPlugin.getInstance(), "raw_nbt");

                                    // Check if it is a Forge Ghost Item
                                    if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(ghostKey, PersistentDataType.STRING)) {
                                        
                                        String realId = item.getItemMeta().getPersistentDataContainer().get(ghostKey, PersistentDataType.STRING);
                                        String tagStr = "";
                                        
                                        if (item.getItemMeta().getPersistentDataContainer().has(nbtKey, PersistentDataType.STRING)) {
                                            String rawNbt = item.getItemMeta().getPersistentDataContainer().get(nbtKey, PersistentDataType.STRING);
                                            tagStr = ",tag:" + rawNbt;
                                        }

                                        // Clear the frame first, turn it invisible
                                        frame.setItem(new ItemStack(Material.AIR));
                                        frame.setVisible(false);

                                        // Inject the modded item using the entity UUID
                                        String uuid = frame.getUniqueId().toString();
                                        String entityCmd = String.format("data merge entity %s {Item:{id:\"%s\",Count:%db%s}}", uuid, realId, item.getAmount(), tagStr);
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), entityCmd);

                                    } else {
                                        // Standard Bukkit API for Vanilla items
                                        frame.setItem(item);
                                        frame.setVisible(false);
                                    }
                                } else {
                                    frame.setItem(new ItemStack(Material.AIR)); 
                                }
                                break;
                            }
                        }
                    }
                    completedTasks++;
                }

                if (sender instanceof Player p) {
                    int percent = (int) (((double) completedTasks / totalTasks) * 100);
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                        ChatColor.YELLOW + "Loot Progress: " + ChatColor.AQUA + percent + "%"
                    ));
                }

                if (containerQueue.isEmpty() && frameQueue.isEmpty()) {
                    sender.sendMessage(ChatColor.GREEN + "Loot generation complete!");
                    this.cancel();
                }
            }
        }.runTaskTimer(LootPlugin.getInstance(), 0L, 1L); 
    }

    private void injectNbtLoot(Location loc, List<ItemStack> items, int size) {
        if (items.isEmpty()) return;

        List<String> itemStrings = new ArrayList<>();
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < size; i++) emptySlots.add(i);

        NamespacedKey nbtKey = new NamespacedKey(LootPlugin.getInstance(), "raw_nbt");
        NamespacedKey ghostKey = new NamespacedKey(LootPlugin.getInstance(), "ghost_id");

        for (ItemStack item : items) {
            if (item == null || item.getType().isAir() || emptySlots.isEmpty()) continue;

            String id = item.getType().getKey().toString(); 
            
            if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(ghostKey, PersistentDataType.STRING)) {
                id = item.getItemMeta().getPersistentDataContainer().get(ghostKey, PersistentDataType.STRING);
            }

            int count = item.getAmount();
            int randomSlot = emptySlots.remove(ThreadLocalRandom.current().nextInt(emptySlots.size()));

            String tagStr = "";
            if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(nbtKey, PersistentDataType.STRING)) {
                String rawNbt = item.getItemMeta().getPersistentDataContainer().get(nbtKey, PersistentDataType.STRING);
                tagStr = ",tag:" + rawNbt;
            }

            itemStrings.add(String.format("{Slot:%db,id:\"%s\",Count:%db%s}", randomSlot, id, count, tagStr));
        }

        if (!itemStrings.isEmpty()) {
            String itemsArray = String.join(",", itemStrings);
            String mergeCmd = String.format("data merge block %d %d %d {Items:[%s]}", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), itemsArray);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), mergeCmd);
        }
    }

    private Location parseLocation(String locStr) {
        String[] pts = locStr.split(",");
        if (pts.length != 4 || Bukkit.getWorld(pts[0]) == null) return null;
        return new Location(Bukkit.getWorld(pts[0]), Integer.parseInt(pts[1]), Integer.parseInt(pts[2]), Integer.parseInt(pts[3]));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("generate", "assigner", "assignerframe", "reload", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            LootManager lm = LootPlugin.getInstance().getLootManager();
            if (args[0].equalsIgnoreCase("assigner")) {
                return lm.getContainerTypes().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("assignerframe")) {
                return lm.getFrameTypes().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}