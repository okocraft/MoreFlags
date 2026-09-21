package net.okocraft.moreflags.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PlayerArmorDeniedEventTest {

    @Test
    void testGetItemReturnsClone() {
        Player player = Mockito.mock(Player.class);
        ItemStack item = Mockito.mock(ItemStack.class);
        ItemStack clonedItem = Mockito.mock(ItemStack.class);
        Mockito.when(item.clone()).thenReturn(clonedItem);

        PlayerArmorDeniedEvent event = new PlayerArmorDeniedEvent(player, EquipmentSlot.HEAD, item);

        Assertions.assertSame(player, event.getPlayer());
        Assertions.assertEquals(EquipmentSlot.HEAD, event.getSlot());
        Assertions.assertSame(clonedItem, event.getItem());
        Mockito.verify(item).clone();
    }
}
