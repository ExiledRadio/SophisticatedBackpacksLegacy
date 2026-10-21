/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.deposit;

import com.exiledradio.scorelegacy.common.gui.IServerUpdater;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DepositFilterLogicContainer extends FilterLogicContainer<DepositFilterLogic> {
    private static final String DATA_DEPOSIT_FILTER_TYPE = "depositFilterType";

    public DepositFilterLogicContainer(Supplier<DepositFilterLogic> filterLogic, IServerUpdater serverUpdater, Consumer<Slot> addSlot) {
        super(filterLogic, serverUpdater, addSlot);
        if (getDepositFilterType() == DepositFilterType.INVENTORY) {
            getFilterSlots().forEach(s -> s.setEnabled(false));
        }
    }

    public void setDepositFilterType(DepositFilterType depositFilterType) {
        filterLogic.get().setDepositFilterType(depositFilterType);
        serverUpdater.sendDataToServer(() -> NBTHelper.withString(DATA_DEPOSIT_FILTER_TYPE, depositFilterType.getSerializedName()));
    }

    @Override
    public boolean handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_DEPOSIT_FILTER_TYPE)) {
            setDepositFilterType(DepositFilterType.fromName(data.getString(DATA_DEPOSIT_FILTER_TYPE)));
            return true;
        }
        return super.handleMessage(data);
    }

    public DepositFilterType getDepositFilterType() {
        return filterLogic.get().getDepositFilterType();
    }
}
