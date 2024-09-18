package eu.kennytv.typedebug.util;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public final class ComponentUtil {

    public static void sendItemHover(final CommandSender sender, final ItemStack itemStack, final String content) {
        sender.sendMessage(Component.text().content(content).hoverEvent(itemStack.asHoverEvent()));
    }
}
