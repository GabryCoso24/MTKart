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
            return Arrays.asList("create", "set", "start", "stop");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("inizio", "fine", "laps", "ost");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("ost")) {
            return Arrays.asList("idle", "coconut_mall");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop"))) {
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
