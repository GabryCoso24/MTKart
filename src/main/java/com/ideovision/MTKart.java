package com.ideovision;

import org.bukkit.plugin.java.JavaPlugin;
import com.ideovision.managers.PluginManager;
import com.ideovision.listeners.PlayerListener;

public class MTKart extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        getLogger().info("MTKart has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MTKart has been disabled!");
    }
    
}