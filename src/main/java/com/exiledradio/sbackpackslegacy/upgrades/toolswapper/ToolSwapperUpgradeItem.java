/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Only one tool swapper (basic or advanced) per backpack: two would fight over the hand. */
public class ToolSwapperUpgradeItem extends UpgradeItemBase<ToolSwapperUpgradeWrapper> {
    public static final UpgradeType<ToolSwapperUpgradeWrapper> TYPE = new UpgradeType<>(ToolSwapperUpgradeWrapper::new);
    private final boolean hasSettingsTab;
    private final boolean swapToolOnKeyPress;

    public ToolSwapperUpgradeItem(String name, CreativeTabs tab, boolean hasSettingsTab, boolean swapToolOnKeyPress) {
        super(name, tab);
        this.hasSettingsTab = hasSettingsTab;
        this.swapToolOnKeyPress = swapToolOnKeyPress;
    }

    @Override
    public UpgradeType<ToolSwapperUpgradeWrapper> getType() {
        return TYPE;
    }

    public boolean hasSettingsTab() {
        return hasSettingsTab;
    }

    public boolean shouldSwapToolOnKeyPress() {
        return swapToolOnKeyPress;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelBackpack) {
        Set<Integer> errorUpgradeSlots = new HashSet<>();
        for (Map.Entry<Integer, IUpgradeWrapper> entry : storageWrapper.getUpgradeHandler().getSlotWrappers().entrySet()) {
            if (entry.getValue() instanceof ToolSwapperUpgradeWrapper) {
                errorUpgradeSlots.add(entry.getKey());
            }
        }
        if (!errorUpgradeSlots.isEmpty()) {
            return UpgradeSlotChangeResult.fail("add.tool_swapper_exists", errorUpgradeSlots, Collections.emptySet());
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }
}
