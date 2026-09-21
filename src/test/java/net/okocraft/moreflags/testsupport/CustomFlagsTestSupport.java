package net.okocraft.moreflags.testsupport;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.okocraft.moreflags.CustomFlags;
import org.mockito.Mockito;

public final class CustomFlagsTestSupport {

    private static boolean initialized;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        WorldGuard worldGuard = Mockito.mock(WorldGuard.class);
        FlagRegistry registry = Mockito.mock(FlagRegistry.class);
        Mockito.when(worldGuard.getFlagRegistry()).thenReturn(registry);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);
            try {
                Class.forName(CustomFlags.class.getName());
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }

        initialized = true;
    }

    private CustomFlagsTestSupport() {
        throw new UnsupportedOperationException();
    }
}
