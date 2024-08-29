package eu.kennytv.typedebug.util;

import eu.kennytv.typedebug.TypeDebugPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class BufferedTask extends BukkitRunnable {
    private static final TypeDebugPlugin PLUGIN = JavaPlugin.getPlugin(TypeDebugPlugin.class);
    private final Location location;
    private final Player player;
    private final int distance;
    private final int totalSteps;
    private boolean forwards;
    private int i;
    private int counter;

    protected BufferedTask(final Player player, final int totalSteps, final int distance) {
        this.player = player;
        this.totalSteps = totalSteps;
        this.distance = distance;
        this.location = player.getLocation();
    }

    @Override
    public void run() {
        if (PLUGIN.isPaused()) {
            return;
        }

        if (i == totalSteps || !player.isOnline()) {
            cancel();
            return;
        }

        if (counter++ == 7) {
            // Next row
            counter = 0;
            forwards = !forwards;
            location.add(0, 0, 4);
        }

        this.test(i++, location);

        // Step sideways
        location.add(forwards ? distance : -distance, 0, 0);
    }

    protected abstract void test(final int i, final Location location);
}
