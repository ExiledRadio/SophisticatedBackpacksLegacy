/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.gui.upgrades;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.filter.FilterLogicControlBase;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolFilterSlot;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapMode;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapperFilterContainer;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapperFilterLogic;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapperUpgradeContainer;
import com.google.common.collect.ImmutableMap;

import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.createToggleButtonDefinition;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.getBooleanStateData;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.iconWithDetail;

/** Settings of the advanced tool swapper: weapon swapping, tool swapping mode and the filters. */
public class ToolSwapperUpgradeTab extends UpgradeSettingsTab<ToolSwapperUpgradeContainer> {
    private static final ButtonDefinition.Toggle<Boolean> SWAP_WEAPON = createToggleButtonDefinition(
            getBooleanStateData(iconWithDetail(32, 64, "swap_weapon"), iconWithDetail(48, 64, "do_not_swap_weapon")));
    private static final ButtonDefinition.Toggle<ToolSwapMode> SWAP_TOOLS = createToggleButtonDefinition(ImmutableMap.of(
            ToolSwapMode.NO_SWAP, iconWithDetail(96, 64, "do_not_swap_tools"),
            ToolSwapMode.ONLY_TOOLS, iconWithDetail(80, 64, "only_swap_for_tools"),
            ToolSwapMode.ANY, iconWithDetail(64, 64, "swap_tools")));
    private final ToolSwapperFilterControl filterLogicControl;

    public ToolSwapperUpgradeTab(ToolSwapperUpgradeContainer container, Position position, StorageScreenBase screen) {
        super(container, position, screen, "advanced_tool_swapper");
        addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), SWAP_WEAPON,
                button -> container.setSwapWeapon(!container.shouldSwapWeapon()), container::shouldSwapWeapon));
        addHideableChild(new ToggleButton<>(new Position(x + 21, y + 24), SWAP_TOOLS,
                button -> container.setToolSwapMode(container.getToolSwapMode().next()), container::getToolSwapMode));
        filterLogicControl = addHideableChild(new ToolSwapperFilterControl(screen, container.getFilterLogicContainer(), new Position(x + 3, y + 44),
                ModConfig.upgrades.toolSwapperSlotsInRow));
    }

    @Override
    protected void moveSlotsToTab() {
        filterLogicControl.moveSlotsToView();
    }

    private static class ToolSwapperFilterControl extends FilterLogicControlBase<ToolSwapperFilterLogic, ToolFilterSlot, ToolSwapperFilterContainer> {
        ToolSwapperFilterControl(StorageScreenBase screen, ToolSwapperFilterContainer container, Position position, int slotsPerRow) {
            super(screen, container, position, true, slotsPerRow, MatchButton.ALLOW_LIST, MatchButton.PRIMARY_MATCH, MatchButton.DURABILITY, MatchButton.NBT);
        }
    }
}
