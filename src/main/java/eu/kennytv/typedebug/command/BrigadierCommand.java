package eu.kennytv.typedebug.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.kennytv.typedebug.TypeDebugPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
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
            commands.register(cmd.then(literal("pause")).executes(ctx -> {
                plugin.togglePause(ctx.getSource().getSender());
                return Command.SINGLE_SUCCESS;
            }).build(), "Pause current run tasks");

            commands.register(cmd.then(literal("reload")).executes(ctx -> {
                plugin.reloadConfig();
                ctx.getSource().getSender().sendMessage("Reloaded config");
                return Command.SINGLE_SUCCESS;
            }).build(), "Reload the config");

            commands.register(cmd.then(literal("run").then(argument("task", word()).suggests((context, builder) -> {
                // Surely there has to be a helper method for this somewhere
                for (final String s : TypeDebugPlugin.TESTS) {
                    if (StringUtil.startsWithIgnoreCase(s, builder.getRemainingLowerCase())) {
                        builder.suggest(s);
                    }
                }
                return builder.buildFuture();
            }).executes(ctx -> {
                final String task = ctx.getArgument("task", String.class);
                return SpaghetCommand.startTask(task, (Player) ctx.getSource().getSender()) ? Command.SINGLE_SUCCESS : -1;
            }))).build(), "Run a task");
        });
    }
}
