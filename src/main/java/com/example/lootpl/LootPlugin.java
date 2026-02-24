package com.example.lootpl;

import com.example.lootpl.commands.LootCommand;
import com.example.lootpl.listeners.InteractListener;
import com.example.lootpl.managers.DataManager;
import com.example.lootpl.managers.LootManager;
import com.example.lootpl.tasks.ParticleTask;
import org.bukkit.plugin.java.JavaPlugin;

public class LootPlugin extends JavaPlugin {

    private static LootPlugin instance;
    private DataManager dataManager;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        instance = this;

        // Ensure the plugin folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize Managers
        this.dataManager = new DataManager();
        this.lootManager = new LootManager();

        // Register Commands and Tab Completer
        LootCommand lootCommand = new LootCommand();
        getCommand("lootpl").setExecutor(lootCommand);
        getCommand("lootpl").setTabCompleter(lootCommand);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new InteractListener(), this);

        // Start the Particle Task (Green Boxes)
        new ParticleTask().runTaskTimer(this, 0L, 10L);

        getLogger().info("LootPlugin has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save all marked containers and frames before shutdown
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("LootPlugin disabled and data saved.");
    }

    public static LootPlugin getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    // --- THE MISSING RELOAD METHOD ---
    public void reloadLootManager() {
        this.lootManager = new LootManager();
    }
}