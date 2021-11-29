package eu.kennytv.blockdebug;

import org.bukkit.Bukkit;

public final class NMSUtil {

    private static final String CRAFTBUKKIT;
    private static final String NMS;
    private static final String SERVER_VERSION;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        SERVER_VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
        CRAFTBUKKIT = "org.bukkit.craftbukkit." + SERVER_VERSION + '.';
        NMS = "net.minecraft.server." + SERVER_VERSION + '.';
    }

    public static Class<?> cbClass(final String className) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT + className);
    }

    public static Class<?> nmsClass(final String className) throws ClassNotFoundException {
        return Class.forName(NMS + className);
    }
}
