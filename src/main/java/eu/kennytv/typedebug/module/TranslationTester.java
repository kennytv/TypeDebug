package eu.kennytv.typedebug.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public final class TranslationTester {

    public static void test(final Player player) {
        final JsonObject object;
        try (final InputStreamReader reader = new InputStreamReader(Bukkit.getServer().getClass().getResourceAsStream("/assets/minecraft/lang/en_us.json"))) {
            object = new GsonBuilder().create().fromJson(reader, JsonObject.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final Location location = player.getLocation();
        final World world = location.getWorld();
        int i = 0;
        boolean forwards = true;
        for (final String translationKey : object.keySet()) {
            if (i++ == 60) {
                // Next row
                i = 0;
                forwards = !forwards;
                location.add(0, 0, 2);
            }

            world.spawn(location, ArmorStand.class, entity -> {
                entity.setAI(false);
                entity.setMarker(true);
                entity.setBasePlate(false);
                entity.setArms(false);
                entity.setCanTick(false);
                entity.setSmall(true);
                entity.setGravity(false);
                entity.customName(Component.translatable(translationKey));
                entity.setCustomNameVisible(true);
            });

            // Step sideways
            location.add(forwards ? 1.5 : -1.5, 0, 0);
        }
    }
}
