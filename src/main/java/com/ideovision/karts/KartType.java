package com.ideovision.karts;

import org.bukkit.ChatColor;

public enum KartType {
    DEFAULT("default", "&7Kart Standard"),
    SPEED_KART("speed_kart", "&cKart Veloce"),
    BALANCE_KART("balance_kart", "&aKart Bilanciato"),
    ACCELERATION_KART("acceleration_kart", "&bKart Accelerazione");

    private final String configName;
    private final String displayName;

    KartType(String configName, String displayName) {
        this.configName = configName;
        this.displayName = displayName;
    }

    public String getConfigName() {
        return configName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredDisplayName() {
        return displayName.replace("&", "§");
    }

    public static KartType fromString(String name) {
        try {
            return KartType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}
