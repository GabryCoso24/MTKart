package com.ideovision.managers;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CircuitManager {
    private static final File CIRCUITS_FOLDER = new File(
        Bukkit.getPluginManager().getPlugin("MTKart").getDataFolder(), 
        "Circuits"
    );

    static {
        if (!CIRCUITS_FOLDER.exists()) {
            CIRCUITS_FOLDER.mkdirs();
        }
    }

    public static void saveCircuit(String name, Location start, Location end, int laps, String ost) {
        File file = new File(CIRCUITS_FOLDER, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("name", name);
        config.set("start.x", start.getX());
        config.set("start.y", start.getY());
        config.set("start.z", start.getZ());
        config.set("start.world", start.getWorld().getName());
        
        config.set("end.x", end.getX());
        config.set("end.y", end.getY());
        config.set("end.z", end.getZ());
        config.set("end.world", end.getWorld().getName());
        
        config.set("laps", laps);
        config.set("ost", ost);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration loadCircuit(String name) {
        File file = new File(CIRCUITS_FOLDER, name + ".yml");
        if (!file.exists()) return null;
        return YamlConfiguration.loadConfiguration(file);
    }

    public static String getOstFromCircuit(String name) {
        FileConfiguration config = loadCircuit(name);
        if (config == null) return null;
        return config.getString("ost", null);
    }

    public static File getCircuitFolder(){
        return CIRCUITS_FOLDER;
    }
}