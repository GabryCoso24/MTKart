package com.ideovision.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ideovision.MTKart;

/**
 * Gestisce il caricamento dei layout di scoreboard da file YAML.
 * I file devono trovarsi nella cartella "scoreboards" nella plugin folder.
 */
public class ScoreboardConfig {
    private static final File SCOREBOARDS_FOLDER = new File(
        MTKart.getInstance().getDataFolder(),
        "scoreboards"
    );

    private final MTKart plugin;
    private final Map<String, ScoreboardLayout> layouts = new HashMap<>();
    private ScoreboardLayout defaultLayout;

    public ScoreboardConfig(MTKart plugin) {
        this.plugin = plugin;

        if (!SCOREBOARDS_FOLDER.exists()) {
            SCOREBOARDS_FOLDER.mkdirs();
            createDefaultScoreboardFile();
        }

        loadAllScoreboards();
    }

    /**
     * Crea un file scoreboard di default con un esempio
     */
    private void createDefaultScoreboardFile() {
        File defaultFile = new File(SCOREBOARDS_FOLDER, "default_scoreboard.yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("title", "&6&lMTKART");
        config.set("update-interval", 20);

        List<String> lines = new ArrayList<>();
        lines.add("&f");
        lines.add("&7Tempo: &e%race_time%");
        lines.add("&7Posizione: %position_color%#%position%&7/&e%total%");
        lines.add("&f");
        lines.add("&7Giro: &e%current_lap%&7/&e%total_laps%");
        lines.add("&f");
        lines.add("&7Miglior giro: &a%best_lap%");
        lines.add("&f");
        lines.add("&6&lClassifica:");
        lines.add("%leaderboard_1%");
        lines.add("%leaderboard_2%");
        lines.add("%leaderboard_3%");
        lines.add("&f");

        config.set("lines", lines);

        try {
            config.save(defaultFile);
            plugin.getLogger().info("Creato file scoreboard di default");
        } catch (IOException e) {
            plugin.getLogger().severe("Errore nella creazione del file scoreboard di default: " + e.getMessage());
        }
    }

    /**
     * Carica tutti i file scoreboard dalla cartella
     */
    public void loadAllScoreboards() {
        layouts.clear();
        defaultLayout = null;

        File[] files = SCOREBOARDS_FOLDER.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Nessun file scoreboard trovato nella cartella scoreboards/");
            return;
        }

        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = file.getName().replace(".yml", "");

                ScoreboardLayout layout = parseLayout(config);
                if (layout != null) {
                    layouts.put(name, layout);
                    plugin.getLogger().info("Caricato scoreboard: " + name);

                    if (name.contains("default") || defaultLayout == null) {
                        defaultLayout = layout;
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Errore nel caricamento del file " + file.getName() + ": " + e.getMessage());
            }
        }

        if (defaultLayout == null) {
            defaultLayout = createFallbackLayout();
        }
    }

    /**
     * Parsea un file YAML in un oggetto ScoreboardLayout
     */
    private ScoreboardLayout parseLayout(FileConfiguration config) {
        ScoreboardLayout layout = new ScoreboardLayout();
        layout.title = ChatColor.translateAlternateColorCodes('&', config.getString("title", "&6&lMTKART"));
        layout.updateInterval = config.getLong("update-interval", 20L);

        List<String> rawLines = config.getStringList("lines");
        if (rawLines.isEmpty()) {
            // Se non ci sono linee, usa un layout base
            rawLines = createDefaultLines();
        }

        for (String line : rawLines) {
            layout.lines.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        // Carica le configurazioni opzionali per la classifica
        ConfigurationSection leaderboardSection = config.getConfigurationSection("leaderboard");
        if (leaderboardSection != null) {
            layout.leaderboardEnabled = leaderboardSection.getBoolean("enabled", true);
            layout.leaderboardPositions = leaderboardSection.getInt("positions", 3);
        } else {
            layout.leaderboardEnabled = true;
            layout.leaderboardPositions = 3;
        }

        return layout;
    }

    private List<String> createDefaultLines() {
        List<String> lines = new ArrayList<>();
        lines.add("&f");
        lines.add("&7Tempo: &e%race_time%");
        lines.add("&7Pos: %position_color%#%position%&7/&e%total%");
        lines.add("&f");
        lines.add("&7Giro: &e%current_lap%&7/&e%total_laps%");
        lines.add("&f");
        lines.add("&6&lClassifica:");
        lines.add("%leaderboard_1%");
        lines.add("%leaderboard_2%");
        lines.add("%leaderboard_3%");
        return lines;
    }

    /**
     * Crea un layout di fallback se nessun file è disponibile
     */
    private ScoreboardLayout createFallbackLayout() {
        ScoreboardLayout layout = new ScoreboardLayout();
        layout.title = "§6§lMTKART";
        layout.updateInterval = 20L;
        layout.lines = createDefaultLines();
        layout.leaderboardEnabled = true;
        layout.leaderboardPositions = 3;
        return layout;
    }

    /**
     * Ottiene un layout per nome (senza .yml)
     */
    public ScoreboardLayout getLayout(String name) {
        return layouts.getOrDefault(name, defaultLayout);
    }

    /**
     * Ottiene il layout di default
     */
    public ScoreboardLayout getDefaultLayout() {
        return defaultLayout;
    }

    /**
     * Ricarica tutti i scoreboard (utile per /mtkart reload)
     */
    public void reload() {
        loadAllScoreboards();
    }

    /**
     * Classe che rappresenta un layout di scoreboard
     */
    public static class ScoreboardLayout {
        public String title;
        public List<String> lines = new ArrayList<>();
        public long updateInterval;
        public boolean leaderboardEnabled;
        public int leaderboardPositions;

        public ScoreboardLayout() {}

        /**
         * Crea una copia profonda del layout
         */
        public ScoreboardLayout clone() {
            ScoreboardLayout copy = new ScoreboardLayout();
            copy.title = this.title;
            copy.lines = new ArrayList<>(this.lines);
            copy.updateInterval = this.updateInterval;
            copy.leaderboardEnabled = this.leaderboardEnabled;
            copy.leaderboardPositions = this.leaderboardPositions;
            return copy;
        }
    }
}
