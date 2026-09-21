package net.okocraft.moreflags.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PlayerArmorDeniedEventTest {

    @Test
    void testGetItemReturnsDefensiveCopy() {
        Player player = Mockito.mock(Player.class);
        ItemStack item = Mockito.mock(ItemStack.class);
        ItemStack firstClone = Mockito.mock(ItemStack.class);
        ItemStack secondClone = Mockito.mock(ItemStack.class);
        Mockito.when(item.clone()).thenReturn(firstClone, secondClone);

        PlayerArmorDeniedEvent event = new PlayerArmorDeniedEvent(player, EquipmentSlot.HEAD, item);

        Assertions.assertSame(player, event.getPlayer());
        Assertions.assertEquals(EquipmentSlot.HEAD, event.getSlot());
        Assertions.assertSame(firstClone, event.getItem());
        Assertions.assertSame(secondClone, event.getItem());
        Assertions.assertNotSame(firstClone, secondClone);
        Mockito.verify(item, Mockito.times(2)).clone();
    }
}
