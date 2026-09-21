package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.testsupport.CustomFlagsTestSupport;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CobwebPlaceListenerTest {

    @BeforeAll
    static void initializeCustomFlags() {
        CustomFlagsTestSupport.initialize();
    }

    @Test
    void testIgnoresNonCobwebChange() {
        CobwebPlaceListener listener = new CobwebPlaceListener();
        EntityChangeBlockEvent event = Mockito.mock(EntityChangeBlockEvent.class);
        Mockito.when(event.getTo()).thenReturn(Material.STONE);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class);
             var mockedBukkitAdapter = Mockito.mockStatic(BukkitAdapter.class)) {
            listener.onChangeBlock(event);

            Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
            mockedWorldGuard.verifyNoInteractions();
            mockedBukkitAdapter.verifyNoInteractions();
        }
    }

    @Test
    void testCancelsCobwebChangeWhenFlagIsDenied() {
        CobwebPlaceListener listener = new CobwebPlaceListener();
        EntityChangeBlockEvent event = Mockito.mock(EntityChangeBlockEvent.class);
        Block block = Mockito.mock(Block.class);
        org.bukkit.Location bukkitLocation = Mockito.mock(org.bukkit.Location.class);
        com.sk89q.worldedit.util.Location worldEditLocation = Mockito.mock(com.sk89q.worldedit.util.Location.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);

        Mockito.when(event.getTo()).thenReturn(Material.COBWEB);
        Mockito.when(event.getBlock()).thenReturn(block);
        Mockito.when(block.getLocation()).thenReturn(bukkitLocation);
        Mockito.when(worldGuard.getPlatform().getRegionContainer().createQuery()
                .queryState(worldEditLocation, null, CustomFlags.PLACE_COBWEB_ON_DEATH))
                .thenReturn(StateFlag.State.DENY);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class);
             var mockedBukkitAdapter = Mockito.mockStatic(BukkitAdapter.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);
            mockedBukkitAdapter.when(() -> BukkitAdapter.adapt(bukkitLocation)).thenReturn(worldEditLocation);

            listener.onChangeBlock(event);

            Mockito.verify(event).setCancelled(true);
        }
    }

    @Test
    void testKeepsCobwebChangeWhenFlagIsAllowed() {
        CobwebPlaceListener listener = new CobwebPlaceListener();
        EntityChangeBlockEvent event = Mockito.mock(EntityChangeBlockEvent.class);
        Block block = Mockito.mock(Block.class);
        org.bukkit.Location bukkitLocation = Mockito.mock(org.bukkit.Location.class);
        com.sk89q.worldedit.util.Location worldEditLocation = Mockito.mock(com.sk89q.worldedit.util.Location.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);

        Mockito.when(event.getTo()).thenReturn(Material.COBWEB);
        Mockito.when(event.getBlock()).thenReturn(block);
        Mockito.when(block.getLocation()).thenReturn(bukkitLocation);
        Mockito.when(worldGuard.getPlatform().getRegionContainer().createQuery()
                .queryState(worldEditLocation, null, CustomFlags.PLACE_COBWEB_ON_DEATH))
                .thenReturn(StateFlag.State.ALLOW);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class);
             var mockedBukkitAdapter = Mockito.mockStatic(BukkitAdapter.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);
            mockedBukkitAdapter.when(() -> BukkitAdapter.adapt(bukkitLocation)).thenReturn(worldEditLocation);

            listener.onChangeBlock(event);

            Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
        }
    }
}
