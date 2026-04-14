package com.ideovision.commands;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.ideovision.managers.CircuitManager;

public class RaceTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "edit", "set", "save", "delete", "start", "stop");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("inizio", "fine", "laps", "ost", "grid", "powerup", "powerup-remove");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String option = args[1].toLowerCase();
            if (option.equals("ost")) {
                return Arrays.asList("idle", "coconut_mall");
            }
            if (option.equals("grid")) {
                return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
            }
            if (option.equals("powerup-remove")) {
                File folder = CircuitManager.getCircuitFolder();
                File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
                if (files == null) return Collections.emptyList();
                return Arrays.stream(files)
                    .map(File::getName)
                    .map(name -> name.substring(0, name.length() - 4))
                    .collect(Collectors.toList());
            }
        }
        if (args.length == 2 && (
            args[0].equalsIgnoreCase("start")
                || args[0].equalsIgnoreCase("stop")
                || args[0].equalsIgnoreCase("edit")
                || args[0].equalsIgnoreCase("delete")
                || args[0].equalsIgnoreCase("save")
        )) {
            File folder = CircuitManager.getCircuitFolder();
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
            if (files == null) {
                return Collections.emptyList();
            }

            return Arrays.stream(files)
                .map(File::getName)
                .map(name -> name.substring(0, name.length() - 4))
                .collect(Collectors.toList());
        }
        return null;
    }
}