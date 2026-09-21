package net.okocraft.moreflags.handler;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.SetFlag;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.testsupport.CustomFlagsTestSupport;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Set;
import java.util.stream.Stream;

class ArmorCheckHandlerTest {

    @BeforeAll
    static void initializeCustomFlags() {
        CustomFlagsTestSupport.initialize();
    }

    @Test
    void testShouldCheckArmorsReturnsFalseForBypassPlayer() {
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        World world = Mockito.mock(World.class);
        ApplicableRegionSet regions = Mockito.mock(ApplicableRegionSet.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(player.getWorld()).thenReturn(world);
        Mockito.when(worldGuard.getPlatform().getSessionManager().hasBypass(player, world)).thenReturn(true);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);

            Assertions.assertFalse(ArmorCheckHandler.shouldCheckArmors(player, regions));
        }
    }

    @Test
    void testShouldCheckArmorsReturnsFalseWithoutRegionManager() {
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        World world = Mockito.mock(World.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(player.getWorld()).thenReturn(world);
        Mockito.when(worldGuard.getPlatform().getSessionManager().hasBypass(player, world)).thenReturn(false);
        Mockito.when(worldGuard.getPlatform().getRegionContainer().get(world)).thenReturn(null);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);

            Assertions.assertFalse(ArmorCheckHandler.shouldCheckArmors(player, null));
        }
    }

    @Test
    void testShouldCheckArmorsReturnsFalseWithoutActionFlags() {
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        World world = Mockito.mock(World.class);
        ApplicableRegionSet regions = Mockito.mock(ApplicableRegionSet.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(player.getWorld()).thenReturn(world);
        Mockito.when(worldGuard.getPlatform().getSessionManager().hasBypass(player, world)).thenReturn(false);
        Mockito.when(regions.queryValue(player, CustomFlags.MESSAGE_ON_ARMOR_DENIED)).thenReturn(null);
        Mockito.when(regions.queryValue(player, CustomFlags.TELEPORT_ON_ARMOR_DENIED)).thenReturn("");

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);

            Assertions.assertFalse(ArmorCheckHandler.shouldCheckArmors(player, regions));
        }
    }

    @Test
    void testShouldCheckArmorsReturnsTrueWithMessageFlag() {
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        World world = Mockito.mock(World.class);
        ApplicableRegionSet regions = Mockito.mock(ApplicableRegionSet.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(player.getWorld()).thenReturn(world);
        Mockito.when(worldGuard.getPlatform().getSessionManager().hasBypass(player, world)).thenReturn(false);
        Mockito.when(regions.queryValue(player, CustomFlags.MESSAGE_ON_ARMOR_DENIED)).thenReturn("denied");
        Mockito.when(regions.queryValue(player, CustomFlags.TELEPORT_ON_ARMOR_DENIED)).thenReturn(null);

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);

            Assertions.assertTrue(ArmorCheckHandler.shouldCheckArmors(player, regions));
        }
    }

    @Test
    void testShouldCheckArmorsReturnsTrueWithTeleportFlag() {
        LocalPlayer player = Mockito.mock(LocalPlayer.class);
        World world = Mockito.mock(World.class);
        ApplicableRegionSet regions = Mockito.mock(ApplicableRegionSet.class);
        WorldGuard worldGuard = Mockito.mock(WorldGuard.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(player.getWorld()).thenReturn(world);
        Mockito.when(worldGuard.getPlatform().getSessionManager().hasBypass(player, world)).thenReturn(false);
        Mockito.when(regions.queryValue(player, CustomFlags.MESSAGE_ON_ARMOR_DENIED)).thenReturn("");
        Mockito.when(regions.queryValue(player, CustomFlags.TELEPORT_ON_ARMOR_DENIED)).thenReturn("0,64,0");

        try (var mockedWorldGuard = Mockito.mockStatic(WorldGuard.class)) {
            mockedWorldGuard.when(WorldGuard::getInstance).thenReturn(worldGuard);

            Assertions.assertTrue(ArmorCheckHandler.shouldCheckArmors(player, regions));
        }
    }

    @Test
    void testCheckArmorIgnoresNonBukkitPlayer() {
        LocalPlayer player = Mockito.mock(LocalPlayer.class);

        Assertions.assertNull(ArmorCheckHandler.checkArmor(player, EquipmentSlot.HEAD, null));
    }

    @Test
    void testCheckArmorIgnoresNonArmorSlot() {
        BukkitPlayer player = Mockito.mock(BukkitPlayer.class);

        try (var mockedFlagUtil = Mockito.mockStatic(FlagUtil.class)) {
            Assertions.assertNull(ArmorCheckHandler.checkArmor(player, EquipmentSlot.HAND, null));
            mockedFlagUtil.verifyNoInteractions();
        }
    }

    @ParameterizedTest
    @MethodSource("armorSlots")
    void testCheckArmorUsesMatchingSlotAndBlacklistFlag(EquipmentSlot slot, SetFlag<ItemType> flag) {
        BukkitPlayer player = Mockito.mock(BukkitPlayer.class);
        Player bukkitPlayer = Mockito.mock(Player.class);
        PlayerInventory inventory = Mockito.mock(PlayerInventory.class);
        Mockito.when(player.getPlayer()).thenReturn(bukkitPlayer);
        Mockito.when(bukkitPlayer.getInventory()).thenReturn(inventory);
        Mockito.when(inventory.getItem(slot)).thenReturn(null);

        try (var mockedFlagUtil = Mockito.mockStatic(FlagUtil.class)) {
            mockedFlagUtil.when(() -> FlagUtil.queryValueForPlayer(player, flag))
                    .thenReturn(Set.of(Mockito.mock(ItemType.class)));

            Assertions.assertNull(ArmorCheckHandler.checkArmor(player, slot, null));
            mockedFlagUtil.verify(() -> FlagUtil.queryValueForPlayer(player, flag));
            Mockito.verify(inventory).getItem(slot);
        }
    }

    static Stream<Arguments> armorSlots() {
        return Stream.of(
                Arguments.of(EquipmentSlot.HEAD, CustomFlags.ARMOR_BLACKLIST_HEAD),
                Arguments.of(EquipmentSlot.CHEST, CustomFlags.ARMOR_BLACKLIST_CHEST),
                Arguments.of(EquipmentSlot.LEGS, CustomFlags.ARMOR_BLACKLIST_LEGS),
                Arguments.of(EquipmentSlot.FEET, CustomFlags.ARMOR_BLACKLIST_FEET)
        );
    }

    @Test
    void testCheckArmorAcceptsEmptyNewItem() {
        BukkitPlayer player = Mockito.mock(BukkitPlayer.class);
        ItemStack item = Mockito.mock(ItemStack.class);
        Mockito.when(item.isEmpty()).thenReturn(true);

        try (var mockedFlagUtil = Mockito.mockStatic(FlagUtil.class)) {
            mockedFlagUtil.when(() -> FlagUtil.queryValueForPlayer(player, CustomFlags.ARMOR_BLACKLIST_CHEST))
                    .thenReturn(Set.of(Mockito.mock(ItemType.class)));

            Assertions.assertNull(ArmorCheckHandler.checkArmor(player, EquipmentSlot.CHEST, item));
        }
    }
}
