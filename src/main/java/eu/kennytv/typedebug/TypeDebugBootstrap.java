package eu.kennytv.typedebug;

import eu.kennytv.typedebug.util.ReflectionUtil;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

public class TypeDebugBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final @NotNull BootstrapContext context) {
        if (ReflectionUtil.has("io.papermc.paper.registry.data.EnchantmentRegistryEntry")) {
            context.getLogger().info("Registering custom enchantments");
            ClassLoadingFun.registerCustomRegistryEntries(context);
        }
    }

    private static final class ClassLoadingFun {

        private static void registerCustomRegistryEntries(final BootstrapContext context) {
            context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> event.registry().register(
                TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("typedebug:test")),
                builder -> builder.description(Component.text("Pointy"))
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    .anvilCost(1)
                    .maxLevel(25)
                    .weight(10)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
                    .activeSlots(EquipmentSlotGroup.BODY, EquipmentSlotGroup.ARMOR, EquipmentSlotGroup.SADDLE)
            )));
        }
    }
}
