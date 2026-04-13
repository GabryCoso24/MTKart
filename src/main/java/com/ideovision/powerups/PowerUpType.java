package com.ideovision.powerups;

public enum PowerUpType {
    GREEN_SHELL("green_shell", 2001, Rarity.COMMON),
    RED_SHELL("red_shell", 2002, Rarity.RARE),
    BANANA("banana", 2003, Rarity.COMMON),
    MUSHROOM("mushroom", 2004, Rarity.RARE),
    STAR("star", 2005, Rarity.LEGENDARY),
    LIGHTNING("lightning", 2006, Rarity.LEGENDARY),
    BLOOPER("blooper", 2007, Rarity.RARE),
    BOB_OMB("bob_omb", 2008, Rarity.COMMON);

    private final String name;
    private final int modelId;
    private final Rarity rarity;

    PowerUpType(String name, int modelId, Rarity rarity) {
        this.name = name;
        this.modelId = modelId;
        this.rarity = rarity;
    }

    public String getName() {
        return name;
    }

    public int getModelId() {
        return modelId;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public enum Rarity {
        COMMON,
        RARE,
        LEGENDARY
    }
}
