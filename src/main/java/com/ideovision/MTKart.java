package com.ideovision;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.ideovision.commands.RaceCommand;
import com.ideovision.commands.RaceTabCompleter;
import com.ideovision.listeners.PlayerListener;
import com.ideovision.managers.PluginManager;
import com.ideovision.placeholders.MTKartPlaceholderExpansion;

public class MTKart extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MTKartPlaceholderExpansion(this).register();
            getLogger().info("[MTKart] PlaceholderAPI expansion registered");
        }
        
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[MTKart] enabled");
        PluginCommand raceCommand = getCommand("race");
        if (raceCommand != null) {
            raceCommand.setExecutor(new RaceCommand());
            raceCommand.setTabCompleter(new RaceTabCompleter());
        } else {
            getLogger().warning("[MTKart] Command 'race' not found in plugin.yml");
        }
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MTKart] disabled");
    }
    
}