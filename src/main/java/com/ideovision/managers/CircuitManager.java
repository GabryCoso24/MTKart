package com.ideovision.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
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
        config.set("start.yaw", start.getYaw());
        config.set("start.pitch", start.getPitch());

        config.set("end.x", end.getX());
        config.set("end.y", end.getY());
        config.set("end.z", end.getZ());
        config.set("end.world", end.getWorld().getName());
        config.set("end.yaw", end.getYaw());
        config.set("end.pitch", end.getPitch());
        
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

    // === GRIGLIA DI PARTENZA ===

    /**
     * Salva una posizione della griglia di partenza
     */
    public static void saveGridPosition(String name, int slot, Location loc) {
        File file = new File(CIRCUITS_FOLDER, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = "grid.pos" + slot;
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());

        try { config.save(file); } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Ottiene una posizione della griglia di partenza
     */
    public static Location getGridPosition(String name, int slot) {
        FileConfiguration config = loadCircuit(name);
        if (config == null) return null;

        String path = "grid.pos" + slot;
        String worldName = config.getString(path + ".world", null);
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw", 0.0);
        float pitch = (float) config.getDouble(path + ".pitch", 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    // === POWER-UP SPAWNER ===

    /**
     * Salva la posizione di uno spawner power-up
     */
    @SuppressWarnings("unchecked")
    public static void savePowerUpSpawner(String name, Location loc) {
        File file = new File(CIRCUITS_FOLDER, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<Map<String, Object>> spawners = (List<Map<String, Object>>) (List<?>) config.getMapList("powerup-spawners");
        Map<String, Object> newSpawner = new LinkedHashMap<>();
        newSpawner.put("x", loc.getX());
        newSpawner.put("y", loc.getY());
        newSpawner.put("z", loc.getZ());
        newSpawner.put("world", loc.getWorld().getName());
        spawners.add(newSpawner);

        config.set("powerup-spawners", spawners);
        try { config.save(file); } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Ottiene tutte le posizioni degli spawner power-up
     */
    public static List<Location> getPowerUpSpawners(String name) {
        FileConfiguration config = loadCircuit(name);
        if (config == null) return new ArrayList<>();

        List<Location> locations = new ArrayList<>();
        for (Map<?, ?> map : config.getMapList("powerup-spawners")) {
            String worldName = (String) map.get("world");
            if (worldName == null) continue;
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();

            locations.add(new Location(world, x, y, z));
        }
        return locations;
    }

    /**
     * Rimuove lo spawner power-up più vicino alla posizione data (entro 2 blocchi)
     */
    @SuppressWarnings("unchecked")
    public static boolean removePowerUpSpawner(String name, Location loc) {
        File file = new File(CIRCUITS_FOLDER, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<Map<?, ?>> spawners = config.getMapList("powerup-spawners");
        int nearestIndex = -1;
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < spawners.size(); i++) {
            Map<?, ?> map = spawners.get(i);
            String worldName = (String) map.get("world");
            if (worldName == null || !worldName.equals(loc.getWorld().getName())) continue;

            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();

            double dist = new Location(loc.getWorld(), x, y, z).distanceSquared(loc);
            if (dist < minDist && dist < 4.0) {
                minDist = dist;
                nearestIndex = i;
            }
        }

        if (nearestIndex >= 0) {
            spawners.remove(nearestIndex);
            config.set("powerup-spawners", spawners);
            try { config.save(file); } catch (Exception e) { e.printStackTrace(); }
            return true;
        }
        return false;
    }
}