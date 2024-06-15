package eu.kennytv.typedebug;

import eu.kennytv.typedebug.module.ExtraTests;
import eu.kennytv.typedebug.module.ParticleTest;
import eu.kennytv.typedebug.module.TranslationTest;
import eu.kennytv.typedebug.util.BlockEntities;
import eu.kennytv.typedebug.util.NMSUtil;
import eu.kennytv.typedebug.util.Version;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static eu.kennytv.typedebug.util.ReflectionUtil.has;
import static org.bukkit.util.StringUtil.copyPartialMatches;

public final class TypeDebugPlugin extends JavaPlugin implements Listener {

    private static final List<String> TESTS = Arrays.asList("entities", "blocks", "blockentities", "blocksbutinbad", "items", "particles", "cloud", "translations", "extra");
    private static final List<String> COMPLETIONS = Arrays.asList("reload", "run", "pause");
    private static final boolean HAS_ITEM_GETKEY = has(Item.class, "getKey");
    private static final boolean HAS_MATERIAL_ISAIR = has(Material.class, "isAir");
    private static final boolean HAS_ENTITY_SETGRAVITY = has(Entity.class, "setGravity", boolean.class);
    private static final boolean HAS_ENTITY_SETINVULNERABLE = has(Entity.class, "setInvulnerable", boolean.class);
    private static final Version VERSION;
    private final Settings settings = new Settings(this);
    private final ExtraTests extraTests = new ExtraTests(this);
    private boolean pause;

    static {
        if (has("net.minecraft.world.level.block.state.BlockState")) {
            VERSION = Version.SANE;
        } else if (has("net.minecraft.world.level.block.state.IBlockData")) {
            VERSION = Version.STUPID;
        } else {
            VERSION = Version.REALLY_STUPID;
        }
    }

    @Override
    public void onEnable() {
        getCommand("start").setExecutor(this);
        saveDefaultConfig();

        settings.load();

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Congratulations, you are running a " + VERSION.versionName() + " server version.");
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String s, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Not a player");
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                settings.load();
                sender.sendMessage("Reloaded");
            } else if (args[0].equalsIgnoreCase("pause")) {
                pause = !pause;
                sender.sendMessage("Pause: " + pause);
            } else {
                sender.sendMessage("Usage: /typedebug " + String.join("|", COMPLETIONS));
            }
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /typedebug run " + String.join("|", TESTS));
            return true;
        }

        // Put things into different classes, so they don't get loaded on startup
        final Player player = (Player) sender;
        final String arg = args[1].toLowerCase(Locale.ROOT);
        switch (arg) {
            case "blocks":
                try {
                    setBlocks(player);
                } catch (final ReflectiveOperationException e) {
                    e.printStackTrace();
                }
                break;
            case "blocksbutinbad":
                setBlocksButInBad(player);
                break;
            case "blockentities":
                setBlockEntities(player);
                break;
            case "entities":
                spawnEntities(player);
                break;
            case "items":
                spawnItems(player);
                break;
            case "particles":
                new ParticleTest(player, false).runTaskTimer(this, settings.particleSpawnDelay(), settings.particleSpawnDelay());
                break;
            case "cloud":
                new ParticleTest(player, true).runTaskTimer(this, settings.particleSpawnDelay(), settings.particleSpawnDelay());
                break;
            case "translations":
                TranslationTest.run(player);
                break;
            case "extras":
                extraTests.run(player);
                break;
            default:
                return false;
        }
        return true;
    }

    private void itemData(final Player player) {
        // TODO One item for each item data component
    }

    private void setBlocks(final Player player) throws ReflectiveOperationException {
        final World world = player.getWorld();
        final Location location = player.getLocation();
        int i = 0;
        boolean forwards = false;
        final int y = location.getBlockY();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        final Class<?> blockClass = VERSION.nmsSupplier().clazz("net.minecraft.world.level.block", "Block", "Block");
        final Class<?> blockStateClass = VERSION.nmsSupplier().clazz("net.minecraft.world.level.block.state", "IBlockData", "BlockState");
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

    private void setBlockEntities(final Player player) {
        new BufferedTask(player, BlockEntities.BLOCK_ENTITY_TYPES.size(), 1) {
            @Override
            protected void test(final int i, final Location location) {
                final Material type = BlockEntities.BLOCK_ENTITY_TYPES.get(i);
                getLogger().info("Placing " + type.name());
                location.getBlock().setType(type, false);
            }
        }.runTaskTimer(this, 3, 3);
    }

    private void spawnEntities(final Player player) {
        final World world = player.getWorld();
        final List<EntityType> types = Arrays.stream(EntityType.values()).filter(EntityType::isSpawnable).filter(type -> !settings.ignoredEntityTypes().contains(type)).collect(Collectors.toList());
        new BufferedTask(player, types.size(), 3) {
            @Override
            protected void test(final int i, final Location location) {
                final EntityType entityType = types.get(i);
                getLogger().info("Spawning " + entityType.name());

                final Entity entity = world.spawnEntity(location, entityType);
                if (HAS_ENTITY_SETGRAVITY) {
                    entity.setGravity(false);
                }
                if (HAS_ENTITY_SETINVULNERABLE) {
                    entity.setInvulnerable(true);
                }
            }
        }.runTaskTimer(this, settings.entitySpawnDelay(), settings.entitySpawnDelay());
    }

    private void spawnItems(final Player player) {
        final World world = player.getWorld();
        final Location location = player.getLocation();
        final Material[] materials = Material.values();
        new BukkitRunnable() {
            private final Item[] lastItems = new Item[settings.itemsPerTick()];
            private int i;

            @Override
            public void run() {
                if (pause) {
                    return;
                }
                if (i != 0) {
                    // Remove last items
                    for (final Item item : lastItems) {
                        player.getInventory().remove(item.getItemStack());
                        item.remove();
                    }
                }

                final Location clone = location.clone();
                for (int j = 0; j < settings.itemsPerTick(); j++) {
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
        }.runTaskTimer(this, settings.itemSpawnDelay(), settings.itemSpawnDelay());
    }

    @Override
    public List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final String[] args) {
        if (args.length == 0) {
            return COMPLETIONS;
        } else if (args.length == 1) {
            return copyPartialMatches(args[0], COMPLETIONS, new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("run")) {
            return copyPartialMatches(args[1], TESTS, new ArrayList<>());
        }
        return Collections.emptyList();
    }

    public boolean isPaused() {
        return pause;
    }
}
