/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.deposit;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.IItemHandlerInteractionUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.inventory.FilteredItemHandler;
import com.exiledradio.scorelegacy.upgrades.IFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DepositUpgradeWrapper extends UpgradeWrapperBase<DepositUpgradeWrapper, DepositUpgradeItem>
        implements IFilteredUpgrade, IItemHandlerInteractionUpgrade {
    private final DepositFilterLogic filterLogic;

    public DepositUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new DepositFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount());
    }

    @Override
    public DepositFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player) {
        if (filterLogic.getDepositFilterType() == DepositFilterType.INVENTORY) {
            filterLogic.setInventory(itemHandler);
        }
        AtomicInteger stacksAdded = new AtomicInteger(0);
        InventoryHelper.transfer(storageWrapper.getInventoryForUpgradeProcessing(),
                new FilteredItemHandler(itemHandler, Collections.singletonList(filterLogic), Collections.emptyList()),
                s -> stacksAdded.incrementAndGet());
        int stacksDeposited = stacksAdded.get();
        String key = stacksDeposited > 0 ? "gui.sbackpackslegacy.status.stacks_deposited" : "gui.sbackpackslegacy.status.nothing_to_deposit";
        player.sendStatusMessage(new TextComponentTranslation(key, stacksDeposited), true);
    }
}
