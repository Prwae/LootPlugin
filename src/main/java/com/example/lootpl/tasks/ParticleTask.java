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
        Particle.DustOptions greenDust = new Particle.DustOptions(Color.LIME, 1.5F);
        NamespacedKey typeKey = new NamespacedKey(LootPlugin.getInstance(), "assigner_type");
        NamespacedKey kindKey = new NamespacedKey(LootPlugin.getInstance(), "tool_kind");

        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (!item.hasItemMeta()) continue;
            
            String typeName = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            String toolKind = item.getItemMeta().getPersistentDataContainer().get(kindKey, PersistentDataType.STRING);
            if (typeName == null || toolKind == null) continue;

            Map<String, String> targetMap = toolKind.equals("container") ? data.containers : data.frames;
            String worldName = p.getLocation().getWorld().getName();

            for (Map.Entry<String, String> entry : targetMap.entrySet()) {
                if (!entry.getValue().equals(typeName)) continue;

                String[] pts = entry.getKey().split(",");
                if (!pts[0].equals(worldName)) continue;

                int x = Integer.parseInt(pts[1]);
                int y = Integer.parseInt(pts[2]);
                int z = Integer.parseInt(pts[3]);
                
                if (Math.abs(p.getLocation().getBlockX() - x) > 50 || Math.abs(p.getLocation().getBlockZ() - z) > 50) continue;
                
                spawnBox(p, new Location(p.getWorld(), x, y, z), greenDust);
            }
        }
    }

    private void spawnBox(Player p, Location loc, Particle.DustOptions dust) {
        Location center = loc.clone().add(0.5, 0.5, 0.5);
        p.spawnParticle(Particle.REDSTONE, center, 8, 0.4, 0.4, 0.4, 0, dust);
    }
}