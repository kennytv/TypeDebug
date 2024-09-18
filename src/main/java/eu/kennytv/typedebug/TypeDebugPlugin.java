package eu.kennytv.typedebug;

import eu.kennytv.typedebug.command.BrigadierCommand;
import eu.kennytv.typedebug.command.SpaghetCommand;
import eu.kennytv.typedebug.module.ExtraTests;
import eu.kennytv.typedebug.module.ItemTests;
import eu.kennytv.typedebug.util.BlockEntities;
import eu.kennytv.typedebug.util.BufferedTask;
import eu.kennytv.typedebug.util.NMSUtil;
import eu.kennytv.typedebug.util.Version;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import static eu.kennytv.typedebug.util.ReflectionUtil.has;

public final class TypeDebugPlugin extends JavaPlugin implements Listener {

    private static final boolean HAS_ITEM_GETKEY = has(Item.class, "getKey");
    private static final boolean HAS_MATERIAL_ISAIR = has(Material.class, "isAir");
    private static final boolean HAS_ENTITY_SETGRAVITY = has(Entity.class, "setGravity", boolean.class);
    private static final boolean HAS_ENTITY_SETINVULNERABLE = has(Entity.class, "setInvulnerable", boolean.class);
    private static final Version VERSION;
    private final Settings settings = new Settings(this);
    private final ExtraTests extraTests;
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

    public TypeDebugPlugin() {
        extraTests = VERSION == Version.REALLY_STUPID ? null : new ExtraTests(this);
    }

    @Override
    public void onEnable() {
        if (VERSION == Version.SANE) {
            ItemTests.init();
        }

        saveDefaultConfig();
        settings.load();

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Congratulations, you are running a " + VERSION.versionName() + " server version.");

        if (has("io.papermc.paper.command.brigadier.Commands")) {
            getLogger().info("Registering Brigadier command");
            BrigadierCommand.register(this);
        } else {
            getServer().getCommandMap().register("typedebug", new SpaghetCommand(this));
        }
    }

    public void setBlocks(final Player player) throws ReflectiveOperationException {
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
        for (final Object blockState : (Iterable<?>) blockClass.getDeclaredField(VERSION == Version.SANE ? "BLOCK_STATE_REGISTRY" : "REGISTRY_ID").get(null)) {
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

    public void setBlocksButInBad(final Player player) {
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

    public void setBlockEntities(final Player player) {
        new BufferedTask(player, BlockEntities.BLOCK_ENTITY_TYPES.size(), 1) {
            @Override
            protected void test(final int i, final Location location) {
                final Material type = BlockEntities.BLOCK_ENTITY_TYPES.get(i);
                getLogger().info("Placing " + type.name());
                location.getBlock().setType(type, false);
            }
        }.runTaskTimer(this, 3, 3);
    }

    public void spawnEntities(final Player player) {
        final World world = player.getWorld();
        final List<EntityType> types = Arrays.stream(EntityType.values()).filter(EntityType::isSpawnable).filter(type -> !settings.ignoredEntityTypes().contains(type)).toList();
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

    public void spawnItems(final Player player) {
        final List<ItemTests.ItemAndKey> items = new ArrayList<>();
        for (final Material material : Material.values()) {
            if (material.isItem() && !isAir(material)) {
                final String key = HAS_ITEM_GETKEY ? material.getKey().toString() : material.name();
                items.add(new ItemTests.ItemAndKey(key, new ItemStack(material)));
            }
        }
        spawnItems(player, items, settings.itemsPerTick(), false);
    }

    private boolean isAir(final Material material) {
        return HAS_MATERIAL_ISAIR ? material.isAir() : material == Material.AIR || material.name().endsWith("_AIR");
    }

    public void spawnItems(final Player player, final List<ItemTests.ItemAndKey> items, final int itemsPerTick, final boolean sendAsComponent) {
        final World world = player.getWorld();
        final Location location = player.getLocation();

        new BukkitRunnable() {
            private final Item[] lastItems = new Item[itemsPerTick];
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
                for (int j = 0; j < itemsPerTick; j++) {
                    if (!next(j, clone)) {
                        return;
                    }
                    clone.add(0.45, 0, 0);
                }
            }

            private boolean next(final int j, final Location location) {
                if (i == items.size() || !player.isOnline()) {
                    stop();
                    return false;
                }

                final ItemStack itemStack = items.get(i).item();
                final String key = items.get(i).key();
                getLogger().info("Spawning " + key);
                final Item item = world.dropItem(location, itemStack);
                item.setGravity(false);
                item.setInvulnerable(true);

                lastItems[j] = item;
                player.getInventory().addItem(itemStack);

                if (sendAsComponent) {
                    player.sendMessage(Component.text().content(key).hoverEvent(itemStack.asHoverEvent()));
                }

                i++;
                return true;
            }

            private void stop() {
                cancel();
                getLogger().info("Done!");
            }


        }.runTaskTimer(this, settings.itemSpawnDelay(), settings.itemSpawnDelay());
    }

    public void togglePause(final CommandSender sender) {
        pause = !pause;
        sender.sendMessage("Pause: " + pause);
    }

    public boolean isPaused() {
        return pause;
    }

    public Settings settings() {
        return settings;
    }

    public ExtraTests extraTests() {
        return extraTests;
    }
}
