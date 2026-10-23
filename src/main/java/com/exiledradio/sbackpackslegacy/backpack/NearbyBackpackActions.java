/*
 * Written for Sophisticated Backpacks Legacy by ExiledRadio, GPLv3.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The backpack screen's two loot buttons.
 * <ul>
 * <li><b>Deposit to nearby chests</b> (like Quark's): each item goes into nearby containers that
 * already hold that item, onto matching stacks first. No-sort slots are left alone.</li>
 * <li><b>Merge backpacks</b>: items of a kind this backpack already holds come over from nearby
 * backpacks, placed ones and those worn by players in range (others' only where the server lets
 * players open other players' backpacks). The other backpack's no-sort slots are left alone, and
 * nothing goes through this backpack's void or compacting upgrades on the way in.</li>
 * </ul>
 * Both run on the server; the client uses {@link #hasMergeSource} to show the merge button.
 */
public final class NearbyBackpackActions {
    /** Containers with fewer slots (furnaces, hoppers, brewing stands) aren't storage and are skipped. */
    private static final int MIN_CONTAINER_SLOTS = 9;

    private NearbyBackpackActions() {}

    // ---- Deposit to nearby chests --------------------------------------------------------------

    public static void depositToNearbyChests(EntityPlayer player, BackpackContainer container) {
        int range = ModConfig.nearbyActions.depositToNearbyChestsRange;
        BackpackWrapper wrapper = container.getBackpackWrapper();
        if (range <= 0 || hasInfinityUpgrade(wrapper)) {
            return;
        }
        List<IItemHandler> targets = findNearbyContainers(player, range);
        InventoryHandler inventory = wrapper.getInventoryHandler();
        NoSortSettingsCategory noSort = wrapper.getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class);
        int moved = 0;
        java.util.Set<IItemHandler> used = new java.util.HashSet<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (noSort != null && noSort.isSlotSelected(slot)) {
                continue;
            }
            for (IItemHandler target : targets) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    break;
                }
                if (!containsItem(target, stack)) {
                    continue;
                }
                int movedHere = moveOut(inventory, slot, target);
                if (movedHere > 0) {
                    moved += movedHere;
                    used.add(target);
                }
            }
        }
        player.sendStatusMessage(moved > 0
                ? new TextComponentTranslation("gui.sbackpackslegacy.deposit_nearby.done", moved, used.size())
                : new TextComponentTranslation(targets.isEmpty() ? "gui.sbackpackslegacy.deposit_nearby.no_containers" : "gui.sbackpackslegacy.deposit_nearby.nothing"), true);
    }

    /** Moves as much of one backpack slot as fits into the target, a stack's worth at a time; returns how many moved. */
    private static int moveOut(InventoryHandler inventory, int slot, IItemHandler target) {
        int moved = 0;
        while (true) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return moved;
            }
            ItemStack chunk = stack.copy();
            chunk.setCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(target, chunk, true);
            int fits = chunk.getCount() - remaining.getCount();
            if (fits <= 0) {
                return moved;
            }
            ItemStack extracted = inventory.extractItem(slot, fits, false);
            if (extracted.isEmpty()) {
                return moved;
            }
            ItemStack left = ItemHandlerHelper.insertItemStacked(target, extracted, false);
            if (!left.isEmpty()) {
                // Whatever the target turned down after all goes back where it came from.
                inventory.insertItemFromGui(slot, left, false);
            }
            moved += extracted.getCount() - left.getCount();
            if (!left.isEmpty()) {
                return moved;
            }
        }
    }

    /** Storage-like containers in range of the player that they could open themselves (not backpacks). */
    private static List<IItemHandler> findNearbyContainers(EntityPlayer player, int range) {
        List<IItemHandler> containers = new ArrayList<>();
        World world = player.world;
        double rangeSq = (double) range * range;
        for (TileEntity tile : new ArrayList<>(world.loadedTileEntityList)) {
            if (tile.isInvalid() || tile instanceof BackpackTileEntity || player.getDistanceSqToCenter(tile.getPos()) > rangeSq) {
                continue;
            }
            if (tile instanceof ILockableContainer && ((ILockableContainer) tile).isLocked()
                    && !player.canOpen(((ILockableContainer) tile).getLockCode())) {
                continue;
            }
            if (!tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                continue;
            }
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null && handler.getSlots() >= MIN_CONTAINER_SLOTS) {
                containers.add(handler);
            }
        }
        return containers;
    }

    private static boolean containsItem(IItemHandler handler, ItemStack stack) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (ItemHandlerHelper.canItemStacksStack(handler.getStackInSlot(slot), stack)) {
                return true;
            }
        }
        return false;
    }

    // ---- Merge backpacks ------------------------------------------------------------------------

    public static void mergeBackpacks(EntityPlayer player, BackpackContainer container) {
        BackpackWrapper target = container.getBackpackWrapper();
        if (ModConfig.nearbyActions.mergeBackpacksRange <= 0 || target.getContentsUuid() == null) {
            return;
        }
        List<BackpackWrapper> sources = findMergeSources(player, container);
        InventoryHandler targetInventory = target.getInventoryHandler();
        MemorySettingsCategory memory = target.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);
        int moved = 0;
        int backpacksUsed = 0;
        for (BackpackWrapper source : sources) {
            if (hasInfinityUpgrade(source)) {
                continue;
            }
            InventoryHandler sourceInventory = source.getInventoryHandler();
            NoSortSettingsCategory noSort = source.getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class);
            int movedFromThis = 0;
            for (int slot = 0; slot < sourceInventory.getSlots(); slot++) {
                if (noSort != null && noSort.isSlotSelected(slot)) {
                    continue;
                }
                ItemStack stack = sourceInventory.getStackInSlot(slot);
                if (stack.isEmpty() || !containsItem(targetInventory, stack)) {
                    continue;
                }
                movedFromThis += moveInto(sourceInventory, slot, targetInventory, memory);
            }
            if (movedFromThis > 0) {
                moved += movedFromThis;
                backpacksUsed++;
            }
        }
        player.sendStatusMessage(moved > 0
                ? new TextComponentTranslation("gui.sbackpackslegacy.merge_backpacks.done", moved, backpacksUsed)
                : new TextComponentTranslation(sources.isEmpty() ? "gui.sbackpackslegacy.merge_backpacks.no_backpacks" : "gui.sbackpackslegacy.merge_backpacks.nothing"), true);
    }

    /** Moves one source slot into the target backpack, a stack's worth at a time; returns how many moved. */
    private static int moveInto(InventoryHandler source, int slot, InventoryHandler target, @Nullable MemorySettingsCategory memory) {
        int moved = 0;
        while (true) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return moved;
            }
            ItemStack chunk = stack.copy();
            chunk.setCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
            int fits = chunk.getCount() - insertWithoutUpgrades(target, chunk, memory, true).getCount();
            if (fits <= 0) {
                return moved;
            }
            ItemStack extracted = source.extractItem(slot, fits, false);
            if (extracted.isEmpty()) {
                return moved;
            }
            ItemStack left = insertWithoutUpgrades(target, extracted, memory, false);
            if (!left.isEmpty()) {
                source.insertItemFromGui(slot, left, false);
            }
            moved += extracted.getCount() - left.getCount();
            if (!left.isEmpty()) {
                return moved;
            }
        }
    }

    /**
     * The backpack's own insert order (matching stacks, then memory slots for the item, then other
     * empty slots), but straight into the slots: no void, compacting or overflow upgrades, which
     * would destroy loot that's only moving between backpacks.
     */
    private static ItemStack insertWithoutUpgrades(InventoryHandler inventory, ItemStack stack, @Nullable MemorySettingsCategory memory, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < inventory.getSlots() && !remaining.isEmpty(); slot++) {
            if (ItemHandlerHelper.canItemStacksStack(inventory.getStackInSlot(slot), remaining)) {
                remaining = inventory.insertItemFromGui(slot, remaining, simulate);
            }
        }
        Set<Integer> memorySlots = memory == null ? java.util.Collections.emptySet() : memory.getSlotsFor(remaining);
        for (int slot : memorySlots) {
            if (remaining.isEmpty()) {
                break;
            }
            if (slot < inventory.getSlots() && inventory.getStackInSlot(slot).isEmpty()) {
                remaining = inventory.insertItemFromGui(slot, remaining, simulate);
            }
        }
        for (int slot = 0; slot < inventory.getSlots() && !remaining.isEmpty(); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty() && (memory == null || !memory.isSlotSelected(slot))) {
                remaining = inventory.insertItemFromGui(slot, remaining, simulate);
            }
        }
        return remaining;
    }

    /** Whether there's another backpack in range to merge from (the client shows the button by this). */
    public static boolean hasMergeSource(EntityPlayer player, BackpackContainer container) {
        return ModConfig.nearbyActions.mergeBackpacksRange > 0 && !findMergeSources(player, container).isEmpty();
    }

    /**
     * Backpacks in range of the open one: placed backpacks, the player's own worn backpack when
     * another one is open, and other players' worn backpacks where the server allows opening them.
     * Never the open backpack itself or another stack with the same contents.
     */
    private static List<BackpackWrapper> findMergeSources(EntityPlayer player, BackpackContainer container) {
        List<BackpackWrapper> sources = new ArrayList<>();
        BackpackWrapper target = container.getBackpackWrapper();
        UUID targetContents = target.getContentsUuid();
        if (targetContents == null) {
            return sources;
        }
        World world = player.world;
        Vec3d origin = getBackpackPosition(player, container);
        double rangeSq = (double) ModConfig.nearbyActions.mergeBackpacksRange * ModConfig.nearbyActions.mergeBackpacksRange;
        BlockPos openPos = container.getStorageBlockPos();
        for (TileEntity tile : new ArrayList<>(world.loadedTileEntityList)) {
            if (!(tile instanceof BackpackTileEntity) || tile.isInvalid() || tile.getPos().equals(openPos)
                    || origin.squareDistanceTo(new Vec3d(tile.getPos()).add(0.5, 0.5, 0.5)) > rangeSq) {
                continue;
            }
            addSource(sources, ((BackpackTileEntity) tile).getBackpackWrapper(), targetContents);
        }
        for (EntityPlayer other : new ArrayList<>(world.playerEntities)) {
            if (other.isSpectator() || !other.isEntityAlive() || origin.squareDistanceTo(other.getPositionVector()) > rangeSq) {
                continue;
            }
            if (other != player && !ModConfig.allowOpeningOtherPlayerBackpacks) {
                continue;
            }
            WornBackpack worn = WornBackpack.find(other);
            if (worn == null || worn.getBackpack() == container.getOpenedStack()) {
                continue;
            }
            addSource(sources, CapabilityBackpackWrapper.get(worn.getBackpack()), targetContents);
        }
        return sources;
    }

    private static void addSource(List<BackpackWrapper> sources, @Nullable BackpackWrapper wrapper, UUID targetContents) {
        if (wrapper == null) {
            return;
        }
        UUID contents = wrapper.getContentsUuid();
        if (contents == null || contents.equals(targetContents)) {
            return;
        }
        for (BackpackWrapper known : sources) {
            if (contents.equals(known.getContentsUuid())) {
                return;
            }
        }
        sources.add(wrapper);
    }

    /** Where the open backpack is: its block, its wearer, or the player holding it. */
    private static Vec3d getBackpackPosition(EntityPlayer player, BackpackContainer container) {
        BlockPos pos = container.getStorageBlockPos();
        if (pos != null) {
            return new Vec3d(pos).add(0.5, 0.5, 0.5);
        }
        BackpackContext context = container.getContext();
        if (context instanceof BackpackContext.AnotherPlayer) {
            EntityPlayer wearer = ((BackpackContext.AnotherPlayer) context).getWearer(player);
            if (wearer != null) {
                return wearer.getPositionVector();
            }
        }
        return player.getPositionVector();
    }

    /** An infinity upgrade's endless stacks can't be moved out without inventing items. */
    private static boolean hasInfinityUpgrade(BackpackWrapper wrapper) {
        return com.exiledradio.scorelegacy.upgrades.infinity.InfinityUpgradeItem.hasInfinityUpgrade(wrapper.getUpgradeHandler());
    }
}
