package eu.kennytv.typedebug.module;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.JukeboxSong;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import static eu.kennytv.typedebug.util.ReflectionUtil.has;
import static net.kyori.adventure.text.Component.text;

public final class ItemTests {

    public static final List<ItemAndKey> ITEMS = new ArrayList<>();

    public static void init() { // Method instead of constructor to avoid class loading blowing up on errors
        // Mostly one item per item data component type to have clear-cut disconnects/errors
        meta("custom_data", Material.STICK, meta -> {
            final PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(NamespacedKey.fromString("aaaa"), PersistentDataType.BOOLEAN, true);
        });
        meta("max_stack_size", Material.STICK, meta -> meta.setMaxStackSize(99));
        meta("max_damage", Material.GOLDEN_SWORD, Damageable.class, meta -> meta.setMaxDamage(149));
        meta("damage", Material.GOLDEN_SWORD, Damageable.class, meta -> meta.setDamage(5));
        meta("unbreakable", Material.GOLDEN_SWORD, meta -> meta.setUnbreakable(true));
        meta("custom_name", Material.STICK, meta -> meta.displayName(text("Display name")));
        meta("item_name", Material.STICK, meta -> meta.itemName(text("Literal name")));
        // TODO item model
        meta("lore", Material.STICK, meta -> meta.lore(List.of(text("Lore"), text("Second line"))));
        meta("rarity", Material.STICK, meta -> meta.setRarity(ItemRarity.EPIC));
        meta("enchantments", Material.STICK, meta -> meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, false));
        // TODO can_place_on, can_break
        meta("attribute_modifiers", Material.STICK, meta -> {
            try {
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(NamespacedKey.fromString("aaa"), 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
            } catch (final NoSuchMethodError ignored) {
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "test", 10, AttributeModifier.Operation.ADD_NUMBER));
            }
        });
        meta("custom_model_data", Material.STICK, meta -> meta.setCustomModelData(1));
        meta("hide_additional_tooltip", Material.STICK, meta -> meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));
        meta("hide_tooltip", Material.STICK, meta -> meta.setHideTooltip(true));
        meta("repair_cost", Material.STICK, Repairable.class, meta -> meta.setRepairCost(5));
        // TODO creative_slot_lock
        meta("enchantment_glint_override", Material.STICK, meta -> meta.setEnchantmentGlintOverride(true));
        // TODO intangible_projectile
        meta("food", Material.STICK, meta -> {
            final FoodComponent food = meta.getFood();
            food.setCanAlwaysEat(true);
            food.setNutrition(1);
            food.setSaturation(1);
            food.setEatSeconds(1);
            food.addEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20, 1, false, false, true), 1);
            meta.setFood(food);
        });
        // TODO consumable
        // TODO use_remainder
        // TODO use_cooldown
        // TODO damage_resistant
        if (has("org.bukkit.inventory.meta.components.ToolComponent")) {
            meta("tool", Material.STICK, meta -> {
                final ToolComponent tool = meta.getTool();
                tool.setDamagePerBlock(1);
                tool.setDefaultMiningSpeed(1);
                tool.addRule(Material.ANDESITE, 1F, true);
                meta.setTool(tool);
            });
        }
        // TODO enchantable
        // TODO equippable
        // TODO repairable
        // TODO glider
        // TODO tooltip_style
        // TODO death_protection
        meta("stored_enchantments", Material.ENCHANTED_BOOK, EnchantmentStorageMeta.class, meta -> meta.addStoredEnchant(Enchantment.AQUA_AFFINITY, 1, false));
        meta("dyed_color", Material.LEATHER_CHESTPLATE, ColorableArmorMeta.class, meta -> meta.setColor(Color.AQUA));
        // All map related data in one item // TODO map_deocrations
        meta("map related data", Material.MAP, MapMeta.class, meta -> {
            final MapView map = Bukkit.createMap(Bukkit.getWorlds().get(0));
            map.setScale(MapView.Scale.FAR);
            meta.setScaling(true);
            meta.setColor(Color.AQUA);
            meta.setMapView(map);
        });
        meta("charged_projectiles", Material.CROSSBOW, CrossbowMeta.class, meta -> meta.addChargedProjectile(new ItemStack(Material.ARROW)));
        meta("bundle_contents", Material.BUNDLE, BundleMeta.class, meta -> meta.addItem(new ItemStack(Material.ARROW)));
        meta("potion_contents", Material.BUNDLE, PotionMeta.class, meta -> {
            meta.setColor(Color.AQUA);
            meta.setBasePotionType(PotionType.HARMING);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20, 1, false, false, true), true);
            // TODO custom name
        });
        meta("suspicious_stew_effects", Material.SUSPICIOUS_STEW, SuspiciousStewMeta.class, meta -> meta.addCustomEffect(SuspiciousEffectEntry.create(PotionEffectType.ABSORPTION, 20), true));
        meta("writable_book_content", Material.WRITABLE_BOOK, BookMeta.class, meta -> meta.addPages(text("Page 1"), text("Page 2")));
        meta("written_book_content", Material.WRITTEN_BOOK, BookMeta.class, meta -> {
            meta.addPages(text("Page 1"), text("Page 2"));
            meta.setAuthor("Author");
            meta.setTitle("Title");
            meta.setGeneration(BookMeta.Generation.COPY_OF_ORIGINAL);
        });
        meta("trim", Material.LEATHER_CHESTPLATE, ArmorMeta.class, meta -> meta.setTrim(new ArmorTrim(TrimMaterial.GOLD, TrimPattern.COAST)));
        // TODO debug_stick_state
        // TODO entity_data, bucket_entity_data, block_entity_data
        meta("instrument", Material.GOAT_HORN, MusicInstrumentMeta.class, meta -> meta.setInstrument(MusicInstrument.SEEK_GOAT_HORN));
        meta("ominous_bottle_amplifier", Material.OMINOUS_BOTTLE, OminousBottleMeta.class, meta -> meta.setAmplifier(1));
        if (has("org.bukkit.inventory.meta.components.JukeboxPlayableComponent")) {
            meta("jukebox_playable", Material.MUSIC_DISC_11, meta -> {
                final JukeboxPlayableComponent jukeboxPlayable = meta.getJukeboxPlayable();
                jukeboxPlayable.setSong(JukeboxSong.BLOCKS);
                meta.setJukeboxPlayable(jukeboxPlayable);
            });
        }
        meta("recipes", Material.KNOWLEDGE_BOOK, KnowledgeBookMeta.class, meta -> meta.addRecipe(new NamespacedKey("test", "recipe")));
        meta("lodestone_tracker", Material.COMPASS, CompassMeta.class, meta -> {
            meta.setLodestone(new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
            meta.setLodestoneTracked(true);
        });
        meta("firework_explosion", Material.FIREWORK_STAR, FireworkEffectMeta.class, meta -> meta.setEffect(FireworkEffect.builder().flicker(true).withColor(Color.AQUA).trail(true).withFade(Color.AQUA).build()));
        meta("fireworks", Material.FIREWORK_ROCKET, FireworkMeta.class, meta -> {
            meta.addEffect(FireworkEffect.builder().flicker(true).withColor(Color.AQUA).trail(true).withFade(Color.AQUA).build());
            meta.setPower(2);
        });
        meta("profile", Material.PLAYER_HEAD, SkullMeta.class, meta -> meta.setPlayerProfile(Bukkit.getOnlinePlayers().iterator().next().getPlayerProfile()));
        meta("note_block_sound", Material.PLAYER_HEAD, SkullMeta.class, meta -> meta.setNoteBlockSound(NamespacedKey.fromString("test:note_block_sound")));
        meta("banner_patterns", Material.BLACK_BANNER, BannerMeta.class, meta -> {
            meta.addPattern(new Pattern(DyeColor.RED, PatternType.BORDER));
            meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.CREEPER));
        });
        if (has("org.bukkit.inventory.meta.ShieldMeta")) {
            meta("base_color", Material.SHIELD, ShieldMeta.class, meta -> meta.setBaseColor(DyeColor.GREEN));
        }
        // TODO pot_decorations
        // TODO container
        // TODO block_state
        // TODO bees
        // TODO container_loot

        // TODO Send items as chat message
    }

    private static void meta(final String key, final Material type, final Consumer<ItemMeta> metaConsumer) {
        final ItemStack item = new ItemStack(type);
        try {
            item.editMeta(metaConsumer);
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        ITEMS.add(new ItemAndKey(key, item));
    }

    private static <T extends ItemMeta> void meta(final String key, final Material type, final Class<T> metaClass, final Consumer<T> metaConsumer) {
        final ItemStack item = new ItemStack(type);
        try {
            item.editMeta(metaClass, metaConsumer);
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        ITEMS.add(new ItemAndKey(key, item));
    }

    public record ItemAndKey(String key, ItemStack item) {
    }
}
