/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.deposit;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class DepositUpgradeContainer extends UpgradeContainerBase<DepositUpgradeWrapper, DepositUpgradeContainer> {
    private final DepositFilterLogicContainer filterLogicContainer;

    public DepositUpgradeContainer(EntityPlayer player, int containerId, DepositUpgradeWrapper wrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<DepositUpgradeWrapper, DepositUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new DepositFilterLogicContainer(() -> getUpgradeWrapper().getFilterLogic(), this, slots::add);
    }

    public DepositFilterLogicContainer getFilterLogicContainer() {
        return filterLogicContainer;
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        filterLogicContainer.handleMessage(data);
    }
}
