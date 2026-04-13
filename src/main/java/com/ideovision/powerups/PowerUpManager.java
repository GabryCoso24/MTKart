package com.ideovision.powerups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.ideovision.MTKart;
import com.ideovision.config.PluginConfig;
import com.ideovision.managers.LapsManager;

public class PowerUpManager {
    private final MTKart plugin;
    private final PluginConfig config;
    private final Map<UUID, List<PowerUp>> playerInventory = new HashMap<>();
    private final Set<Location> placedBananas = new HashSet<>();
    private final Random random = new Random();

    public PowerUpManager(MTKart plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.getInstance(plugin);
    }

    /**
     * Dà un power-up casuale basato sulla posizione in gara
     */
    public PowerUp giveRandomPowerUp(Player player) {
        if (!config.isPowerUpsEnabled()) {
            return null;
        }

        int position = getPlayerPosition(player);
        int totalPlayers = getTotalRacers();

        Map<String, Double> chances = config.getPositionChances(position, totalPlayers);
        PowerUpType selectedType = selectPowerUpByRarity(chances);

        if (selectedType == null) {
            selectedType = PowerUpType.GREEN_SHELL;
        }

        PowerUp powerUp = new PowerUp(selectedType, plugin);

        List<PowerUp> inventory = playerInventory.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        inventory.add(powerUp);

        addPowerUpToHotbar(player, powerUp.toItemStack());

        player.sendMessage(config.getColoredMessage("powerup-got",
            Map.of("powerup", config.getPowerUpName(selectedType.getName()))));

        Location playerLocation = Objects.requireNonNull(player.getLocation(), "player location");
        player.playSound(playerLocation,
            resolveSound(config.getSound("powerup_collect"), Sound.ENTITY_ITEM_PICKUP),
            SoundCategory.PLAYERS, 1.0f, 1.0f);

        return powerUp;
    }

    private void addPowerUpToHotbar(Player player, ItemStack item) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack slotItem = player.getInventory().getItem(slot);
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                player.getInventory().setItem(slot, item);
                player.getInventory().setHeldItemSlot(slot);
                return;
            }
        }

        player.getInventory().addItem(item);
    }

    private PowerUpType selectPowerUpByRarity(Map<String, Double> chances) {
        double roll = random.nextDouble();

        if (roll < chances.get("legendary")) {
            List<PowerUpType> legendary = List.of(PowerUpType.STAR, PowerUpType.LIGHTNING);
            return legendary.get(random.nextInt(legendary.size()));
        }

        roll -= chances.get("legendary");
        if (roll < chances.get("rare")) {
            List<PowerUpType> rare = List.of(PowerUpType.RED_SHELL, PowerUpType.MUSHROOM, PowerUpType.BLOOPER);
            return rare.get(random.nextInt(rare.size()));
        }

        List<PowerUpType> common = List.of(PowerUpType.GREEN_SHELL, PowerUpType.BANANA, PowerUpType.BOB_OMB);
        return common.get(random.nextInt(common.size()));
    }

    private int getPlayerPosition(Player player) {
        if (!LapsManager.isRacing(player)) {
            return 1;
        }

        List<Player> racers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (LapsManager.isRacing(p)) {
                racers.add(p);
            }
        }

        racers.sort((a, b) -> {
            int lapA = LapsManager.getCurrentLap(a);
            int lapB = LapsManager.getCurrentLap(b);
            return Integer.compare(lapB, lapA);
        });

        return racers.indexOf(player) + 1;
    }

    private int getTotalRacers() {
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (LapsManager.isRacing(p)) {
                count++;
            }
        }
        return Math.max(count, 1);
    }

    /**
     * Rimuove un power-up dall'inventario del giocatore
     */
    public boolean removePowerUp(Player player, PowerUpType type) {
        List<PowerUp> inventory = playerInventory.get(player.getUniqueId());
        if (inventory == null) {
            return false;
        }

        for (PowerUp p : inventory) {
            if (p.getType() == type) {
                inventory.remove(p);
                return true;
            }
        }
        return false;
    }

    public List<PowerUp> getPlayerInventory(Player player) {
        return playerInventory.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    /**
     * Lancia un proiettile (guscio)
     */
    public void launchProjectile(Player player, PowerUp powerUp, boolean homing) {
        Location loc = Objects.requireNonNull(player.getEyeLocation(), "eye location");
        Vector direction = loc.getDirection();
        final Vector[] velocityHolder = { direction.clone().multiply(homing ? 0.8 : 1.2) };
        org.bukkit.World world = Objects.requireNonNull(loc.getWorld(), "projectile world");

        ItemStack shellItem = powerUp.toItemStack();
        ItemDisplay shell = world.spawn(loc, ItemDisplay.class, display -> {
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setItemStack(shellItem);
            display.setGlowing(true);
        });

        shell.setVelocity(velocityHolder[0]);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!shell.isValid() || shell.isDead()) {
                return;
            }

            Location shellLoc = shell.getLocation();

            org.bukkit.World shellWorld = shellLoc.getWorld();
            if (shellWorld == null) {
                shell.remove();
                return;
            }

            shellWorld.spawnParticle(Particle.ITEM, shellLoc, 5,
                0.1, 0.1, 0.1, 0, shellItem);

            if (homing) {
                Player target = findNearestTarget(shellLoc, player, direction);
                if (target != null) {
                    Location targetLocation = Objects.requireNonNull(target.getLocation(), "target location");
                    Vector toTarget = targetLocation.toVector().subtract(shellLoc.toVector()).normalize();
                    velocityHolder[0] = velocityHolder[0].multiply(0.9).add(toTarget.multiply(0.1));
                    shell.setVelocity(velocityHolder[0]);
                }
            }

            for (Player p : shellWorld.getPlayers()) {
                Location playerLocation = Objects.requireNonNull(p.getLocation(), "player location");
                if (!p.equals(player) && playerLocation.distance(shellLoc) < 1.5) {
                    hitPlayer(p, player);
                    shell.remove();
                    return;
                }
            }

            if (shell.getTicksLived() > 100) {
                shell.remove();
            }
        }, 1L, 1L);
    }

    private Player findNearestTarget(Location from, Player shooter, Vector direction) {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        org.bukkit.World world = from.getWorld();
        if (world == null) {
            return null;
        }

        for (Player p : world.getPlayers()) {
            if (p.equals(shooter) || !LapsManager.isRacing(p)) {
                continue;
            }

            Location playerLocation = Objects.requireNonNull(p.getLocation(), "player location");
            Vector toPlayer = playerLocation.toVector().subtract(from.toVector());
            if (toPlayer.dot(direction) < 0) {
                continue;
            }

            double dist = toPlayer.length();
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }

        return nearest;
    }

    /**
     * Posiziona una banana a terra
     */
    @SuppressWarnings("deprecation")
    public void placeBanana(Location location, Player owner) {
        Location bananaLoc = location.clone().subtract(0, 1, 0);
        org.bukkit.World world = Objects.requireNonNull(bananaLoc.getWorld(), "banana world");

        ItemStack bananaItem = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = bananaItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(2003);
            meta.setDisplayName("§eBanana");
            bananaItem.setItemMeta(meta);
        }

        ItemDisplay banana = world.spawn(bananaLoc, ItemDisplay.class, display -> {
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setItemStack(bananaItem);
            display.setGlowing(true);
        });

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!banana.isValid() || banana.isDead()) {
                return;
            }

            for (Player p : world.getPlayers()) {
                Location playerLocation = Objects.requireNonNull(p.getLocation(), "player location");
                if (playerLocation.distance(banana.getLocation()) < 1.0) {
                    if (!p.equals(owner) && LapsManager.isRacing(p)) {
                        applySlip(p, owner);
                    }
                    banana.remove();
                    return;
                }
            }

            if (banana.getTicksLived() > 600) {
                banana.remove();
            }
        }, 1L, 5L);
    }

    /**
     * Applica effetto scivolamento (banana)
     */
    private void applySlip(Player player, Player causer) {
        Location playerLocation = Objects.requireNonNull(player.getLocation(), "player location");
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3));

        Vector knockback = new Vector(0, 0.5, 0);
        player.setVelocity(knockback);

        player.getWorld().playSound(playerLocation, Sound.ENTITY_SLIME_JUMP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.CLOUD, playerLocation, 20, 0.5, 0.5, 0.5, 0.1);

        player.sendMessage(config.getColoredMessage("hit-by", Map.of("player", causer != null ? causer.getName() : "una banana")));
    }

    private Sound resolveSound(String soundName, Sound fallback) {
        if (soundName == null || soundName.isBlank()) {
            return fallback;
        }

        NamespacedKey key = NamespacedKey.fromString(soundName);
        if (key == null) {
            return fallback;
        }

        Sound sound = Registry.SOUNDS.get(key);
        return sound != null ? sound : fallback;
    }

    /**
     * Applica boost di velocità (fungo)
     */
    public void applyBoost(Player player, int duration, double multiplier) {
        Location playerLocation = Objects.requireNonNull(player.getLocation(), "player location");
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2));

        player.getWorld().spawnParticle(Particle.DUST, playerLocation, 30,
            0.5, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.RED, 1.0f));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }, duration);
    }

    /**
     * Applica effetto stella (invincibilità + velocità)
     */
    public void applyStarEffect(Player player, int duration) {
        Location playerLocation = Objects.requireNonNull(player.getLocation(), "player location");
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 1));

        player.getWorld().spawnParticle(Particle.DUST, playerLocation, 50,
            0.5, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.AQUA, 1.0f));
        player.getWorld().spawnParticle(Particle.END_ROD, playerLocation, 20, 0.3, 0.3, 0.3, 0.05);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.GLOWING);
            }
        }, duration);
    }

    /**
     * Applica effetto fulmine (colpisce tutti)
     */
    public void applyLightning(Player caster) {
        org.bukkit.World world = Objects.requireNonNull(caster.getWorld(), "caster world");
        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && LapsManager.isRacing(target)) {
                Location targetLocation = Objects.requireNonNull(target.getLocation(), "target location");
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));

                world.strikeLightningEffect(targetLocation);
                world.playSound(targetLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 1.0f, 1.0f);

                target.sendMessage("§eSei stato colpito dal fulmine di " + caster.getName() + "!");
            }
        }
    }

    /**
     * Applica effetto calamaro (cecità agli avversari)
     */
    public void applyBlooper(Player caster) {
        org.bukkit.World world = Objects.requireNonNull(caster.getWorld(), "caster world");
        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && LapsManager.isRacing(target)) {
                Location targetLocation = Objects.requireNonNull(target.getLocation(), "target location");
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));

                world.spawnParticle(Particle.SQUID_INK, targetLocation, 100, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    /**
     * Lancia una Bob-omba
     */
    public void launchBobOmb(Player player, PowerUp powerUp) {
        Location loc = Objects.requireNonNull(player.getEyeLocation(), "eye location");
        Vector direction = loc.getDirection();
        org.bukkit.World world = Objects.requireNonNull(loc.getWorld(), "bob-omb world");

        ItemStack bobOmbItem = powerUp.toItemStack();
        ItemDisplay bobOmb = world.spawn(loc, ItemDisplay.class, display -> {
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setItemStack(bobOmbItem);
            display.setGlowing(true);
        });

        Vector velocity = direction.multiply(0.8);
        bobOmb.setVelocity(velocity);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (bobOmb.isValid() && !bobOmb.isDead()) {
                explode(bobOmb.getLocation(), player);
                bobOmb.remove();
            }
        }, 60L);
    }

    private void explode(Location location, Player owner) {
        org.bukkit.World world = Objects.requireNonNull(location.getWorld(), "explosion world");
        world.createExplosion(location, 2.0f, false, false);
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.FLAME, location, 30, 0.5, 0.5, 0.5, 0.1);

        for (Player p : world.getPlayers()) {
            Location playerLocation = Objects.requireNonNull(p.getLocation(), "player location");
            if (!p.equals(owner) && playerLocation.distance(location) < 5.0) {
                if (LapsManager.isRacing(p)) {
                    p.setVelocity(playerLocation.toVector().subtract(location.toVector()).normalize().multiply(2).setY(1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                    p.damage(3.0, owner);

                    p.sendMessage(config.getColoredMessage("hit-by", Map.of("player", owner.getName())));
                }
            }
        }
    }

    private void hitPlayer(Player victim, Player attacker) {
        Location victimLocation = Objects.requireNonNull(victim.getLocation(), "victim location");
        Location attackerLocation = Objects.requireNonNull(attacker.getLocation(), "attacker location");
        victim.setVelocity(victimLocation.toVector().subtract(attackerLocation.toVector()).normalize().multiply(1.5).setY(0.5));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

        victim.getWorld().playSound(victimLocation, Sound.ENTITY_GENERIC_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        victim.getWorld().spawnParticle(Particle.CRIT, victimLocation, 20, 0.5, 0.5, 0.5, 0.1);

        victim.sendMessage(config.getColoredMessage("hit-by", Map.of("player", attacker.getName())));
    }

    public void clear() {
        playerInventory.clear();
        placedBananas.clear();
    }
}
