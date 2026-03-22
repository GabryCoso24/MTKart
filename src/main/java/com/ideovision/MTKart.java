package com.ideovision;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.ideovision.commands.StartRaceCommand;
import com.ideovision.listeners.PlayerListener;
import com.ideovision.managers.PluginManager;

public class MTKart extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[MTKart] enabled");
        getCommand("start-race").setExecutor(new StartRaceCommand());
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MTKart] disabled");
    }
    
}