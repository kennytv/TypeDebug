package eu.kennytv.typedebug.command;

import eu.kennytv.typedebug.TypeDebugPlugin;
import eu.kennytv.typedebug.module.ItemTests;
import eu.kennytv.typedebug.module.ParticleTest;
import eu.kennytv.typedebug.module.TranslationTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.util.StringUtil.copyPartialMatches;

public final class SpaghetCommand extends Command {

    static final List<String> TESTS = Arrays.asList("entities", "blocks", "blockentities", "items", "itemswithdata", "particles", "cloud", "translations", "extra");
    private static final TypeDebugPlugin PLUGIN = TypeDebugPlugin.getPlugin(TypeDebugPlugin.class);
    private static final List<String> COMPLETIONS = List.of("reload", "run", "pause", "printitem");
    private final TypeDebugPlugin plugin;

    public SpaghetCommand(final TypeDebugPlugin plugin) {
        super("start");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String s, final @NotNull String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage("Not a player");
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.settings().load();
                sender.sendMessage("Reloaded");
            } else if (args[0].equalsIgnoreCase("pause")) {
                plugin.togglePause(sender);
            } else if (args[0].equalsIgnoreCase("printitem")) {
                PrintItem.printItem(player);
            } else {
                sender.sendMessage("Usage: /start run " + String.join("|", COMPLETIONS));
            }
            return true;
        } else if (args.length == 0) {
            sender.sendMessage("Usage: /start run " + String.join("|", TESTS));
            return true;
        }

        // Copy args array starting with index 2
        final String[] extra = new String[args.length - 2];
        System.arraycopy(args, 2, extra, 0, extra.length);
        if (!startTask(args[1].toLowerCase(Locale.ROOT), extra, player)) {
            sender.sendMessage("Usage: /start run " + String.join("|", TESTS));
            return false;
        }
        return true;
    }

    static boolean startTask(final String name, final String[] extra, final Player player) {
        // Put things into different classes, so they don't get loaded on startup
        switch (name) {
            case "blocks":
                try {
                    PLUGIN.setBlocks(player);
                } catch (final ReflectiveOperationException e) {
                    player.sendMessage("Falling back to bad/old method");
                    PLUGIN.setBlocksButInBad(player);
                }
                break;
            case "blockentities":
                PLUGIN.setBlockEntities(player);
                break;
            case "entities":
                PLUGIN.spawnEntities(player);
                break;
            case "items":
                PLUGIN.spawnItems(player);
                break;
            case "itemswithdata":
                PLUGIN.spawnItems(player, ItemTests.ITEMS, 1, true);
                break;
            case "particles":
                new ParticleTest(player, false).runTaskTimer(PLUGIN, PLUGIN.settings().particleSpawnDelay(), PLUGIN.settings().particleSpawnDelay());
                break;
            case "cloud":
                new ParticleTest(player, true).runTaskTimer(PLUGIN, PLUGIN.settings().particleSpawnDelay(), PLUGIN.settings().particleSpawnDelay());
                break;
            case "translations":
                TranslationTest.run(player);
                break;
            case "extras":
                PLUGIN.extraTests().run(player);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final String[] args) {
        if (args.length == 0) {
            return COMPLETIONS;
        } else if (args.length == 1) {
            return copyPartialMatches(args[0], COMPLETIONS, new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("run")) {
            return copyPartialMatches(args[1], TESTS, new ArrayList<>());
        }
        return Collections.emptyList();
    }

    @Override
    public String getPermission() {
        return "typedebug.command";
    }

    // Class loading!
    private static final class PrintItem {

        private static void printItem(final Player player) {
            final ItemStack item = player.getInventory().getItemInMainHand();
            player.sendMessage(Component.text().content("Hover here").hoverEvent(item.asHoverEvent()));
        }
    }
}
