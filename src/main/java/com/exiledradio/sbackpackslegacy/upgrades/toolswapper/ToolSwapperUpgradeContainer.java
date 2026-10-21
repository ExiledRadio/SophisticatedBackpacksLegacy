/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class ToolSwapperUpgradeContainer extends UpgradeContainerBase<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeContainer> {
    private static final String DATA_SHOULD_SWAP_WEAPON = "shouldSwapWeapon";
    private static final String DATA_TOOL_SWAP_MODE = "toolSwapMode";
    private final ToolSwapperFilterContainer filterLogicContainer;

    public ToolSwapperUpgradeContainer(EntityPlayer player, int containerId, ToolSwapperUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                       UpgradeContainerType<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new ToolSwapperFilterContainer(this, () -> getUpgradeWrapper().getFilterLogic(), slots::add);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_SHOULD_SWAP_WEAPON)) {
            setSwapWeapon(data.getBoolean(DATA_SHOULD_SWAP_WEAPON));
        } else if (data.hasKey(DATA_TOOL_SWAP_MODE)) {
            setToolSwapMode(ToolSwapMode.fromName(data.getString(DATA_TOOL_SWAP_MODE)));
        } else {
            filterLogicContainer.handleMessage(data);
        }
    }

    public ToolSwapperFilterContainer getFilterLogicContainer() {
        return filterLogicContainer;
    }

    public void setSwapWeapon(boolean shouldSwapWeapon) {
        getUpgradeWrapper().setSwapWeapon(shouldSwapWeapon);
        sendBooleanToServer(DATA_SHOULD_SWAP_WEAPON, shouldSwapWeapon);
    }

    public boolean shouldSwapWeapon() {
        return getUpgradeWrapper().shouldSwapWeapon();
    }

    public void setToolSwapMode(ToolSwapMode toolSwapMode) {
        getUpgradeWrapper().setToolSwapMode(toolSwapMode);
        sendDataToServer(() -> NBTHelper.withString(DATA_TOOL_SWAP_MODE, toolSwapMode.getSerializedName()));
    }

    public ToolSwapMode getToolSwapMode() {
        return getUpgradeWrapper().getToolSwapMode();
    }
}
