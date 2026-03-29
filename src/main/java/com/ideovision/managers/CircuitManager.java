package com.ideovision.managers;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

    public static int getLapsFromCircuit(String name){
        FileConfiguration config = loadCircuit(name);
        if (config == null) return  -1;
        return  config.getInt("laps");
    }

    public static Location getStartLocation(String name){
        FileConfiguration config = loadCircuit(name);
        if (config == null) return null;

        String worldName = config.getString("start.world", null);
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = config.getDouble("start.x");
        double y = config.getDouble("start.y");
        double z = config.getDouble("start.z");
        float yaw = (float) config.getDouble("start.yaw", 0.0);
        float pitch = (float) config.getDouble("start.pitch", 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Location getEndLocation(String name){
        FileConfiguration config = loadCircuit(name);
        if (config == null) return null;

        String worldName = config.getString("end.world", null);
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = config.getDouble("end.x");
        double y = config.getDouble("end.y");
        double z = config.getDouble("end.z");
        float yaw = (float) config.getDouble("end.yaw", 0.0);
        float pitch = (float) config.getDouble("end.pitch", 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static File getCircuitFolder(){
        return CIRCUITS_FOLDER;
    }
}