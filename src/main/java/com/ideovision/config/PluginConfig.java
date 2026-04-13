package com.ideovision.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginConfig {
    private static PluginConfig instance;
    private final JavaPlugin plugin;
    private FileConfiguration config;

    private PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public static PluginConfig getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PluginConfig(plugin);
        }
        return instance;
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // Resource pack settings
    public boolean isResourcePackEnabled() {
        return config.getBoolean("resource-pack.enabled", true);
    }

    public String getResourcePackUrl() {
        return config.getString("resource-pack.url", "");
    }

    public String getResourcePackHash() {
        return config.getString("resource-pack.hash", "");
    }

    public boolean shouldPromptResourcePack() {
        return config.getBoolean("resource-pack.prompt", true);
    }

    // Kart settings
    public int getKartModelId(String kartType) {
        return config.getInt("kart.models." + kartType, 1000);
    }

    public Map<String, Double> getKartStats(String kartType) {
        Map<String, Double> stats = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("kart.stats." + kartType);
        if (section != null) {
            stats.put("speed", section.getDouble("speed", 5.0));
            stats.put("acceleration", section.getDouble("acceleration", 5.0));
            stats.put("handling", section.getDouble("handling", 5.0));
        } else {
            stats.put("speed", 5.0);
            stats.put("acceleration", 5.0);
            stats.put("handling", 5.0);
        }
        return stats;
    }

    // Power-up settings
    public boolean isPowerUpsEnabled() {
        return config.getBoolean("powerups.enabled", true);
    }

    public int getPowerUpModelId(String powerUpType) {
        return config.getInt("powerups.items." + powerUpType + ".model-id", 0);
    }

    public String getPowerUpName(String powerUpType) {
        return config.getString("powerups.items." + powerUpType + ".name", powerUpType);
    }

    public List<String> getPowerUpLore(String powerUpType) {
        return config.getStringList("powerups.items." + powerUpType + ".lore");
    }

    public double getPowerUpDouble(String powerUpType, String path, double defaultValue) {
        return config.getDouble("powerups.items." + powerUpType + "." + path, defaultValue);
    }

    public int getPowerUpInt(String powerUpType, String path, int defaultValue) {
        return config.getInt("powerups.items." + powerUpType + "." + path, defaultValue);
    }

    // Distribution settings
    public boolean isDistributionEnabled() {
        return config.getBoolean("powerups.distribution.enabled", true);
    }

    public int getPowerUpRespawnTime() {
        return config.getInt("powerups.distribution.respawn-time", 100);
    }

    public int getMaxPowerUpsPerBox() {
        return config.getInt("powerups.distribution.max-powerups", 3);
    }

    public Map<String, Double> getPositionChances(int position, int totalPlayers) {
        Map<String, Double> chances = new HashMap<>();
        double normalizedPos = (double) position / totalPlayers;

        boolean positionBased = config.getBoolean("powerups.distribution.position-based.enabled", true);
        if (positionBased) {
            String prefix = normalizedPos <= 0.33 ? "first-place-chance" : "last-place-chance";
            chances.put("legendary", config.getDouble("powerups.distribution.position-based." + prefix + ".legendary", 0.02));
            chances.put("rare", config.getDouble("powerups.distribution.position-based." + prefix + ".rare", 0.18));
            chances.put("common", config.getDouble("powerups.distribution.position-based." + prefix + ".common", 0.80));
        } else {
            chances.put("legendary", 0.05);
            chances.put("rare", 0.25);
            chances.put("common", 0.70);
        }
        return chances;
    }

    // GUI settings
    public boolean isMapGuiEnabled() {
        return config.getBoolean("gui.map.enabled", true);
    }

    public String getMapGuiTitle() {
        return config.getString("gui.map.title", "&6Mappa del Circuito");
    }

    public int getMapGuiRows() {
        return config.getInt("gui.map.rows", 6);
    }

    public int getMapUpdateInterval() {
        return config.getInt("gui.map.update-interval", 20);
    }

    public boolean isLeaderboardEnabled() {
        return config.getBoolean("gui.leaderboard.enabled", true);
    }

    public String getLeaderboardTitle() {
        return config.getString("gui.leaderboard.title", "&6Classifica");
    }

    public int getLeaderboardRows() {
        return config.getInt("gui.leaderboard.rows", 6);
    }

    public int getLeaderboardUpdateInterval() {
        return config.getInt("gui.leaderboard.update-interval", 40);
    }

    // Race settings
    public boolean isCountdownEnabled() {
        return config.getBoolean("race.countdown.enabled", true);
    }

    public List<String> getCountdownTimes() {
        return config.getStringList("race.countdown.times");
    }

    public int getCountdownInterval() {
        return config.getInt("race.countdown.interval", 20);
    }

    // Messages
    public String getMessagePrefix() {
        return config.getString("messages.prefix", "&6&lMTKart &r&8» &r");
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, key);
    }

    public String getColoredMessage(String key) {
        String msg = getMessage(key);
        return colorize(msg);
    }

    public String getColoredMessage(String key, Map<String, String> placeholders) {
        String msg = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return colorize(msg);
    }

    // Sounds
    public String getSound(String key) {
        return config.getString("sounds." + key, "BLOCK_NOTE_BLOCK_PLING");
    }

    private String colorize(String msg) {
        if (msg == null) return "";
        return msg.replace("&", "§");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
