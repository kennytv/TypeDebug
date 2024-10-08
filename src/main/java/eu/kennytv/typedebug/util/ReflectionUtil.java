package eu.kennytv.typedebug.util;

public final class ReflectionUtil {

    public static boolean has(final Class<?> clazz, final String method, final Class<?>... args) {
        try {
            clazz.getDeclaredMethod(method, args);
            return true;
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean has(final String clazz, final String method, final Class<?>... args) {
        try {
            return has(Class.forName(clazz), method, args);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean has(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
