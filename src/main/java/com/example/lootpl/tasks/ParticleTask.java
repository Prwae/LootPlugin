package com.example.lootpl.tasks;

import com.example.lootpl.LootPlugin;
import com.example.lootpl.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class ParticleTask extends BukkitRunnable {
    @Override
    public void run() {
        DataManager data = LootPlugin.getInstance().getDataManager();
        // Reduced size from 1.5F to 0.6F for smaller, sharper particles
        Particle.DustOptions greenDust = new Particle.DustOptions(Color.LIME, 0.6F);
        NamespacedKey typeKey = new NamespacedKey(LootPlugin.getInstance(), "assigner_type");
        NamespacedKey kindKey = new NamespacedKey(LootPlugin.getInstance(), "tool_kind");

        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (!item.hasItemMeta()) continue;
            
            String typeName = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            String toolKind = item.getItemMeta().getPersistentDataContainer().get(kindKey, PersistentDataType.STRING);
            if (typeName == null || toolKind == null) continue;

            String worldName = p.getLocation().getWorld().getName();

            if (toolKind.equals("container")) {
                for (Map.Entry<String, DataManager.ContainerMark> entry : data.containers.entrySet()) {
                    if (!entry.getValue().type.equals(typeName)) continue;
                    validateAndSpawnBox(p, entry.getKey(), worldName, greenDust);
                }
            } else {
                for (Map.Entry<String, String> entry : data.frames.entrySet()) {
                    if (!entry.getValue().equals(typeName)) continue;
                    validateAndSpawnBox(p, entry.getKey(), worldName, greenDust);
                }
            }
        }
    }

    private void validateAndSpawnBox(Player p, String locStr, String worldName, Particle.DustOptions dust) {
        String[] pts = locStr.split(",");
        if (!pts[0].equals(worldName)) return;

        int x = Integer.parseInt(pts[1]);
        int y = Integer.parseInt(pts[2]);
        int z = Integer.parseInt(pts[3]);
        
        if (Math.abs(p.getLocation().getBlockX() - x) > 50 || Math.abs(p.getLocation().getBlockZ() - z) > 50) return;
        
        spawnBoxOutline(p, x, y, z, dust);
    }

    private void spawnBoxOutline(Player p, int startX, int startY, int startZ, Particle.DustOptions dust) {
        double[] offsets = {0.0, 0.25, 0.5, 0.75, 1.0}; // 5 points per edge

        for (double d : offsets) {
            // Bottom edges
            spawnPoint(p, startX + d, startY, startZ, dust);
            spawnPoint(p, startX, startY, startZ + d, dust);
            spawnPoint(p, startX + 1, startY, startZ + d, dust);
            spawnPoint(p, startX + d, startY, startZ + 1, dust);

            // Top edges
            spawnPoint(p, startX + d, startY + 1, startZ, dust);
            spawnPoint(p, startX, startY + 1, startZ + d, dust);
            spawnPoint(p, startX + 1, startY + 1, startZ + d, dust);
            spawnPoint(p, startX + d, startY + 1, startZ + 1, dust);

            // Vertical edges
            spawnPoint(p, startX, startY + d, startZ, dust);
            spawnPoint(p, startX + 1, startY + d, startZ, dust);
            spawnPoint(p, startX, startY + d, startZ + 1, dust);
            spawnPoint(p, startX + 1, startY + d, startZ + 1, dust);
        }
    }

    private void spawnPoint(Player p, double x, double y, double z, Particle.DustOptions dust) {
        // Count: 1, Offsets: 0, Speed: 0 -> Spawns exactly at the provided coordinate
        p.spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0, dust);
    }
}