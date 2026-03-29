package com.ideovision.placeholders;

import org.bukkit.entity.Player;

import com.ideovision.MTKart;
import com.ideovision.managers.LapsManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class MTKartPlaceholderExpansion extends PlaceholderExpansion {

    private final MTKart plugin;

    public MTKartPlaceholderExpansion(MTKart plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "mtkart";
    }

    @Override
    public String getAuthor() {
        return String.join(",", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null) {
            return "";
        }

        String key = params.toLowerCase();
        String raceName = LapsManager.getActiveRaceName();
        int totalLaps = LapsManager.getActiveRaceTotalLaps();

        if (key.equals("race_name")) {
            return raceName != null ? raceName : "none";
        }

        if (key.equals("lap_total")) {
            return String.valueOf(Math.max(totalLaps, 0));
        }

        if (player == null) {
            if (key.equals("status") || key.equals("in_race") || key.equals("finished") || key.equals("lap_current") || key.equals("lap_remaining") || key.equals("lap_formatted")) {
                return "0";
            }
            return null;
        }

        int currentLap = LapsManager.getCurrentLap(player);

        if (key.equals("lap_current")) {
            return String.valueOf(currentLap);
        }

        if (key.equals("lap_remaining")) {
            int remaining = Math.max(totalLaps - currentLap, 0);
            return String.valueOf(remaining);
        }

        if (key.equals("lap_formatted")) {
            return currentLap + "/" + Math.max(totalLaps, 0);
        }

        if (key.equals("in_race")) {
            return LapsManager.isRacing(player) ? "true" : "false";
        }

        if (key.equals("finished")) {
            return LapsManager.hasFinished(player) ? "true" : "false";
        }

        if (key.equals("status")) {
            if (!LapsManager.isRacing(player)) {
                return "idle";
            }
            return LapsManager.hasFinished(player) ? "finished" : "racing";
        }

        return null;
    }
}
