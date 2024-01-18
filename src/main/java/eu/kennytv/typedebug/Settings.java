package eu.kennytv.typedebug;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public final class Settings {

    private final TypeDebugPlugin plugin;
    private final Set<EntityType> ignoredEntityTypes = EnumSet.noneOf(EntityType.class);
    private int itemsPerTick;
    private int itemSpawnDelay;
    private int entitySpawnDelay;
    private int particleSpawnDelay;

    public Settings(final TypeDebugPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        final FileConfiguration config = plugin.getConfig();

        ignoredEntityTypes.clear();
        for (final String entityName : config.getStringList("ignored-entities")) {
            try {
                ignoredEntityTypes.add(EntityType.valueOf(entityName.toUpperCase(Locale.ROOT)));
            } catch (final IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity: " + entityName);
            }
        }

        itemsPerTick = config.getInt("items-per-tick", 1);
        itemSpawnDelay = config.getInt("item-spawn-delay", 0);
        entitySpawnDelay = config.getInt("entity-spawn-delay", 7);
        particleSpawnDelay = config.getInt("particle-spawn-delay", 4);
    }

    public Set<EntityType> ignoredEntityTypes() {
        return ignoredEntityTypes;
    }

    public int itemsPerTick() {
        return itemsPerTick;
    }

    public int itemSpawnDelay() {
        return itemSpawnDelay;
    }

    public int entitySpawnDelay() {
        return entitySpawnDelay;
    }

    public int particleSpawnDelay() {
        return particleSpawnDelay;
    }
}
