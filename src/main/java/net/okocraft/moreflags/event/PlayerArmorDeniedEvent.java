package net.okocraft.moreflags.event;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerArmorDeniedEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final PlayerArmorChangeEvent.SlotType slot;
    private final ItemStack item;

    public PlayerArmorDeniedEvent(@NotNull Player who, @NotNull PlayerArmorChangeEvent.SlotType slot, @NotNull ItemStack item) {
        super(who);
        this.slot = slot;
        this.item = item;
    }

    public @NotNull PlayerArmorChangeEvent.SlotType getSlot() {
        return this.slot;
    }

    public @NotNull ItemStack getItem() {
        return this.item.clone();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
