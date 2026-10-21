/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.upgrades.BatteryInventoryPart;
import com.exiledradio.scorelegacy.client.gui.upgrades.CoreUpgradeTabs;
import com.exiledradio.scorelegacy.client.gui.upgrades.CookingUpgradeTabs;
import com.exiledradio.scorelegacy.client.gui.upgrades.CraftingUpgradeTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.IOSlotsTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.JukeboxUpgradeTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.PumpUpgradeTab;
import com.exiledradio.scorelegacy.client.gui.upgrades.TankInventoryPart;
import com.exiledradio.scorelegacy.client.gui.upgrades.XpPumpUpgradeTab;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeGuiManager;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.filter.ContentsFilterControl;
import com.exiledradio.sbackpackslegacy.client.gui.filter.DepositFilterLogicControl;
import com.exiledradio.scorelegacy.client.gui.filter.FilterLogicControl;
import com.exiledradio.scorelegacy.client.gui.filter.FilterLogicControlBase;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.sbackpackslegacy.init.ModUpgrades;
import com.exiledradio.scorelegacy.upgrades.ContentsFilteredUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import com.exiledradio.scorelegacy.upgrades.FilteredUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.compacting.CompactingUpgradeContainer;
import com.exiledradio.sbackpackslegacy.upgrades.deposit.DepositUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.feeding.FeedingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.feeding.HungerLevel;
import com.exiledradio.scorelegacy.upgrades.filter.FilterUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.filter.FilterUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.inception.InceptionUpgradeContainer;
import com.exiledradio.sbackpackslegacy.upgrades.inception.InventoryOrder;
import com.exiledradio.scorelegacy.upgrades.magnet.MagnetUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.pickup.PickupUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.refill.RefillUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.restock.RestockUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.voiding.VoidUpgradeContainer;
import com.google.common.collect.ImmutableMap;
import com.exiledradio.scorelegacy.upgrades.pump.PumpUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeContainer;

import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.createToggleButtonDefinition;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.getBooleanStateData;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.icon;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.iconWithDetail;

/** The settings tab of every upgrade, registered against its container type. */
public final class UpgradeTabs {

    private static final ButtonDefinition.Toggle<InventoryOrder> INVENTORY_ORDER = createToggleButtonDefinition(ImmutableMap.of(
            InventoryOrder.MAIN_FIRST, iconWithDetail(48, 32, "inventory_order_main_first"),
            InventoryOrder.INCEPTED_FIRST, iconWithDetail(64, 32, "inventory_order_incepted_first")));

    private UpgradeTabs() {}

    public static void register() {
        ModConfig.Upgrades c = ModConfig.upgrades;
        UpgradeGuiManager.registerTab(ModUpgrades.PICKUP_BASIC, (ContentsFilteredUpgradeContainer<PickupUpgradeWrapper> container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.ContentsTab<>(container, p, s, "pickup", false, c.pickup.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.PICKUP_ADVANCED, (ContentsFilteredUpgradeContainer<PickupUpgradeWrapper> container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.ContentsTab<>(container, p, s, "advanced_pickup", true, c.advancedPickup.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.RESTOCK_BASIC, (ContentsFilteredUpgradeContainer<RestockUpgradeWrapper> container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.ContentsTab<>(container, p, s, "restock", false, c.restock.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.RESTOCK_ADVANCED, (ContentsFilteredUpgradeContainer<RestockUpgradeWrapper> container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.ContentsTab<>(container, p, s, "advanced_restock", true, c.advancedRestock.slotsInRow));
        UpgradeGuiManager.registerTab(FilterUpgradeContainer.BASIC_TYPE, (FilterUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.FilterTab(container, p, s, "filter", false, c.filter.slotsInRow));
        UpgradeGuiManager.registerTab(FilterUpgradeContainer.ADVANCED_TYPE, (FilterUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.FilterTab(container, p, s, "advanced_filter", true, c.advancedFilter.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.MAGNET_BASIC, (MagnetUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.MagnetTab(container, p, s, "magnet", false, c.magnet.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.MAGNET_ADVANCED, (MagnetUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.MagnetTab(container, p, s, "advanced_magnet", true, c.advancedMagnet.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.VOID_BASIC, (VoidUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.VoidTab(container, p, s, "void", false, c.voiding.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.VOID_ADVANCED, (VoidUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.VoidTab(container, p, s, "advanced_void", true, c.advancedVoiding.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.DEPOSIT_BASIC, (DepositUpgradeContainer container, Position p, StorageScreenBase s) ->
                new DepositTab(container, p, s, "deposit", false, c.deposit.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.DEPOSIT_ADVANCED, (DepositUpgradeContainer container, Position p, StorageScreenBase s) ->
                new DepositTab(container, p, s, "advanced_deposit", true, c.advancedDeposit.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.REFILL, (FilteredUpgradeContainer<RefillUpgradeWrapper> container, Position p, StorageScreenBase s) ->
                new RefillTab(container, p, s, c.refill.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.FEEDING_BASIC, (FeedingUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.FeedingTab(container, p, s, "feeding", false, c.feeding.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.FEEDING_ADVANCED, (FeedingUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.FeedingTab(container, p, s, "advanced_feeding", true, c.advancedFeeding.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.COMPACTING_BASIC, (CompactingUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.CompactingTab(container, p, s, "compacting", false, c.compacting.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.COMPACTING_ADVANCED, (CompactingUpgradeContainer container, Position p, StorageScreenBase s) ->
                new CoreUpgradeTabs.CompactingTab(container, p, s, "advanced_compacting", true, c.advancedCompacting.slotsInRow));
        UpgradeGuiManager.registerTab(ModUpgrades.INCEPTION, (InceptionUpgradeContainer container, Position p, StorageScreenBase s) ->
                new InceptionTab(container, p, s));
        UpgradeGuiManager.registerTab(ModUpgrades.CRAFTING, CraftingUpgradeTab::new);
        UpgradeGuiManager.registerTab(ModUpgrades.SMELTING, CookingUpgradeTabs.SmeltingTab::new);
        UpgradeGuiManager.registerTab(ModUpgrades.AUTO_SMELTING, CookingUpgradeTabs.AutoSmeltingTab::new);
        UpgradeGuiManager.registerTab(ModUpgrades.ADVANCED_TOOL_SWAPPER, ToolSwapperUpgradeTab::new);
        UpgradeGuiManager.registerTab(ModUpgrades.JUKEBOX, JukeboxUpgradeTab::new);
        UpgradeGuiManager.registerTab(ModUpgrades.TANK, (TankUpgradeContainer container, Position p, StorageScreenBase s) -> new IOSlotsTab<>(container, p, s, "tank"));
        UpgradeGuiManager.registerTab(ModUpgrades.BATTERY, (BatteryUpgradeContainer container, Position p, StorageScreenBase s) -> new IOSlotsTab<>(container, p, s, "battery"));
        UpgradeGuiManager.registerInventoryPart(ModUpgrades.TANK, TankInventoryPart::new);
        UpgradeGuiManager.registerInventoryPart(ModUpgrades.BATTERY, BatteryInventoryPart::new);
        UpgradeGuiManager.registerTab(ModUpgrades.PUMP_BASIC, (PumpUpgradeContainer container, Position p, StorageScreenBase s) -> new PumpUpgradeTab(container, p, s, false));
        UpgradeGuiManager.registerTab(ModUpgrades.PUMP_ADVANCED, (PumpUpgradeContainer container, Position p, StorageScreenBase s) -> new PumpUpgradeTab(container, p, s, true));
        UpgradeGuiManager.registerTab(ModUpgrades.XP_PUMP, XpPumpUpgradeTab::new);
    }

    private static class DepositTab extends CoreUpgradeTabs.FilteredTab<DepositUpgradeContainer> {
        DepositTab(DepositUpgradeContainer container, Position position, StorageScreenBase screen, String name, boolean advanced, int slotsInRow) {
            super(container, position, screen, name);
            Position at = new Position(x + 3, y + CoreUpgradeTabs.TOP_ROW);
            filterLogicControl = addHideableChild(advanced
                    ? new DepositFilterLogicControl.Advanced(screen, at, container.getFilterLogicContainer(), slotsInRow)
                    : new DepositFilterLogicControl.Basic(screen, at, container.getFilterLogicContainer(), slotsInRow));
        }
    }

    private static class RefillTab extends CoreUpgradeTabs.FilteredTab<FilteredUpgradeContainer<RefillUpgradeWrapper>> {
        RefillTab(FilteredUpgradeContainer<RefillUpgradeWrapper> container, Position position, StorageScreenBase screen, int slotsInRow) {
            super(container, position, screen, "refill");
            filterLogicControl = addHideableChild(new FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>>(screen, new Position(x + 3, y + CoreUpgradeTabs.TOP_ROW),
                    container.getFilterLogicContainer(), slotsInRow));
        }
    }

    private static class InceptionTab extends UpgradeSettingsTab<InceptionUpgradeContainer> {
        InceptionTab(InceptionUpgradeContainer container, Position position, StorageScreenBase screen) {
            super(container, position, screen, "inception");
            addHideableChild(new ToggleButton<>(new Position(x + 3, y + CoreUpgradeTabs.TOP_ROW), INVENTORY_ORDER,
                    button -> container.setInventoryOrder(container.getInventoryOrder().next()), container::getInventoryOrder));
        }

        @Override
        protected void moveSlotsToTab() {
            //noop
        }
    }
}
