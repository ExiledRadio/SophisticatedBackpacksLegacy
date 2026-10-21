/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.inception;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Tracks which slots of the parent backpack currently hold backpacks. */
public class SubBackpacksHandler {
    private final Map<Integer, BackpackWrapper> subBackpacks = new LinkedHashMap<>();
    private final InventoryHandler inventoryHandler;
    private final Set<Consumer<Collection<BackpackWrapper>>> refreshListeners = new HashSet<>();
    private final Set<Consumer<Collection<BackpackWrapper>>> beforeRefreshListeners = new HashSet<>();
    private final IntConsumer contentsListener = this::onContentsChanged;

    public SubBackpacksHandler(InventoryHandler inventoryHandler) {
        this.inventoryHandler = inventoryHandler;
        inventoryHandler.addListener(contentsListener);
        refreshSubBackpacks();
    }

    public void dispose() {
        inventoryHandler.removeListener(contentsListener);
    }

    public void addRefreshListener(Consumer<Collection<BackpackWrapper>> listener) {
        refreshListeners.add(listener);
    }

    public void addBeforeRefreshListener(Consumer<Collection<BackpackWrapper>> listener) {
        beforeRefreshListeners.add(listener);
    }

    public Collection<BackpackWrapper> getSubBackpacks() {
        return subBackpacks.values();
    }

    private void onContentsChanged(int slot) {
        ItemStack stackInSlot = inventoryHandler.getStackInSlot(slot);
        boolean backpackIsInTheSlot = stackInSlot.getItem() instanceof BackpackItem;
        boolean backpackWasInTheSlot = subBackpacks.containsKey(slot);
        if (!backpackWasInTheSlot && !backpackIsInTheSlot) {
            return;
        }
        if (backpackWasInTheSlot != backpackIsInTheSlot || CapabilityBackpackWrapper.get(stackInSlot) != subBackpacks.get(slot)) {
            beforeRefreshListeners.forEach(l -> l.accept(subBackpacks.values()));
            refreshSubBackpacks();
            refreshListeners.forEach(l -> l.accept(subBackpacks.values()));
        }
    }

    private void refreshSubBackpacks() {
        subBackpacks.clear();
        for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
            BackpackWrapper wrapper = CapabilityBackpackWrapper.get(inventoryHandler.getStackInSlot(slot));
            if (wrapper != null) {
                subBackpacks.put(slot, wrapper);
            }
        }
    }
}
