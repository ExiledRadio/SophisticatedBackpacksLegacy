/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.refill;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.upgrades.FilterItemStackHandler;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.IFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class RefillUpgradeWrapper extends UpgradeWrapperBase<RefillUpgradeWrapper, RefillUpgradeItem> implements IFilteredUpgrade, ITickableUpgrade {
    private static final int COOLDOWN = 5;
    private final FilterLogic filterLogic;

    public RefillUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, ModConfig.upgrades.refill.filterSlots);
        filterLogic.setAllowByDefault();
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (!(entity instanceof EntityPlayer) || isInCooldown(world)) {
            return;
        }
        IItemHandler playerInventory = new PlayerMainInvWrapper(((EntityPlayer) entity).inventory);
        FilterItemStackHandler filters = filterLogic.getFilterHandler();
        for (int slot = 0; slot < filters.getSlots(); slot++) {
            ItemStack filter = filters.getStackInSlot(slot);
            if (!filter.isEmpty()) {
                refill(playerInventory, filter);
            }
        }
        setCooldown(world, COOLDOWN);
    }

    private void refill(IItemHandler playerInventory, ItemStack filter) {
        int missingCount = InventoryHelper.getCountMissingInHandler(playerInventory, filter, filter.getMaxStackSize());
        if (missingCount == 0) {
            return;
        }
        IItemHandler backpackInventory = storageWrapper.getInventoryForUpgradeProcessing();
        ItemStack toMove = filter.copy();
        toMove.setCount(missingCount);
        ItemStack extracted = InventoryHelper.extractFromInventory(toMove, backpackInventory, true);
        if (extracted.isEmpty()) {
            return;
        }
        if (missingCount < filter.getMaxStackSize()) {
            // Top up the stack that is already there rather than starting a second one.
            for (int slot = 0; slot < playerInventory.getSlots(); slot++) {
                if (ItemHandlerHelper.canItemStacksStack(playerInventory.getStackInSlot(slot), extracted)) {
                    playerInventory.insertItem(slot, InventoryHelper.extractFromInventory(extracted, backpackInventory, false), false);
                    return;
                }
            }
            return;
        }
        ItemStack remaining = InventoryHelper.insertIntoInventory(extracted, playerInventory, true);
        if (remaining.getCount() == extracted.getCount()) {
            return;
        }
        toMove.setCount(extracted.getCount() - remaining.getCount());
        InventoryHelper.insertIntoInventory(InventoryHelper.extractFromInventory(toMove, backpackInventory, false), playerInventory, false);
    }
}
