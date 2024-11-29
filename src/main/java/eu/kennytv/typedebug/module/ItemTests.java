package eu.kennytv.typedebug.module;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public final class ItemTests {

    private final List<ItemAndKey> items = new ArrayList<>();

    public ItemTests() throws IOException {
        // Load items.mcfunction file froom root dir.
        // See https://github.com/kennytv/ItemDataComponentExamples
        Files.readString(Path.of("items.mcfunction")).lines().forEach(line -> {
            final String command = line.trim();
            if (!command.startsWith("give @s ")) {
                return;
            }

            final String itemPart = command.substring("give @s ".length());
            final int keyStart = itemPart.indexOf('[');
            final int keyEnd = itemPart.indexOf('=');
            final String key = itemPart.substring(keyStart + 1, keyEnd);
            final ItemStack itemStack = Bukkit.getItemFactory().createItemStack(itemPart);
            this.items.add(new ItemAndKey(key, itemStack));
        });
    }

    public record ItemAndKey(String key, ItemStack item) {
    }

    public List<ItemAndKey> items() {
        return items;
    }
}
