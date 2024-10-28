package eu.kennytv.typedebug.module;

import eu.kennytv.typedebug.TypeDebugPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import static eu.kennytv.typedebug.util.ReflectionUtil.has;

@SuppressWarnings("removal")
public final class ParticleTest extends BukkitRunnable {

    private static final boolean HAS_SETPARTICLE = has(AreaEffectCloud.class, "setParticle", Particle.class, Object.class);
    private static final Particle[] PARTICLES = Particle.values();
    private final TypeDebugPlugin plugin = JavaPlugin.getPlugin(TypeDebugPlugin.class);
    private final Player player;
    private final boolean areaEffectCloud;
    private final AreaEffectCloud cloud;
    private final Location location;
    private int i;

    public ParticleTest(final Player player, final boolean areaEffectCloud) {
        this.player = player;
        this.areaEffectCloud = areaEffectCloud;
        this.location = player.getLocation();

        if (areaEffectCloud) {
            cloud = (AreaEffectCloud) location.getWorld().spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);
            cloud.setDuration(PARTICLES.length * 4);
            cloud.setDurationOnUse(0);
            cloud.setRadiusOnUse(0);
        } else {
            cloud = null;
        }
    }

    @Override
    public void run() {
        if (plugin.isPaused()) {
            return;
        }
        if (i == PARTICLES.length || !player.isOnline()) {
            stop();
            return;
        }

        final Particle particle = PARTICLES[i++];
        final String particleName = particle.name();
        if (particleName.startsWith("LEGACY_")) {
            // ok Spigot
            stop();
            return;
        }

        plugin.getLogger().info("Spawning " + particleName);
        final Object data;
        final Class<?> dataType = particle.getDataType();
        final String typeName = dataType.getSimpleName(); // For compat
        if (particleName.equals("SHRIEK")) {
            data = 0;
        } else if (particleName.equals("SCULK_CHARGE")) {
            data = 0F;
        } else if (dataType == ItemStack.class) {
            data = new ItemStack(Material.STICK);
        } else if (dataType == Color.class) {
            data = Color.fromRGB(255, 0, 0);
        } else if (typeName.equals("BlockData")) {
            data = Bukkit.createBlockData(Material.SAND);
        } else if (typeName.equals("DustOptions")) {
            data = new Particle.DustOptions(Color.RED, 1);
        } else if (typeName.equals("Vibration")) {
            data = VibrationHandler.getData(location, player);
        } else if (typeName.equals("DustTransition")) {
            data = new Particle.DustTransition(Color.RED, Color.BLUE, 1);
        } else if (dataType == Void.class) {
            data = null;
        } else if (dataType == MaterialData.class) {
            data = new MaterialData(Material.SAND);
        } else {
            plugin.getLogger().severe("Missing data for " + particleName + " - " + typeName);
            return;
        }

        if (areaEffectCloud) {
            if (HAS_SETPARTICLE) {
                cloud.setParticle(particle, data);
            } else if (data != null) {
                cloud.setParticle(particle);
            }
        } else {
            location.getWorld().spawnParticle(particle, location, 5, 0.3, 0.3, 0.3, 0, data);
        }
    }

    private void stop() {
        cancel();
        if (cloud != null) {
            cloud.remove();
        }
    }

    // MMMMMMMMMMMMMMMMMMMMMMMMMMMM CLASS LOADING
    private static final class VibrationHandler {

        private static Object getData(final Location location, final Entity entity) {
            return new Vibration(location, new Vibration.Destination.EntityDestination(entity), 20);
        }
    }
}
