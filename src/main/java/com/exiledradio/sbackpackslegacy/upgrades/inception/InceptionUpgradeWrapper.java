/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.inception;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.scorelegacy.api.IInventoryWrapperUpgrade;
import com.exiledradio.scorelegacy.api.IUpgradeAccessModifier;
import com.exiledradio.scorelegacy.api.IUpgradeWrapperAccessor;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class InceptionUpgradeWrapper extends UpgradeWrapperBase<InceptionUpgradeWrapper, InceptionUpgradeItem>
        implements IInventoryWrapperUpgrade, IUpgradeAccessModifier {
    @Nullable
    private SubBackpacksHandler subBackpacksHandler = null;

    public InceptionUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean hideSettingsTab() {
        return !ModConfig.upgrades.inceptionUsesInventoriesOfContainedBackpacks;
    }

    public InventoryOrder getInventoryOrder() {
        return NBTHelper.getEnum(upgrade, "inventoryOrder", InventoryOrder::fromName, InventoryOrder.MAIN_FIRST);
    }

    public void setInventoryOrder(InventoryOrder inventoryOrder) {
        NBTHelper.setEnum(upgrade, "inventoryOrder", inventoryOrder);
        save();
        storageWrapper.refreshInventoryForUpgradeProcessing();
    }

    private SubBackpacksHandler getSubBackpacksHandler() {
        if (subBackpacksHandler == null) {
            subBackpacksHandler = new SubBackpacksHandler(storageWrapper.getInventoryHandler());
        }
        return subBackpacksHandler;
    }

    @Override
    public IItemHandlerSimpleInserter wrapInventory(IItemHandlerSimpleInserter inventory) {
        if (ModConfig.upgrades.inceptionUsesInventoriesOfContainedBackpacks) {
            return new InceptionInventoryHandler(inventory, getInventoryOrder(), getSubBackpacksHandler());
        }
        return inventory;
    }

    @Override
    public IUpgradeWrapperAccessor wrapAccessor(IUpgradeWrapperAccessor upgradeWrapperAccessor) {
        if (ModConfig.upgrades.inceptionContainedUpgradesAreFunctional) {
            return new InceptionWrapperAccessor(storageWrapper, getSubBackpacksHandler());
        }
        return upgradeWrapperAccessor;
    }

    @Override
    public void onBeforeRemoved() {
        if (subBackpacksHandler != null) {
            subBackpacksHandler.dispose();
        }
    }
}
