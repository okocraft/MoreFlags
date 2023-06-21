package net.okocraft.moreflags.util;

import net.okocraft.moreflags.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlatformHelper {

    private static final boolean ENTITY_SCHEDULER;
    private static final boolean FOLIA;

    static {
        boolean entityScheduler;

        try {
            Entity.class.getDeclaredMethod("getScheduler");
            entityScheduler = true;
        } catch (NoSuchMethodException ignored) {
            entityScheduler = false;
        }

        ENTITY_SCHEDULER = entityScheduler;

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

    public static <E extends Entity> void runEntityTask(@NotNull E entity, @NotNull Consumer<E> task, long delay) {
        var runnable = toRunnable(entity, task);

        if (ENTITY_SCHEDULER) {
            entity.getScheduler().runDelayed(plugin(), $ -> runnable.run(), null, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin(), runnable, delay);
        }
    }

    private static <E extends Entity> @NotNull Runnable toRunnable(@NotNull E entity, @NotNull Consumer<E> task) {
        return () -> {
            if (entity.isValid()) {
                task.accept(entity);
            }
        };
    }

    private static @NotNull Plugin plugin() {
        return JavaPlugin.getPlugin(Main.class);
    }

    private PlatformHelper() {
        throw new UnsupportedOperationException();
    }
}
