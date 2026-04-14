package com.ideovision.race;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.ideovision.MTKart;
import com.ideovision.config.ScoreboardConfig;
import com.ideovision.config.ScoreboardConfig.ScoreboardLayout;
import com.ideovision.managers.LapsManager;

public class ScoreboardManager {
    private final MTKart plugin;
    private final ScoreboardConfig scoreboardConfig;
    private final Map<UUID, Integer> updateTasks = new HashMap<>();

    public ScoreboardManager(MTKart plugin) {
        this.plugin = plugin;
        this.scoreboardConfig = plugin.getScoreboardConfig();
    }

    /**
     * Mostra la scoreboard di gara a un giocatore
     */
    public void showRaceScoreboard(Player player) {
        showRaceScoreboard(player, "default_scoreboard");
    }

    /**
     * Mostra la scoreboard di gara a un giocatore con un layout specifico
     */
    public void showRaceScoreboard(Player player, String layoutName) {
        org.bukkit.scoreboard.ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            return;
        }

        ScoreboardLayout layout = scoreboardConfig.getLayout(layoutName);
        if (layout == null) {
            layout = scoreboardConfig.getDefaultLayout();
        }

        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("mtkart", Criteria.DUMMY, layout.title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboard(player, objective, layout);
        updateTabList(scoreboard);

        player.setScoreboard(scoreboard);

        startUpdateTask(player, scoreboard, objective, layout);
    }

    /**
     * Nasconde la scoreboard a un giocatore
     */
    public void hideScoreboard(Player player) {
        Integer taskId = updateTasks.remove(player.getUniqueId());
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        org.bukkit.scoreboard.ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            return;
        }

        Scoreboard blank = scoreboardManager.getNewScoreboard();
        player.setScoreboard(blank);
    }

    /**
     * Nasconde tutte le scoreboard
     */
    public void hideAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hideScoreboard(player);
        }
    }

    private void updateScoreboard(Player player, Objective objective, ScoreboardLayout layout) {
        RaceManager raceManager = plugin.getRaceManager();
        int position = raceManager.getPosition(player);
        int total = raceManager.getTotalRacers();
        String positionColor = getPositionColor(position);
        int currentLap = LapsManager.getCurrentLap(player);
        int totalLaps = LapsManager.getActiveRaceTotalLaps();
        Long bestTime = raceManager.getBestLapTime(player);
        String bestTimeString = bestTime != null ? raceManager.formatTime(bestTime) : "--:--";

        List<String> formattedLines = new ArrayList<>();
        for (String line : layout.lines) {
            formattedLines.add(formatLine(line, player, raceManager, position, positionColor, currentLap, totalLaps, bestTimeString));
        }

        // Se la classifica è abilitata e ci sono placeholder, espandi la classifica
        if (layout.leaderboardEnabled) {
            List<String> expandedLines = new ArrayList<>();
            for (String line : formattedLines) {
                if (line.contains("%leaderboard_")) {
                    int pos = getLeaderboardPositionFromPlaceholder(line);
                    if (pos > 0) {
                        for (int i = 1; i <= layout.leaderboardPositions; i++) {
                            Player racer = raceManager.getPlayerAtPosition(i);
                            if (racer != null) {
                                String racerLine = line
                                    .replace("%leaderboard_" + i + "%", getRacerDisplayName(racer, player))
                                    .replace("%leaderboard_pos%", String.valueOf(i))
                                    .replace("%leaderboard_name%", racer.getName());
                                expandedLines.add(racerLine);
                            }
                        }
                    } else {
                        // Placeholder generico - espandi tutti
                        for (int i = 1; i <= layout.leaderboardPositions; i++) {
                            Player racer = raceManager.getPlayerAtPosition(i);
                            if (racer != null) {
                                expandedLines.add(getRacerDisplayName(racer, player));
                            }
                        }
                    }
                } else {
                    expandedLines.add(line);
                }
            }
            formattedLines = expandedLines;
        }

        // Assegna i punteggi partendo dal basso
        int score = formattedLines.size() - 1;
        for (String line : formattedLines) {
            setScore(objective, score--, line);
        }
    }

    private String formatLine(String line, Player player, RaceManager raceManager,
                              int position, String positionColor, int currentLap, int totalLaps, String bestTimeString) {
        return line
            .replace("%player%", player.getName())
            .replace("%position%", String.valueOf(position))
            .replace("%position_color%", positionColor)
            .replace("%total%", String.valueOf(raceManager.getTotalRacers()))
            .replace("%current_lap%", String.valueOf(currentLap))
            .replace("%total_laps%", String.valueOf(totalLaps))
            .replace("%best_lap%", bestTimeString)
            .replace("%race_time%", raceManager.getFormattedRaceTime());
    }

    private int getLeaderboardPositionFromPlaceholder(String line) {
        for (int i = 1; i <= 12; i++) {
            if (line.contains("%leaderboard_" + i + "%")) {
                return i;
            }
        }
        return 0;
    }

    private String getRacerDisplayName(Player racer, Player viewer) {
        if (racer.equals(viewer)) {
            return ChatColor.YELLOW + "" + ChatColor.BOLD + ">" + racer.getName() + " <";
        }
        return ChatColor.GRAY + racer.getName();
    }

    /**
     * Aggiorna la tab list con i giocatori ordinati per posizione di gara
     */
    private void updateTabList(Scoreboard scoreboard) {
        RaceManager raceManager = plugin.getRaceManager();
        List<UUID> leaderboard = raceManager.getLeaderboard();

        // Crea/aggiorna i team per ogni posizione (1-12)
        for (int i = 1; i <= 12; i++) {
            String teamName = "mtp" + String.format("%02d", i);
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }

            // Rimuovi tutti i membri precedenti
            for (String entry : new HashSet<>(team.getEntries())) {
                team.removeEntry(entry);
            }

            // Imposta il prefisso con la posizione
            team.setPrefix(getTabPositionPrefix(i));

            // Aggiungi il giocatore in questa posizione
            if (i <= leaderboard.size()) {
                Player racer = Bukkit.getPlayer(leaderboard.get(i - 1));
                if (racer != null) {
                    team.addEntry(racer.getName());
                }
            }
        }

        // Team per i giocatori non in gara
        Team noneTeam = scoreboard.getTeam("mtznone");
        if (noneTeam == null) {
            noneTeam = scoreboard.registerNewTeam("mtznone");
        }
        for (String entry : new HashSet<>(noneTeam.getEntries())) {
            noneTeam.removeEntry(entry);
        }
        noneTeam.setPrefix("§8");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!leaderboard.contains(p.getUniqueId())) {
                noneTeam.addEntry(p.getName());
            }
        }
    }

    private String getTabPositionPrefix(int position) {
        return switch (position) {
            case 1 -> "§6#1 ";
            case 2 -> "§f#2 ";
            case 3 -> "§e#3 ";
            default -> "§7#" + position + " ";
        };
    }

    private String getPositionColor(int position) {
        return switch (position) {
            case 1 -> ChatColor.GOLD.toString();
            case 2 -> ChatColor.WHITE.toString();
            case 3 -> ChatColor.YELLOW.toString();
            default -> ChatColor.GRAY.toString();
        };
    }

    private void setScore(Objective objective, int score, String name) {
        Score scoreObj = objective.getScore(name);
        scoreObj.setScore(score);
    }

    private void startUpdateTask(Player player, Scoreboard scoreboard, Objective objective, ScoreboardLayout layout) {
        final int[] taskId = new int[1];
        taskId[0] = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            plugin,
            () -> {
                if (!player.isOnline()) {
                    plugin.getServer().getScheduler().cancelTask(taskId[0]);
                    return;
                }

                if (player.getScoreboard().equals(scoreboard)) {
                    for (String entry : scoreboard.getEntries()) {
                        scoreboard.resetScores(entry);
                    }
                    updateScoreboard(player, objective, layout);
                    updateTabList(scoreboard);
                } else {
                    plugin.getServer().getScheduler().cancelTask(taskId[0]);
                }
            },
            layout.updateInterval,
            layout.updateInterval
        );

        updateTasks.put(player.getUniqueId(), taskId[0]);
    }

    /**
     * Mostra il titolo di fine gara
     */
    public void showFinishTitle(Player player, int position, int total) {
        String positionText = switch (position) {
            case 1 -> "&6&l1° POSTO";
            case 2 -> "&f&l2° POSTO";
            case 3 -> "&e&l3° POSTO";
            default -> "&7" + position + "° POSTO";
        };

        String subtitle = switch (position) {
            case 1 -> "&e&lVITTORIA!";
            case 2 -> "&7Buon lavoro!";
            case 3 -> "&7Sul podio!";
            default -> "&8Continua ad allenarti!";
        };

        player.sendTitle(
            ChatColor.translateAlternateColorCodes('&', positionText),
            ChatColor.translateAlternateColorCodes('&', subtitle),
            10, 60, 20
        );
    }
}