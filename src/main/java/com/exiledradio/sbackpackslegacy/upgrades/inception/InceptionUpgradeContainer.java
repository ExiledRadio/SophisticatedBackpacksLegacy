/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.inception;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class InceptionUpgradeContainer extends UpgradeContainerBase<InceptionUpgradeWrapper, InceptionUpgradeContainer> {
    private static final String DATA_INVENTORY_ORDER = "inventoryOrder";

    public InceptionUpgradeContainer(EntityPlayer player, int upgradeContainerId, InceptionUpgradeWrapper upgradeWrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<InceptionUpgradeWrapper, InceptionUpgradeContainer> type) {
        super(player, upgradeContainerId, upgradeWrapper, upgradeHandler, type);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_INVENTORY_ORDER)) {
            setInventoryOrder(InventoryOrder.fromName(data.getString(DATA_INVENTORY_ORDER)));
        }
    }

    public InventoryOrder getInventoryOrder() {
        return getUpgradeWrapper().getInventoryOrder();
    }

    public void setInventoryOrder(InventoryOrder inventoryOrder) {
        getUpgradeWrapper().setInventoryOrder(inventoryOrder);
        sendDataToServer(() -> NBTHelper.withString(DATA_INVENTORY_ORDER, inventoryOrder.getSerializedName()));
    }
}
