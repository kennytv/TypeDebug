package eu.kennytv.blockdebug;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class TypeDebugPlugin extends JavaPlugin implements Listener {

    private static final boolean HAS_ITEM_GETKEY = has(Item.class, "getKey");
    private static final boolean HAS_MATERIAL_ISAIR = has(Material.class, "isAir");
    private static final boolean HAS_ENTITY_SETGRAVITY = has(Entity.class, "setGravity");
    private static final boolean HAS_ENTITY_SETINVULNERABLE = has(Entity.class, "setInvulnerable");
    private static final Version VERSION;
    private final Set<EntityType> ignoredEntityTypes = EnumSet.noneOf(EntityType.class);
    private int itemsPerTick;

    static {
        if (has("net.minecraft.world.level.block.state.BlockState")) {
            VERSION = Version.SANE;
        } else if (has("net.minecraft.world.level.block.state.IBlockData")) {
            VERSION = Version.STUPID;
        } else {
            VERSION = Version.REALLY_STUPID;
        }
    }

    private static boolean has(final Class<?> clazz, final String method) {
        try {
            clazz.getDeclaredMethod(method);
            return true;
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean has(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onEnable() {
        getCommand("start").setExecutor(this);
        saveDefaultConfig();

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        for (final String entityName : config.getStringList("ignored-entities")) {
            try {
                ignoredEntityTypes.add(EntityType.valueOf(entityName.toUpperCase(Locale.ROOT)));
            } catch (final IllegalArgumentException e) {
                getLogger().warning("Invalid entity: " + entityName);
            }
        }

        itemsPerTick = getConfig().getInt("items-per-tick", 1);

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Congratulations, you are running a " + VERSION.versionName + " server version.");
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String s, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("no");
            return true;
        }
        if (args.length != 1) {
            return false;
        }

        final Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("blocks")) {
            try {
                setBlocks(player);
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("blocksbutinbad")) {
            setBlocksButInBad(player);
        } else if (args[0].equalsIgnoreCase("entities")) {
            spawnEntities(player);
        } else if (args[0].equalsIgnoreCase("items")) {
            spawnItems(player);
        } else {
            return false;
        }
        return true;
    }

    private void setBlocks(final Player player) throws ReflectiveOperationException {
        final World world = player.getWorld();
        final Location location = player.getLocation();
        int i = 0;
        boolean forwards = false;
        final int y = location.getBlockY();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        final Class<?> blockClass = VERSION.nmsSupplier.clazz("net.minecraft.world.level.block", "Block", "Block");
        final Class<?> blockStateClass = VERSION.nmsSupplier.clazz("net.minecraft.world.level.block.state", "IBlockData", "BlockState");
        final Class<?> craftBlockClass = NMSUtil.cbClass("block.CraftBlock");
        final Method setTypeAndData = craftBlockClass.getDeclaredMethod("setTypeAndData", blockStateClass, Boolean.TYPE);
        setTypeAndData.setAccessible(true);
        for (final Object blockState : (Iterable) blockClass.getDeclaredField(VERSION == Version.SANE ? "BLOCK_STATE_REGISTRY" : "REGISTRY_ID").get(null)) {
            if (i++ == 100) {
                // Next row
                i = 0;
                forwards = !forwards;
                z += 2;
            }

            // Step sideways
            if (forwards) {
                x += 2;
            } else {
                x -= 2;
            }

            final Block blockAt = world.getBlockAt(x, y, z);
            setTypeAndData.invoke(blockAt, blockState, false);
        }
    }

    private void setBlocksButInBad(final Player player) {
        final World world = player.getWorld();
        final Location location = player.getLocation();
        boolean forwards = false;
        final int y = location.getBlockY();
        int i = 0;
        int x = location.getBlockX();
        int z = location.getBlockZ();
        final Iterator<Material> iterator = Arrays.stream(Material.values()).iterator();
        while (iterator.hasNext()) {
            final Material material = iterator.next();
            if (!material.isBlock()) {
                continue;
            }
            if (i++ == 15) {
                i = 0;
                forwards = !forwards;
                z += 2;
            }
            if (forwards) {
                x += 2;
            } else {
                x -= 2;
            }
            final Block blockAt = world.getBlockAt(x, y, z);
            blockAt.setType(material, false);
        }
    }

    private void spawnEntities(final Player player) {
        final World world = player.getWorld();
        final Location location = player.getLocation();
        final List<EntityType> types = Arrays.stream(EntityType.values()).filter(EntityType::isSpawnable).filter(type -> !ignoredEntityTypes.contains(type)).collect(Collectors.toList());
        new BukkitRunnable() {
            private int i;
            private int counter;
            private boolean forwards;

            @Override
            public void run() {
                if (i == types.size() || !player.isOnline()) {
                    cancel();
                    return;
                }

                final EntityType entityType = types.get(i++);
                if (counter++ == 7) {
                    // Next row
                    counter = 0;
                    forwards = !forwards;
                    location.add(0, 0, 4);
                }

                getLogger().info("Spawning " + entityType.name());
                final Entity entity = world.spawnEntity(location, entityType);
                if (HAS_ENTITY_SETGRAVITY) {
                    entity.setGravity(false);
                }
                if (HAS_ENTITY_SETINVULNERABLE) {
                    entity.setInvulnerable(true);
                }

                // Step sideways
                location.add(forwards ? 4 : -4, 0, 0);
            }
        }.runTaskTimer(this, 7, 7);
    }

    private void spawnItems(final Player player) {
        final World world = player.getWorld();
        final Location location = player.getLocation();
        final Material[] materials = Material.values();
        new BukkitRunnable() {
            private final Item[] lastItems = new Item[itemsPerTick];
            private int i;

            @Override
            public void run() {
                if (i != 0) {
                    // Remove last items
                    for (final Item item : lastItems) {
                        player.getInventory().remove(item.getItemStack());
                        item.remove();
                    }
                }

                final Location clone = location.clone();
                for (int j = 0; j < itemsPerTick; j++) {
                    if (!next(j, clone)) {
                        return;
                    }
                    clone.add(0.45, 0, 0);
                }
            }

            private boolean next(final int j, final Location location) {
                if (i == materials.length || !player.isOnline()) {
                    stop();
                    return false;
                }

                // Try until we find a spawnable item
                Material material;
                do {
                    if (i >= materials.length) {
                        stop();
                        return false;
                    }
                } while (!(material = materials[i++]).isItem() || isAir(material));

                getLogger().info("Spawning " + (HAS_ITEM_GETKEY ? material.getKey() : material.name()));
                final ItemStack itemStack = new ItemStack(material);
                final Item item = world.dropItem(location, itemStack);
                item.setGravity(false);
                item.setInvulnerable(true);

                lastItems[j] = item;
                player.getInventory().addItem(itemStack);
                return true;
            }

            private void stop() {
                cancel();
                getLogger().info("Done!");
            }

            private boolean isAir(final Material material) {
                return HAS_MATERIAL_ISAIR ? material.isAir() : material == Material.AIR || material.name().endsWith("_AIR");
            }
        }.runTaskTimer(this, 0, 0);
    }

    @Override
    public List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final String[] args) {
        if (args.length == 0) {
            return Arrays.asList("entities", "blocks", "items");
        } else if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("entities", "blocks", "items"), new ArrayList<>());
        }
        return Collections.emptyList();
    }

    private enum Version {

        SANE((sanePackage, stupidName, saneName) -> Class.forName(sanePackage + "." + saneName)),
        STUPID((sanePackage, stupidName, saneName) -> Class.forName(sanePackage + "." + stupidName)),
        REALLY_STUPID((sanePackage, stupidName, saneName) -> NMSUtil.nmsClass(stupidName));

        private final ClassNameSupplier nmsSupplier;
        private final String versionName;

        Version(final ClassNameSupplier nmsSupplier) {
            this.nmsSupplier = nmsSupplier;
            this.versionName = name().toLowerCase(Locale.ROOT).replace("_", " ");
        }
    }

    @FunctionalInterface
    private interface ClassNameSupplier {

        /**
         * Returns the nms class given by the stupid or sane name.
         *
         * @param sanePackage name of the sane package
         * @param stupidName  {@link Version#STUPID} name of the class
         * @param saneName    {@link Version#SANE} name of the class
         * @return nms class
         */
        Class<?> clazz(String sanePackage, String stupidName, String saneName) throws ClassNotFoundException;
    }
}
