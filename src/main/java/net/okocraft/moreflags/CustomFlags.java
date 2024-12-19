package net.okocraft.moreflags;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegistryFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public final class CustomFlags {

    private CustomFlags() {
    }

    static void init() {
        // for loading class and flags.
    }

    public static final StateFlag VILLAGER_TRADE = registerFlag(createStateFlag("villager-trade", true, RegionGroup.ALL, true));

    public static final StateFlag DAMAGE_NAMED_ENTITY = registerFlag(createStateFlag("damage-named-entity", true, RegionGroup.ALL, true));

    public static final StateFlag PASSTHROUGH_OUTSIDE_BEACON = registerFlag(createStateFlag("passthrough-outside-beacon", true));

    public static final StateFlag PASSTHROUGH_INSIDE_BEACON = registerFlag(createStateFlag("passthrough-inside-beacon", true));

    public static final StateFlag SEND_DEATH_MESSAGE = registerFlag(createStateFlag("send-death-message", true));
    public static final StateFlag RECEIVE_DEATH_MESSAGE = registerFlag(createStateFlag("receive-death-message", true));
    public static final StateFlag ISOLATE_DEATH_MESSAGE = registerFlag(createStateFlag("isolate-death-message", false));

    public static final StateFlag VEHICLE_ENTRY = registerFlag(createStateFlag("vehicle-entry", true, RegionGroup.ALL, true));

    public static final StateFlag RAID = registerFlag(createStateFlag("raid", true));

    public static final StateFlag EGG_SPAWN_CHICK = registerFlag(createStateFlag("egg-spawn-chick", true));

    public static final StateFlag SIGN_EDIT = registerFlag(createStateFlag("sign-edit", false));
    public static final StateFlag PLACE_COBWEB_ON_DEATH = registerFlag(createStateFlag("place-cobweb-on-death", true));

    public static final SetFlag<BlockType> BREAKABLE_BLOCKS = registerFlag(new SetFlag<>("breakable-blocks", new RegistryFlag<>(null, BlockType.REGISTRY)));

    public static final SetFlag<BlockType> PLACEABLE_BLOCKS = registerFlag(new SetFlag<>("placeable-blocks", new RegistryFlag<>(null, BlockType.REGISTRY)));

    public static final SetFlag<ItemType> ARMOR_BLACKLIST_HEAD = registerFlag(new SetFlag<>("armor-blacklist-head", new RegistryFlag<>(null, ItemType.REGISTRY)));
    public static final SetFlag<ItemType> ARMOR_BLACKLIST_CHEST = registerFlag(new SetFlag<>("armor-blacklist-chest", new RegistryFlag<>(null, ItemType.REGISTRY)));
    public static final SetFlag<ItemType> ARMOR_BLACKLIST_LEGS = registerFlag(new SetFlag<>("armor-blacklist-legs", new RegistryFlag<>(null, ItemType.REGISTRY)));
    public static final SetFlag<ItemType> ARMOR_BLACKLIST_FEET = registerFlag(new SetFlag<>("armor-blacklist-feet", new RegistryFlag<>(null, ItemType.REGISTRY)));

    public static final StringFlag MESSAGE_ON_ARMOR_DENIED = registerFlag(new StringFlag("message-on-armor-denied", ""));
    public static final StringFlag TELEPORT_ON_ARMOR_DENIED = registerFlag(new StringFlag("teleport-on-armor-denied", ""));

    @Deprecated
    public static final StringFlag PREVENT_ELYTRA = registerFlag(new StringFlag("prevent-elytra", "")); // remove this flag later

    @SuppressWarnings("unchecked")
    private static <F extends Flag<?>, C extends F> F registerFlag(C flag) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(flag);
            return flag;
        } catch (FlagConflictException | IllegalStateException e) {
            // other plugin registered flag with same name already or force reloading.
            // use registered flag if available.
            Flag<?> existing = registry.get(flag.getName());
            if (existing == null) {
                throw new RuntimeException("The flag " + flag.getName() + " cannot be register and found on registry. Force reloaded newer version of plugin?");
            }
            try {
                return (F) existing;
            } catch (ClassCastException e2) {
                throw new RuntimeException("The type of flag " + existing.getName() + " cannot be cast to type that is not defined by WorldGuard on force reloading.", e);
            }
        }
    }

    private static StateFlag createStateFlag(String name, boolean def) {
        return new StateFlag(name, def);
    }

    private static StateFlag createStateFlag(String name, boolean def, RegionGroup group) {
        return new StateFlag(name, def, group);
    }

    private static StateFlag createStateFlag(String name, boolean def, RegionGroup group, boolean membershipsAsDefault) {
        if (membershipsAsDefault) {
            return new StateFlag(name, def, group) {
                @Override public boolean implicitlySetWithMembership() { return true; }
                @Override public boolean usesMembershipAsDefault() { return true; }
                @Override public boolean preventsAllowOnGlobal() { return true; }
                @Override public boolean requiresSubject() { return true; }
            };
        } else {
            return new StateFlag(name, def, group);
        }
    }
}
