package eu.kennytv.typedebug.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;

public final class BlockEntities {

    public static final List<Material> BLOCK_ENTITY_TYPES = new ArrayList<>();
    private static final String[] BLOCK_ENTITIES = {
            "furnace",
            "chest",
            "trapped_chest",
            "ender_chest",
            "jukebox",
            "dispenser",
            "dropper",
            "oak_sign", // for sign
            "oak_hanging_sign", // for hanging_sign
            "spawner", // for mob_spawner
            "piston",
            "brewing_stand",
            "enchanting_table",
            "end_portal",
            "beacon",
            "player_head", // as skull
            "daylight_detector",
            "hopper",
            "comparator",
            "red_banner", // for banner
            "structure_block",
            "end_gateway",
            "command_block",
            "shulker_box",
            "red_bed", // as bed
            "conduit",
            "barrel",
            "smoker",
            "blast_furnace",
            "lectern",
            "bell",
            "jigsaw",
            "campfire",
            "beehive",
            "sculk_sensor",
            "calibrated_sculk_sensor",
            "sculk_catalyst",
            "sculk_shrieker",
            "chiseled_bookshelf",
            "suspicious_sand", // as brushable_block
            "decorated_pot",
            "crafter",
            "trial_spawner"
    };

    static {
        // There is no API representation for block entities, so we have to collect them manually
        for (final String blockEntity : BLOCK_ENTITIES) {
            final Material material = Material.getMaterial(blockEntity.toUpperCase(Locale.ROOT));
            if (material == null) {
                System.err.println("Unknown block entity: " + blockEntity);
                continue;
            }

            BLOCK_ENTITY_TYPES.add(material);
        }
    }
}
