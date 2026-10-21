/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.everlasting;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

/** A dropped backpack with this upgrade can't be destroyed and never despawns. */
public class EverlastingUpgradeItem extends UpgradeItemBase<EverlastingUpgradeItem.Wrapper> {
    public static final UpgradeType<Wrapper> TYPE = new UpgradeType<>(Wrapper::new);

    public EverlastingUpgradeItem(String name, CreativeTabs tab) {
        super(name, tab);
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    public static class Wrapper extends UpgradeWrapperBase<Wrapper, EverlastingUpgradeItem> {
        public Wrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public boolean hideSettingsTab() {
            return true;
        }
    }
}
