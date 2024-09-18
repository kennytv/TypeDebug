package eu.kennytv.typedebug.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.kennytv.typedebug.TypeDebugPlugin;
import eu.kennytv.typedebug.module.ItemTests;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class BrigadierCommand {

    public static void register(final TypeDebugPlugin plugin) {
        // Could just leave it with the bukkit command, but this is more fun (pain)
        final LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            final LiteralArgumentBuilder<CommandSourceStack> cmd = literal("start").requires(stack -> stack.getSender().hasPermission("typedebug.command"));
            commands.register(cmd.then(literal("pause").executes(ctx -> {
                plugin.togglePause(ctx.getSource().getSender());
                return Command.SINGLE_SUCCESS;
            })).build(), "Pause current run tasks");

            commands.register(cmd.then(literal("reload").executes(ctx -> {
                plugin.reloadConfig();
                ctx.getSource().getSender().sendMessage("Reloaded config");
                return Command.SINGLE_SUCCESS;
            })).build(), "Reload the config");

            commands.register(cmd.then(literal("item").then(argument("Item data component", word())
                .suggests((context, builder) -> suggest(builder, ItemTests.ITEMS.stream().map(ItemTests.ItemAndKey::key).toList()))
                .executes(ctx -> {
                    final String type = ctx.getArgument("Item data component", String.class);
                    final ItemTests.ItemAndKey item = ItemTests.ITEMS.stream().filter(i -> i.key().equalsIgnoreCase(type)).findAny().orElse(null);
                    if (item == null) {
                        ctx.getSource().getSender().sendMessage("No item with data type " + type);
                        return -1;
                    }

                    final Player player = (Player) ctx.getSource().getSender();
                    player.getInventory().addItem(item.item());
                    ctx.getSource().getSender().sendMessage("Gave item with data type " + type);
                    return Command.SINGLE_SUCCESS;
                }))).build(), "Give an item with data");

            commands.register(cmd.then(literal("run")
                .then(
                    argument("task", word())
                        .suggests((context, builder) -> suggest(builder, TypeDebugPlugin.TESTS))
                        .executes(ctx -> run(ctx, false))
                        .then(
                            argument("extra", StringArgumentType.greedyString()).executes(ctx -> run(ctx, true))
                        )
                )).build(), "Run a task");
        });
    }

    private static CompletableFuture<Suggestions> suggest(final SuggestionsBuilder builder, final Collection<String> completions) {
        // Surely there has to be a helper method for this somewhere
        for (final String s : completions) {
            if (StringUtil.startsWithIgnoreCase(s, builder.getRemainingLowerCase())) {
                builder.suggest(s);
            }
        }
        return builder.buildFuture();
    }

    private static int run(final CommandContext<CommandSourceStack> ctx, final boolean withExtra) {
        final String task = ctx.getArgument("task", String.class);
        // Just pass the extra args raw because lazy
        final String[] extraArgs = withExtra ? ctx.getArgument("extra", String.class).split(" ") : new String[0];
        return SpaghetCommand.startTask(task, extraArgs, (Player) ctx.getSource().getSender()) ? Command.SINGLE_SUCCESS : -1;
    }
}
