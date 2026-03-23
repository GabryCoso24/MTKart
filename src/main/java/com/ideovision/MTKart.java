package com.ideovision;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.ideovision.commands.RaceCommand;
import com.ideovision.commands.RaceTabCompleter;
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
        getCommand("race").setExecutor(new RaceCommand());
        getCommand("race").setTabCompleter(new RaceTabCompleter());
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MTKart] disabled");
    }
    
}