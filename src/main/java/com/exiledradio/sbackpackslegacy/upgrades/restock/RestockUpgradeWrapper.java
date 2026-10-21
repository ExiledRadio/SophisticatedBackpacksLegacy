/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.restock;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.IItemHandlerInteractionUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.inventory.FilteredItemHandler;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogic;
import com.exiledradio.scorelegacy.upgrades.IContentsFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RestockUpgradeWrapper extends UpgradeWrapperBase<RestockUpgradeWrapper, RestockUpgradeItem>
        implements IContentsFilteredUpgrade, IItemHandlerInteractionUpgrade {
    private final ContentsFilterLogic filterLogic;

    public RestockUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new ContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), storageWrapper::getInventoryHandler);
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player) {
        AtomicInteger stacksAdded = new AtomicInteger(0);
        InventoryHelper.transfer(itemHandler,
                new FilteredItemHandler(storageWrapper.getInventoryForUpgradeProcessing(), Collections.singletonList(filterLogic), Collections.emptyList()),
                s -> stacksAdded.incrementAndGet());
        int stacksRestocked = stacksAdded.get();
        String key = stacksRestocked > 0 ? "gui.sbackpackslegacy.status.stacks_restocked" : "gui.sbackpackslegacy.status.nothing_to_restock";
        player.sendStatusMessage(new TextComponentTranslation(key, stacksRestocked), true);
    }
}
