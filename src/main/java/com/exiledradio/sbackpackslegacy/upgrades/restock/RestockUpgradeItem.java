/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.restock;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.IntSupplier;

/** Sneak-right-click an inventory with the backpack to pull matching items from it into the backpack. */
public class RestockUpgradeItem extends UpgradeItemBase<RestockUpgradeWrapper> {
    public static final UpgradeType<RestockUpgradeWrapper> TYPE = new UpgradeType<>(RestockUpgradeWrapper::new);
    private final IntSupplier filterSlotCount;

    public RestockUpgradeItem(String name, CreativeTabs tab, IntSupplier filterSlotCount) {
        super(name, tab);
        this.filterSlotCount = filterSlotCount;
    }

    @Override
    public UpgradeType<RestockUpgradeWrapper> getType() {
        return TYPE;
    }

    public int getFilterSlotCount() {
        return filterSlotCount.getAsInt();
    }
}
