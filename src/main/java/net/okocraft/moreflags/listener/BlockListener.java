package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class BlockListener extends AbstractWorldGuardInternalListener {

    @EventHandler(priority = EventPriority.LOW)
    public void onBreakLow(@NotNull BreakBlockEvent event) {
        if (!getConfig().get(event.getWorld().getName()).useRegions) {
            return;
        }

        switch (event.getOriginalEvent()) {
            case BlockBreakEvent ignored -> {
            }
            case null, default -> {
                return;
            }
        }

        event.setSilent(true); // To prevent sending message from RegionProtectionListener
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakHigh(@NotNull BreakBlockEvent event) {
        if (!getConfig().get(event.getWorld().getName()).useRegions) {
            return;
        }

        switch (event.getOriginalEvent()) {
            case BlockBreakEvent breakEvent -> processEvent(
                    breakEvent.getPlayer(), breakEvent.getBlock(), CustomFlags.BREAKABLE_BLOCKS,
                    event, event.getBlocks(), "worldguard.error.denied.what.break-that-block"
            );
            case null, default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlaceLow(@NotNull PlaceBlockEvent event) {
        if (!getConfig().get(event.getWorld().getName()).useRegions) {
            return;
        }

        switch (event.getOriginalEvent()) {
            case BlockPlaceEvent placeEvent -> {
                if (placeEvent instanceof BlockMultiPlaceEvent) {
                    // We do not support placing multiple blocks.
                    return;
                }
            }
            case BlockFertilizeEvent fertilizeEvent -> {
                if (fertilizeEvent.getPlayer() == null) {
                    return;
                }
                checkFertilizeFlag(fertilizeEvent.getPlayer(), fertilizeEvent.getBlock(), event::setResult);
            }
            case PlayerInteractEvent interactEvent -> { // for fertilize flag
                Block block = interactEvent.getClickedBlock();
                ItemStack item = interactEvent.getItem();
                if (block == null || item == null || item.getType() != Material.BONE_MEAL) {
                    return;
                }
                checkFertilizeFlag(interactEvent.getPlayer(), block, event::setResult);
            }
            case SignChangeEvent ignored -> {
            }
            case null, default -> {
                return;
            }
        }

        event.setSilent(true); // To prevent sending message from RegionProtectionListener
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlaceHigh(@NotNull PlaceBlockEvent event) {
        if (!getConfig().get(event.getWorld().getName()).useRegions) {
            return;
        }

        switch (event.getOriginalEvent()) {
            case BlockPlaceEvent placeEvent -> {
                if (placeEvent instanceof BlockMultiPlaceEvent) {
                    // We do not support placing multiple blocks.
                    return;
                }

                processEvent(
                        placeEvent.getPlayer(), placeEvent.getBlock(), CustomFlags.PLACEABLE_BLOCKS,
                        event, event.getBlocks(), "worldguard.error.denied.what.place-that-block"
                );
            }
            case SignChangeEvent signChangeEvent -> processEvent(
                    signChangeEvent.getPlayer(), signChangeEvent.getBlock(), CustomFlags.SIGN_EDIT,
                    event, event.getBlocks(), "worldguard.error.denied.what.place-that-block"
            );
            case null, default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlockLow(@NotNull UseBlockEvent event) {
        if (!getConfig().get(event.getWorld().getName()).useRegions) {
            return;
        }

        switch (event.getOriginalEvent()) {
            case PlayerOpenSignEvent ignored -> {
            }
            case PlayerInteractEvent interactEvent -> { // for fertilize flag
                Block block = interactEvent.getClickedBlock();
                ItemStack item = interactEvent.getItem();
                if (block == null || item == null || item.getType() != Material.BONE_MEAL || !interactEvent.getAction().isRightClick()) {
                    return;
                }
                checkFertilizeFlag(interactEvent.getPlayer(), block, event::setResult);
            }
            case null, default -> {
                return;
            }
        }

        event.setSilent(true); // To prevent sending message from RegionProtectionListener
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUseBlockHigh(@NotNull UseBlockEvent event) {
        if (!getConfig().get(event.getWorld().getName()).useRegions) {
            return;
        }

        switch (event.getOriginalEvent()) {
            case PlayerOpenSignEvent openSignEvent -> processEvent(
                    openSignEvent.getPlayer(), openSignEvent.getSign().getBlock(), CustomFlags.SIGN_EDIT,
                    event, event.getBlocks(), "worldguard.error.denied.what.open-that"
            );
            case null, default -> {
            }
        }
    }

    private static void processEvent(@NotNull Player bukkitPlayer, @NotNull Block block,
                                     @NotNull SetFlag<BlockType> flag,
                                     @NotNull DelegateEvent weEvent, @NotNull List<Block> eventBlocks,
                                     @NotNull String denyMessageKey) {
        var localPlayer = getPlugin().wrapPlayer(bukkitPlayer);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        var blockType = BukkitAdapter.adapt(block.getBlockData()).getBlockType();
        var blocks = blockType != null ? FlagUtil.queryValue(block.getWorld(), block.getLocation(), localPlayer, flag) : null;

        if (blocks != null) {
            if (weEvent.getResult() == Event.Result.DENY && blocks.contains(blockType)) {
                // Other flags cancel the event, but the given flag allows it.
                weEvent.setResult(Event.Result.ALLOW);
                eventBlocks.add(block); // Re-add a block
            } else if (weEvent.getResult() != Event.Result.DENY && !blocks.contains(blockType)) {
                // Other flags allow the event, but the given flag disallows it.
                weEvent.setResult(Event.Result.DENY);
                eventBlocks.remove(block); // Remove a block
            }
        } /* else {
          // The given flag is not set, so we follow other flags such as "build" flag.
        } */

        weEvent.setSilent(false);

        if (weEvent.getResult() == Event.Result.DENY) {
            tellErrorMessage(bukkitPlayer, block.getLocation(), TranslatableComponent.of(denyMessageKey));
        }
    }

    private static void processEvent(@NotNull Player bukkitPlayer, @NotNull Block block,
                                     @NotNull StateFlag flag,
                                     @NotNull DelegateEvent weEvent, @NotNull List<Block> eventBlocks,
                                     @NotNull String denyMessageKey) {
        var localPlayer = getPlugin().wrapPlayer(bukkitPlayer);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        var state = FlagUtil.queryValue(block.getWorld(), block.getLocation(), localPlayer, flag);

        if (state != null) {
            if (state == StateFlag.State.ALLOW) {
                // Other flags cancel the event, but the given flag allows it.
                weEvent.setResult(Event.Result.ALLOW);
                if (weEvent.getResult() == Event.Result.DENY) {
                    eventBlocks.add(block); // Re-add a block
                }
            } else if (state == StateFlag.State.DENY) {
                // Other flags allow the event, but the given flag disallows it.
                weEvent.setResult(Event.Result.DENY);
                if (weEvent.getResult() != Event.Result.DENY) {
                    eventBlocks.remove(block); // Remove a block
                }
            }
        } /* else {
          // The given flag is not set, so we follow other flags such as "build" flag.
        } */

        weEvent.setSilent(false);

        if (weEvent.getResult() == Event.Result.DENY) {
            tellErrorMessage(bukkitPlayer, block.getLocation(), TranslatableComponent.of(denyMessageKey));
        }
    }

    private static void checkFertilizeFlag(@NotNull Player bukkitPlayer, @NotNull Block block, @NotNull Consumer<Event.Result> setResult) {
        var localPlayer = getPlugin().wrapPlayer(bukkitPlayer);
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        var state = FlagUtil.queryValue(block.getWorld(), block.getLocation(), localPlayer, CustomFlags.FERTILIZE);

        if (state == StateFlag.State.ALLOW) {
            setResult.accept(Event.Result.ALLOW);
        } else if (state == StateFlag.State.DENY) {
            setResult.accept(Event.Result.DENY);
            tellErrorMessage(bukkitPlayer, block.getLocation(), TranslatableComponent.of("worldguard.error.denied.what.use-that"));
        }
    }
}
