/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.gui.filter;

import com.exiledradio.scorelegacy.client.gui.filter.FilterLogicControl;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.sbackpackslegacy.upgrades.deposit.DepositFilterLogic;
import com.exiledradio.sbackpackslegacy.upgrades.deposit.DepositFilterLogicContainer;
import com.exiledradio.sbackpackslegacy.upgrades.deposit.DepositFilterType;
import com.google.common.collect.ImmutableMap;

public abstract class DepositFilterLogicControl extends FilterLogicControl<DepositFilterLogic, DepositFilterLogicContainer> {
    public static final ButtonDefinition.Toggle<DepositFilterType> DEPOSIT_FILTER_TYPE = ButtonDefinitions.createToggleButtonDefinition(
            ImmutableMap.of(
                    DepositFilterType.ALLOW, ButtonDefinitions.icon(0, 0, "allow"),
                    DepositFilterType.BLOCK, ButtonDefinitions.icon(16, 0, "block"),
                    DepositFilterType.INVENTORY, ButtonDefinitions.icon(64, 16, "deposit_filter_type_inventory")));

    protected DepositFilterLogicControl(StorageScreenBase screen, Position position, DepositFilterLogicContainer filterLogicContainer, int slotsPerRow, MatchButton... matchButtons) {
        super(screen, position, filterLogicContainer, slotsPerRow, true, matchButtons);
        addChild(new ToggleButton<>(new Position(x, y), DEPOSIT_FILTER_TYPE, button -> updateDepositFilterType(), container::getDepositFilterType));
    }

    private void updateDepositFilterType() {
        DepositFilterType next = container.getDepositFilterType().next();
        container.setDepositFilterType(next);
        container.getFilterSlots().forEach(slot -> slot.setEnabled(next != DepositFilterType.INVENTORY));
    }

    public static class Basic extends DepositFilterLogicControl {
        public Basic(StorageScreenBase screen, Position position, DepositFilterLogicContainer filterLogicContainer, int slotsPerRow) {
            super(screen, position, filterLogicContainer, slotsPerRow);
        }
    }

    public static class Advanced extends DepositFilterLogicControl {
        public Advanced(StorageScreenBase screen, Position position, DepositFilterLogicContainer filterLogicContainer, int slotsPerRow) {
            super(screen, position, filterLogicContainer, slotsPerRow, MatchButton.PRIMARY_MATCH, MatchButton.DURABILITY, MatchButton.NBT);
        }
    }
}
