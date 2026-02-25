package com.example.lootpl.listeners;

import com.example.lootpl.LootPlugin;
import com.example.lootpl.managers.DataManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class InteractListener implements Listener {

    private final NamespacedKey typeKey = new NamespacedKey(LootPlugin.getInstance(), "assigner_type");
    private final NamespacedKey kindKey = new NamespacedKey(LootPlugin.getInstance(), "tool_kind");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String typeName = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        String toolKind = item.getItemMeta().getPersistentDataContainer().get(kindKey, PersistentDataType.STRING);
        if (typeName == null || !"container".equals(toolKind)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        event.setCancelled(true); 

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            boolean isLeftClick = (event.getAction() == Action.LEFT_CLICK_BLOCK);
            handleMarking(event.getPlayer(), block.getLocation(), typeName, isLeftClick, true, block.getBlockData().getAsString());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(typeKey, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player) || !(event.getEntity() instanceof ItemFrame frame)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;

        String typeName = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        String toolKind = item.getItemMeta().getPersistentDataContainer().get(kindKey, PersistentDataType.STRING);
        if (typeName == null || !"frame".equals(toolKind)) return;

        event.setCancelled(true); 
        handleMarking(player, frame.getLocation().getBlock().getLocation(), typeName, true, false, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFrameBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player) || !(event.getEntity() instanceof ItemFrame frame)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;

        String typeName = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        String toolKind = item.getItemMeta().getPersistentDataContainer().get(kindKey, PersistentDataType.STRING);
        if (typeName == null || !"frame".equals(toolKind)) return;

        event.setCancelled(true); 
        handleMarking(player, frame.getLocation().getBlock().getLocation(), typeName, true, false, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFrameRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;

        String typeName = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        String toolKind = item.getItemMeta().getPersistentDataContainer().get(kindKey, PersistentDataType.STRING);
        if (typeName == null || !"frame".equals(toolKind)) return;

        event.setCancelled(true); 
        handleMarking(player, frame.getLocation().getBlock().getLocation(), typeName, false, false, null);
    }

    private void handleMarking(Player p, Location loc, String type, boolean isAdd, boolean isContainer, String blockData) {
        DataManager data = LootPlugin.getInstance().getDataManager();
        String locStr = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
        
        if (isAdd) {
            if (isContainer) data.containers.put(locStr, new DataManager.ContainerMark(type, blockData));
            else data.frames.put(locStr, type);
            sendAction(p, ChatColor.GREEN + "Marked -> " + type);
        } else {
            if (isContainer) data.containers.remove(locStr);
            else data.frames.remove(locStr);
            sendAction(p, ChatColor.RED + "Removed Marking");
        }
    }

    private void sendAction(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    }
}