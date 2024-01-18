package eu.kennytv.typedebug.util;

import java.util.Locale;

public enum Version {

    SANE((sanePackage, stupidName, saneName) -> Class.forName(sanePackage + "." + saneName)),
    STUPID((sanePackage, stupidName, saneName) -> Class.forName(sanePackage + "." + stupidName)),
    REALLY_STUPID((sanePackage, stupidName, saneName) -> NMSUtil.nmsClass(stupidName));

    private final ClassNameSupplier nmsSupplier;
    private final String versionName;

    Version(final ClassNameSupplier nmsSupplier) {
        this.nmsSupplier = nmsSupplier;
        this.versionName = name().toLowerCase(Locale.ROOT).replace("_", " ");
    }

    public ClassNameSupplier nmsSupplier() {
        return nmsSupplier;
    }

    public String versionName() {
        return versionName;
    }
}