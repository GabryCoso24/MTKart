package com.ideovision;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.ideovision.commands.GUICommand;
import com.ideovision.commands.KartCommand;
import com.ideovision.commands.PowerUpCommand;
import com.ideovision.commands.RaceCommand;
import com.ideovision.commands.RaceTabCompleter;
import com.ideovision.config.PluginConfig;
import com.ideovision.config.ScoreboardConfig;
import com.ideovision.gui.RaceGUI;
import com.ideovision.karts.KartManager;
import com.ideovision.listeners.PlayerListener;
import com.ideovision.listeners.PowerUpListener;
import com.ideovision.listeners.RaceListener;
import com.ideovision.managers.PluginManager;
import com.ideovision.placeholders.MTKartPlaceholderExpansion;
import com.ideovision.powerups.PowerUpManager;
import com.ideovision.powerups.PowerUpSpawnerManager;
import com.ideovision.race.RaceManager;
import com.ideovision.race.StartingGridManager;
import com.ideovision.race.ScoreboardManager;

public class MTKart extends JavaPlugin {

    private static MTKart instance;

    private PluginConfig config;
    private ScoreboardConfig scoreboardConfig;
    private PowerUpManager powerUpManager;
    private PowerUpSpawnerManager powerUpSpawnerManager;
    private KartManager kartManager;
    private RaceManager raceManager;
    private ScoreboardManager scoreboardManager;
    private StartingGridManager startingGridManager;
    private RaceGUI raceGUI;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        saveDefaultConfig();
        config = PluginConfig.getInstance(this);

        // Initialize scoreboard config
        scoreboardConfig = new ScoreboardConfig(this);

        // Initialize managers
        PluginManager.getInstance().initialize();
        powerUpManager = new PowerUpManager(this);
        powerUpSpawnerManager = new PowerUpSpawnerManager(this);
        kartManager = new KartManager(this);
        raceManager = new RaceManager(this);
        scoreboardManager = new ScoreboardManager(this);
        startingGridManager = new StartingGridManager(this);
        raceGUI = new RaceGUI(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new PowerUpListener(this), this);
        getServer().getPluginManager().registerEvents(new RaceListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MTKartPlaceholderExpansion(this).register();
            getLogger().info("[MTKart] PlaceholderAPI expansion registered");
        }

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[MTKart] enabled");

        // Register commands
        PluginCommand raceCommand = getCommand("race");
        if (raceCommand != null) {
            raceCommand.setExecutor(new RaceCommand(this));
            raceCommand.setTabCompleter(new RaceTabCompleter());
        } else {
            getLogger().warning("[MTKart] Command 'race' not found in plugin.yml");
        }

        PluginCommand kartCommand = getCommand("kart");
        if (kartCommand != null) {
            kartCommand.setExecutor(new KartCommand(this));
        }

        PluginCommand guiCommand = getCommand("mtkgui");
        if (guiCommand != null) {
            guiCommand.setExecutor(new GUICommand(this));
        }

        PluginCommand powerupCommand = getCommand("powerup");
        if (powerupCommand != null) {
            powerupCommand.setExecutor(new PowerUpCommand(this));
        }
    }

    @Override
    public void onDisable() {
        // Cleanup
        if (kartManager != null) {
            kartManager.clearAllKarts();
        }
        if (powerUpManager != null) {
            powerUpManager.clear();
        }
        if (powerUpSpawnerManager != null) {
            powerUpSpawnerManager.stopSpawners();
        }
        if (startingGridManager != null) {
            startingGridManager.unfreezeAll();
        }
        if (scoreboardManager != null) {
            scoreboardManager.hideAllScoreboards();
        }
        if (raceGUI != null) {
            raceGUI.closeAllGUIs();
        }

        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MTKart] disabled");
    }

    public static MTKart getInstance() {
        return instance;
    }

    public PluginConfig getConfigManager() {
        return config;
    }

    public ScoreboardConfig getScoreboardConfig() {
        return scoreboardConfig;
    }

    public PowerUpManager getPowerUpManager() {
        return powerUpManager;
    }

    public PowerUpSpawnerManager getPowerUpSpawnerManager() {
        return powerUpSpawnerManager;
    }

    public KartManager getKartManager() {
        return kartManager;
    }

    public RaceManager getRaceManager() {
        return raceManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public StartingGridManager getStartingGridManager() {
        return startingGridManager;
    }

    public RaceGUI getRaceGUI() {
        return raceGUI;
    }
}