/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.refill;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;

/** Keeps a full stack of each filter item in the player inventory, topped up from the backpack. */
public class RefillUpgradeItem extends UpgradeItemBase<RefillUpgradeWrapper> {
    public static final UpgradeType<RefillUpgradeWrapper> TYPE = new UpgradeType<>(RefillUpgradeWrapper::new);

    public RefillUpgradeItem(String name, CreativeTabs tab) {
        super(name, tab);
    }

    @Override
    public UpgradeType<RefillUpgradeWrapper> getType() {
        return TYPE;
    }
}
