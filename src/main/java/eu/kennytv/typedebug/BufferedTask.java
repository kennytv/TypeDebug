package eu.kennytv.typedebug;

import eu.kennytv.typedebug.util.BlockEntities;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class BufferedTask extends BukkitRunnable {
    private static final TypeDebugPlugin plugin = JavaPlugin.getPlugin(TypeDebugPlugin.class);
    private final Location location;
    private final Player player;
    private final int distance;
    private boolean forwards;
    private int i;
    private int counter;

    protected BufferedTask(final Player player, final int distance) {
        this.player = player;
        this.distance = distance;
        this.location = player.getLocation();
    }

    @Override
    public void run() {
        if (plugin.isPaused()) {
            return;
        }
        if (i == BlockEntities.BLOCK_ENTITY_TYPES.size() || !player.isOnline()) {
            cancel();
            return;
        }

        if (counter++ == 7) {
            // Next row
            counter = 0;
            forwards = !forwards;
            location.add(0, 0, 4);
        }

        this.set(i++, location);

        // Step sideways
        location.add(forwards ? distance: -distance, 0, 0);
    }

    protected abstract void set(final int i, final Location location);
}
