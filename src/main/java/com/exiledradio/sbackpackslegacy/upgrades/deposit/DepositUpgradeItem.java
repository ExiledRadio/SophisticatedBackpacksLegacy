/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.deposit;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

/** Sneak-right-click an inventory with the backpack to move matching items from the backpack into it. */
public class DepositUpgradeItem extends UpgradeItemBase<DepositUpgradeWrapper> {
    public static final UpgradeType<DepositUpgradeWrapper> TYPE = new UpgradeType<>(DepositUpgradeWrapper::new);
    private final IntSupplier filterSlotCount;

    public DepositUpgradeItem(String name, CreativeTabs tab, IntSupplier filterSlotCount) {
        super(name, tab);
        this.filterSlotCount = filterSlotCount;
    }

    @Override
    public UpgradeType<DepositUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }
}
