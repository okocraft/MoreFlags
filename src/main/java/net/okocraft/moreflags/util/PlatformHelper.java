package net.okocraft.moreflags.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class PlatformHelper {

    private static final boolean FOLIA;

    static {
        boolean folia;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }

        FOLIA = folia;
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
        return FOLIA ? Bukkit.isOwnedByCurrentRegion(entity) : Bukkit.isPrimaryThread();
    }

    private PlatformHelper() {
        throw new UnsupportedOperationException();
    }
}
