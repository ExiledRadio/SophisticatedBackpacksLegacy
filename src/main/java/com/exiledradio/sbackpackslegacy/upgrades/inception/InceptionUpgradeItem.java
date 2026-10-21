/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.inception;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Lets the backpack hold other backpacks (one level deep), whose upgrades and inventories then join in. */
public class InceptionUpgradeItem extends UpgradeItemBase<InceptionUpgradeWrapper> {
    public static final UpgradeType<InceptionUpgradeWrapper> TYPE = new UpgradeType<>(InceptionUpgradeWrapper::new);

    public InceptionUpgradeItem(String name, CreativeTabs tab) {
        super(name, tab);
    }

    @Override
    public UpgradeType<InceptionUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelBackpack) {
        if (!firstLevelBackpack) {
            return UpgradeSlotChangeResult.fail("add.inception_sub_backpack", Collections.emptySet(), Collections.emptySet());
        }
        Set<Integer> errorUpgradeSlots = new HashSet<>();
        for (Map.Entry<Integer, IUpgradeWrapper> entry : storageWrapper.getUpgradeHandler().getSlotWrappers().entrySet()) {
            if (entry.getValue() instanceof InceptionUpgradeWrapper) {
                errorUpgradeSlots.add(entry.getKey());
            }
        }
        if (!errorUpgradeSlots.isEmpty()) {
            return UpgradeSlotChangeResult.fail("add.inception_exists", errorUpgradeSlots, Collections.emptySet());
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper) {
        Set<Integer> slots = InventoryHelper.getItemSlots(storageWrapper.getInventoryHandler(), stack -> stack.getItem() instanceof BackpackItem);
        if (!slots.isEmpty()) {
            return UpgradeSlotChangeResult.fail("remove.inception_sub_backpack", Collections.emptySet(), slots);
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }
}
