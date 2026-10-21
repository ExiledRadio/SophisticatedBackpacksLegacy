/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.deposit;

import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import com.exiledradio.scorelegacy.util.ItemStackKey;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/** A filter that can also say "whatever the target inventory already holds". */
public class DepositFilterLogic extends FilterLogic {
    private Set<ItemStackKey> inventoryFilterStacks = new HashSet<>();

    public DepositFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount) {
        super(upgrade, saveHandler, filterSlotCount);
    }

    public DepositFilterType getDepositFilterType() {
        if (shouldFilterByInventory()) {
            return DepositFilterType.INVENTORY;
        }
        return isAllowList() ? DepositFilterType.ALLOW : DepositFilterType.BLOCK;
    }

    public void setDepositFilterType(DepositFilterType depositFilterType) {
        switch (depositFilterType) {
            case ALLOW:
                setFilterByInventory(false);
                setAllowList(true);
                break;
            case BLOCK:
                setFilterByInventory(false);
                setAllowList(false);
                break;
            case INVENTORY:
            default:
                setFilterByInventory(true);
                save();
        }
    }

    public void setInventory(IItemHandler inventory) {
        inventoryFilterStacks = InventoryHelper.getUniqueStacks(inventory);
    }

    @Override
    public boolean matchesFilter(ItemStack stack) {
        if (!shouldFilterByInventory()) {
            return super.matchesFilter(stack);
        }
        for (ItemStackKey filterStack : inventoryFilterStacks) {
            if (stackMatchesFilter(stack, filterStack.getStack())) {
                return true;
            }
        }
        return false;
    }

    private void setFilterByInventory(boolean filterByInventory) {
        NBTHelper.setBoolean(upgrade, "filterByInventory", filterByInventory);
        save();
    }

    private boolean shouldFilterByInventory() {
        return NBTHelper.getBoolean(upgrade, "filterByInventory", false);
    }
}
