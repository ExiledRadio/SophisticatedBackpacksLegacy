/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import com.exiledradio.scorelegacy.common.gui.IFilterSlot;
import com.exiledradio.scorelegacy.common.gui.INameableEmptySlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** A ghost slot over one of the tool swapper's filters. */
public class ToolFilterSlot extends Slot implements IFilterSlot, INameableEmptySlot {
    private static final InventoryBasic EMPTY_INVENTORY = new InventoryBasic("", false, 1);
    private final Supplier<ItemStack> getTool;
    private final Consumer<ItemStack> setTool;
    private final Predicate<ItemStack> isToolTypeValid;
    @Nullable
    private String emptyTooltip = null;

    public ToolFilterSlot(Supplier<ItemStack> getTool, Consumer<ItemStack> setTool, Predicate<ItemStack> isToolTypeValid) {
        super(EMPTY_INVENTORY, 0, -100, -100);
        this.getTool = getTool;
        this.setTool = setTool;
        this.isToolTypeValid = isToolTypeValid;
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return getTool.get();
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        setTool.accept(stack);
    }

    @Override
    public void onSlotChanged() {
        //noop
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        if (amount > 0) {
            putStack(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return false;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return stack.isEmpty() || (stack.getMaxStackSize() == 1 && isToolTypeValid.test(stack));
    }

    @Nullable
    @Override
    public String getEmptyTooltip() {
        return emptyTooltip;
    }

    public void setEmptyTooltip(String emptyTooltip) {
        this.emptyTooltip = emptyTooltip;
    }
}
