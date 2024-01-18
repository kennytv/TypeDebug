package eu.kennytv.typedebug.util;

@FunctionalInterface
public interface ClassNameSupplier {

    /**
     * Returns the nms class given by the stupid or sane name.
     *
     * @param sanePackage name of the sane package
     * @param stupidName  {@link Version#STUPID} name of the class
     * @param saneName    {@link Version#SANE} name of the class
     * @return nms class
     */
    Class<?> clazz(String sanePackage, String stupidName, String saneName) throws ClassNotFoundException;
}