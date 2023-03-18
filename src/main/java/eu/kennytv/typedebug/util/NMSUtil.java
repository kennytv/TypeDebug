package eu.kennytv.typedebug.util;

import org.bukkit.Bukkit;

public final class NMSUtil {

    private static final String CRAFTBUKKIT = Bukkit.getServer().getClass().getPackage().getName() + ".";
    private static final String NMS = CRAFTBUKKIT.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public static Class<?> cbClass(final String className) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT + className);
    }

    public static Class<?> nmsClass(final String className) throws ClassNotFoundException {
        return Class.forName(NMS + className);
    }
}
