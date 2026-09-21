package net.okocraft.moreflags.handler;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.Session;
import net.kyori.adventure.key.Key;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.testsupport.CustomFlagsTestSupport;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

class TeleportToSpawnFlagHandlerTest {

    @BeforeAll
    static void initializeCustomFlags() {
        CustomFlagsTestSupport.initialize();
    }

    @Test
    void testDoesNothingWithoutDestinationFlag() {
        TeleportToSpawnFlagHandler handler = new TeleportToSpawnFlagHandler(Mockito.mock(Session.class));
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        Location from = Mockito.mock(Location.class);
        Location to = Mockito.mock(Location.class);
        ApplicableRegionSet toSet = Mockito.mock(ApplicableRegionSet.class);
        Mockito.when(toSet.queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY)).thenReturn(null);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class);
             var mockedBukkit = Mockito.mockStatic(Bukkit.class)) {
            Assertions.assertTrue(handler.onCrossBoundary(player, from, to, toSet, Set.of(), Set.of(), null));
            mockedWorldGuard.verifyNoInteractions();
            mockedBukkit.verifyNoInteractions();
        }
    }

    @Test
    void testDoesNotTeleportWhenFlagValueIsUnchanged() {
        TeleportToSpawnFlagHandler handler = new TeleportToSpawnFlagHandler(Mockito.mock(Session.class));
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        Location from = Mockito.mock(Location.class);
        Location to = Mockito.mock(Location.class);
        ApplicableRegionSet toSet = Mockito.mock(ApplicableRegionSet.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(toSet.queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY)).thenReturn("minecraft:world");
        Mockito.when(worldGuard.getPlatform().getRegionContainer().createQuery()
                .getApplicableRegions(from).queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY))
                .thenReturn("minecraft:world");

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class);
             var mockedBukkit = Mockito.mockStatic(Bukkit.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);

            Assertions.assertTrue(handler.onCrossBoundary(player, from, to, toSet, Set.of(), Set.of(), null));
            mockedBukkit.verifyNoInteractions();
        }
    }

    @Test
    void testChangedFlagValueLooksUpDestinationWorld() {
        TeleportToSpawnFlagHandler handler = new TeleportToSpawnFlagHandler(Mockito.mock(Session.class));
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        Location from = Mockito.mock(Location.class);
        Location to = Mockito.mock(Location.class);
        ApplicableRegionSet toSet = Mockito.mock(ApplicableRegionSet.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Key destinationKey = Key.key("minecraft:new_world");
        Mockito.when(toSet.queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY)).thenReturn(destinationKey.asString());
        Mockito.when(worldGuard.getPlatform().getRegionContainer().createQuery()
                .getApplicableRegions(from).queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY))
                .thenReturn("minecraft:old_world");

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class);
             var mockedBukkit = Mockito.mockStatic(Bukkit.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);
            mockedBukkit.when(() -> Bukkit.getWorld(destinationKey)).thenReturn(null);

            Assertions.assertTrue(handler.onCrossBoundary(player, from, to, toSet, Set.of(), Set.of(), null));
            mockedBukkit.verify(() -> Bukkit.getWorld(destinationKey));
        }
    }

    @Test
    void testInvalidDestinationKeyDoesNotLookUpWorld() {
        TeleportToSpawnFlagHandler handler = new TeleportToSpawnFlagHandler(Mockito.mock(Session.class));
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        Location from = Mockito.mock(Location.class);
        Location to = Mockito.mock(Location.class);
        ApplicableRegionSet toSet = Mockito.mock(ApplicableRegionSet.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(toSet.queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY)).thenReturn("not a key");
        Mockito.when(worldGuard.getPlatform().getRegionContainer().createQuery()
                .getApplicableRegions(from).queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY))
                .thenReturn(null);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class);
             var mockedBukkit = Mockito.mockStatic(Bukkit.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);

            Assertions.assertTrue(handler.onCrossBoundary(player, from, to, toSet, Set.of(), Set.of(), null));
            mockedBukkit.verifyNoInteractions();
        }
    }
}
