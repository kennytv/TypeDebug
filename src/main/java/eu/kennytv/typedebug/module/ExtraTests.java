package eu.kennytv.typedebug.module;

import eu.kennytv.typedebug.util.BufferedTask;
import eu.kennytv.typedebug.TypeDebugPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WorldBorder;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Sign;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("deprecation")
public final class ExtraTests {
    private final List<PacketTest> tests = new ArrayList<>();
    private final TypeDebugPlugin plugin;

    public ExtraTests(final TypeDebugPlugin plugin) {
        this.plugin = plugin;
        addTest("award_stats", player -> {
            player.incrementStatistic(Statistic.AVIATE_ONE_CM, 5);
            player.setStatistic(Statistic.MINE_BLOCK, Material.ACACIA_FENCE, 5);
            player.setStatistic(Statistic.KILL_ENTITY, EntityType.WITCH, 6);
            player.decrementStatistic(Statistic.KILL_ENTITY, EntityType.WITCH, 5);
        });
        addTest("block_entity_data", player -> {
            final Location location = player.getLocation().add(0, 2, 0);
            location.getWorld().setType(location, Material.OAK_SIGN);
            final Sign state = (Sign) location.getBlock().getState();
            state.setLine(0, "Hello");
            state.update();
        });
        addTest("open_sign_editor", player -> {
            final Location location = player.getLocation().add(0, 2, 0);
            final Sign sign = (Sign) location.getBlock().getState();
            player.openSign(sign);
            player.closeInventory();
        });
        addTest("titles", player -> {
            player.sendTitle("Wee", "Woo", 10, 10, 10);
            player.setTitleTimes(5, 5, 5);
            player.resetTitle();
        });
        addTest("container", player -> {
            final Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST);
            inventory.setContents(new ItemStack[]{new ItemStack(Material.ACACIA_BOAT), new ItemStack(Material.DIAMOND_AXE)});
            player.openInventory(inventory);
            inventory.setContents(new ItemStack[]{new ItemStack(Material.DIAMOND_AXE), new ItemStack(Material.ACACIA_BOAT)});
            inventory.setItem(5, new ItemStack(Material.COAL));
            player.closeInventory();
        });
        addTest("cooldown", player -> {
            player.setCooldown(Material.STONE, 10);
            player.setCooldown(Material.DIRT, 20);
        });
        addTest("set_health", player -> {
            player.setHealth(18);
            player.setHealthScaled(true);
            player.setHealthScaled(false);
        });
        addTest("damage", player -> {
            player.damage(2, DamageSource.builder(DamageType.CACTUS).build());
        });
        addTest("chat", player -> {
            player.sendMessage("Woooooo");
        });
        addTest("block/entity_event", player -> {
            player.playEffect(EntityEffect.LOVE_HEARTS);
            player.playEffect(player.getLocation(), Effect.EXTINGUISH, null);
        });
        addTest("explode", player -> {
            player.getWorld().createExplosion(player.getLocation().add(3, 1, 3), 2);
        });
        addTest("border", player -> {
            final WorldBorder border = player.getWorld().getWorldBorder();
            border.setCenter(player.getLocation());
            border.setSize(10);
            border.setWarningDistance(5);
            border.setWarningTime(5);
            border.setDamageAmount(5);
            border.setSize(5, TimeUnit.SECONDS, 1);
            border.reset();
        });
        addTest("merchant_offers", player -> {
            final Location location = player.getLocation().add(5, 1, 5);
            final WanderingTrader villager = (WanderingTrader) location.getWorld().spawnEntity(location, EntityType.WANDERING_TRADER);
            player.openMerchant(villager, true);
            player.closeInventory();
        });
        addTest("open_book", player -> {
            final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            final BookMeta bookMeta = (BookMeta) book.getItemMeta();
            bookMeta.setTitle("Test Book");
            bookMeta.setAuthor("Test author");
            bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
            bookMeta.addPage("Woooo");
            book.setItemMeta(bookMeta);
            player.openBook(book);
            player.closeInventory();
        });
        addTest("player_abilities", player -> {
            player.setAllowFlight(true);
        });
        addTest("player_info", player -> {
            player.setPlayerListName("Wooo");
        });
        addTest("mob_effect", player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20, 1, true, true));
            player.removePotionEffect(PotionEffectType.ABSORPTION);
        });
        addTest("set_entity_link", player -> {
            final Location location = player.getLocation().add(-3, 0, -3);
            final LivingEntity chicken = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.CHICKEN);
            chicken.setLeashHolder(player);
        });
        addTest("set_experience", player -> {
            player.setExp(0.5f);
            player.setLevel(5);
            player.setTotalExperience(100);
        });
        addTest("set_passengers", player -> {
            final Location location = player.getLocation().add(-3, 0, -3);
            final LivingEntity chicken = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.CHICKEN);
            player.addPassenger(chicken);
            player.removePassenger(chicken);
        });
        addTest("sound", player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            player.playSound(player, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            player.stopSound(Sound.BLOCK_ANVIL_PLACE);
        });
        addTest("update_advancements", player -> {
            final Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("story/mine_stone"));
            final AdvancementProgress progress = player.getAdvancementProgress(advancement);
            progress.awardCriteria("stone");
            progress.revokeCriteria("stone");
        });
    }

    private void addTest(final String name, final Consumer<Player> runnable) {
        tests.add(new PacketTest(name, runnable));
    }

    public void run(final Player player) {
        new BufferedTask(player, tests.size(), 0) {

            @Override
            protected void test(final int i, final Location location) {
                final PacketTest test = tests.get(i);
                plugin.getLogger().info("Running test: " + test.name());
                try {
                    test.runnable().accept(player);
                } catch (final Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to run test: " + test.name(), e);
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private record PacketTest(String name, Consumer<Player> runnable) {
    }
}
