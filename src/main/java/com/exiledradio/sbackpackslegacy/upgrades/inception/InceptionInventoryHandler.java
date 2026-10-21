/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.inception;

import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/** The parent backpack and every backpack inside it, as one inventory, in the chosen order. */
public class InceptionInventoryHandler implements IItemHandlerSimpleInserter {
    private final IItemHandlerSimpleInserter wrappedInventoryHandler;
    private final InventoryOrder inventoryOrder;
    private final SubBackpacksHandler subBackpacksHandler;
    private IItemHandlerModifiable combinedInventories;
    private List<IItemHandlerSimpleInserter> handlers;

    public InceptionInventoryHandler(IItemHandlerSimpleInserter wrappedInventoryHandler, InventoryOrder inventoryOrder, SubBackpacksHandler subBackpacksHandler) {
        this.wrappedInventoryHandler = wrappedInventoryHandler;
        this.inventoryOrder = inventoryOrder;
        this.subBackpacksHandler = subBackpacksHandler;
        subBackpacksHandler.addRefreshListener(sbs -> refreshHandlerDelegate());
        refreshHandlerDelegate();
    }

    private void refreshHandlerDelegate() {
        handlers = new ArrayList<>();
        if (inventoryOrder == InventoryOrder.MAIN_FIRST) {
            handlers.add(wrappedInventoryHandler);
        }
        subBackpacksHandler.getSubBackpacks().forEach(sbp -> handlers.add(sbp.getInventoryForInputOutput()));
        if (inventoryOrder == InventoryOrder.INCEPTED_FIRST) {
            handlers.add(wrappedInventoryHandler);
        }
        combinedInventories = new CombinedInvWrapper(handlers.toArray(new IItemHandlerModifiable[0]));
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        combinedInventories.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return combinedInventories.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return combinedInventories.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return combinedInventories.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return combinedInventories.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return combinedInventories.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return combinedInventories.isItemValid(slot, stack);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        ItemStack remainingStack = stack;
        for (IItemHandlerSimpleInserter handler : handlers) {
            remainingStack = handler.insertItem(remainingStack, simulate);
            if (remainingStack.isEmpty()) {
                break;
            }
        }
        return remainingStack;
    }
}
