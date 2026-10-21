/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.init;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerRegistry;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.upgrades.ContentsFilteredUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.FilteredUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.compacting.CompactingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.compacting.CompactingUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.compacting.CompactingUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.deposit.DepositUpgradeContainer;
import com.exiledradio.sbackpackslegacy.upgrades.deposit.DepositUpgradeItem;
import com.exiledradio.sbackpackslegacy.upgrades.deposit.DepositUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.everlasting.EverlastingUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.feeding.FeedingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.feeding.FeedingUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.feeding.FeedingUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.filter.FilterUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.filter.FilterUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.cooking.AutoSmeltingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.cooking.AutoSmeltingUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.cooking.AutoSmeltingUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.cooking.SmeltingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.cooking.SmeltingUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.cooking.SmeltingUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapperUpgradeContainer;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapperUpgradeItem;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapperUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.jukebox.JukeboxUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.jukebox.JukeboxUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.jukebox.JukeboxUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.pump.PumpUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.pump.PumpUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.pump.PumpUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.xppump.XpPumpUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.xppump.XpPumpUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.xppump.XpPumpUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.crafting.CraftingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.crafting.CraftingUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.crafting.CraftingUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.inception.InceptionUpgradeContainer;
import com.exiledradio.sbackpackslegacy.upgrades.inception.InceptionUpgradeItem;
import com.exiledradio.sbackpackslegacy.upgrades.inception.InceptionUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.magnet.MagnetUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.magnet.MagnetUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.magnet.MagnetUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.pickup.PickupUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.pickup.PickupUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.refill.RefillUpgradeItem;
import com.exiledradio.sbackpackslegacy.upgrades.refill.RefillUpgradeWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.restock.RestockUpgradeItem;
import com.exiledradio.sbackpackslegacy.upgrades.restock.RestockUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.stack.StackUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.voiding.VoidUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.voiding.VoidUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.voiding.VoidUpgradeWrapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Every upgrade item, and which settings-tab container each one gets. */
public final class ModUpgrades {
    public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_BASIC = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
    public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_ADVANCED = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
    public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_BASIC = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
    public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_ADVANCED = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
    public static final UpgradeContainerType<FeedingUpgradeWrapper, FeedingUpgradeContainer> FEEDING_BASIC = new UpgradeContainerType<>(FeedingUpgradeContainer::new);
    public static final UpgradeContainerType<FeedingUpgradeWrapper, FeedingUpgradeContainer> FEEDING_ADVANCED = new UpgradeContainerType<>(FeedingUpgradeContainer::new);
    public static final UpgradeContainerType<CompactingUpgradeWrapper, CompactingUpgradeContainer> COMPACTING_BASIC = new UpgradeContainerType<>(CompactingUpgradeContainer::new);
    public static final UpgradeContainerType<CompactingUpgradeWrapper, CompactingUpgradeContainer> COMPACTING_ADVANCED = new UpgradeContainerType<>(CompactingUpgradeContainer::new);
    public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> VOID_BASIC = new UpgradeContainerType<>(VoidUpgradeContainer::new);
    public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> VOID_ADVANCED = new UpgradeContainerType<>(VoidUpgradeContainer::new);
    public static final UpgradeContainerType<RestockUpgradeWrapper, ContentsFilteredUpgradeContainer<RestockUpgradeWrapper>> RESTOCK_BASIC = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
    public static final UpgradeContainerType<RestockUpgradeWrapper, ContentsFilteredUpgradeContainer<RestockUpgradeWrapper>> RESTOCK_ADVANCED = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
    public static final UpgradeContainerType<DepositUpgradeWrapper, DepositUpgradeContainer> DEPOSIT_BASIC = new UpgradeContainerType<>(DepositUpgradeContainer::new);
    public static final UpgradeContainerType<DepositUpgradeWrapper, DepositUpgradeContainer> DEPOSIT_ADVANCED = new UpgradeContainerType<>(DepositUpgradeContainer::new);
    public static final UpgradeContainerType<RefillUpgradeWrapper, FilteredUpgradeContainer<RefillUpgradeWrapper>> REFILL = new UpgradeContainerType<>(FilteredUpgradeContainer::new);
    public static final UpgradeContainerType<InceptionUpgradeWrapper, InceptionUpgradeContainer> INCEPTION = new UpgradeContainerType<>(InceptionUpgradeContainer::new);
    public static final UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> CRAFTING = new UpgradeContainerType<>(CraftingUpgradeContainer::new);
    public static final UpgradeContainerType<SmeltingUpgradeWrapper, SmeltingUpgradeContainer> SMELTING = new UpgradeContainerType<>(SmeltingUpgradeContainer::new);
    public static final UpgradeContainerType<AutoSmeltingUpgradeWrapper, AutoSmeltingUpgradeContainer> AUTO_SMELTING = new UpgradeContainerType<>(AutoSmeltingUpgradeContainer::new);
    public static final UpgradeContainerType<com.exiledradio.scorelegacy.upgrades.stonecutter.StonecutterUpgradeWrapper, com.exiledradio.scorelegacy.upgrades.stonecutter.StonecutterUpgradeContainer> STONECUTTER =
            new UpgradeContainerType<>(com.exiledradio.scorelegacy.upgrades.stonecutter.StonecutterUpgradeContainer::new);
    public static final UpgradeContainerType<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeContainer> ADVANCED_TOOL_SWAPPER = new UpgradeContainerType<>(ToolSwapperUpgradeContainer::new);
    public static final UpgradeContainerType<JukeboxUpgradeWrapper, JukeboxUpgradeContainer> JUKEBOX = new UpgradeContainerType<>(JukeboxUpgradeContainer::new);
    public static final UpgradeContainerType<TankUpgradeWrapper, TankUpgradeContainer> TANK = new UpgradeContainerType<>(TankUpgradeContainer::new);
    public static final UpgradeContainerType<BatteryUpgradeWrapper, BatteryUpgradeContainer> BATTERY = new UpgradeContainerType<>(BatteryUpgradeContainer::new);
    public static final UpgradeContainerType<PumpUpgradeWrapper, PumpUpgradeContainer> PUMP_BASIC = new UpgradeContainerType<>(PumpUpgradeContainer::new);
    public static final UpgradeContainerType<PumpUpgradeWrapper, PumpUpgradeContainer> PUMP_ADVANCED = new UpgradeContainerType<>(PumpUpgradeContainer::new);
    public static final UpgradeContainerType<XpPumpUpgradeWrapper, XpPumpUpgradeContainer> XP_PUMP = new UpgradeContainerType<>(XpPumpUpgradeContainer::new);

    private static final List<Item> UPGRADE_ITEMS = new ArrayList<>();

    private ModUpgrades() {}

    public static List<Item> getUpgradeItems() {
        return Collections.unmodifiableList(UPGRADE_ITEMS);
    }

    public static void registerItems(IForgeRegistry<Item> registry, CreativeTabs tab) {
        ModConfig.Upgrades config = ModConfig.upgrades;
        register(registry, new BaseItem("upgrade_base", tab));
        register(registry, new PickupUpgradeItem("pickup_upgrade", tab, () -> config.pickup.filterSlots), PICKUP_BASIC);
        register(registry, new PickupUpgradeItem("advanced_pickup_upgrade", tab, () -> config.advancedPickup.filterSlots), PICKUP_ADVANCED);
        register(registry, new FilterUpgradeItem("filter_upgrade", tab, () -> config.filter.filterSlots), FilterUpgradeContainer.BASIC_TYPE);
        register(registry, new FilterUpgradeItem("advanced_filter_upgrade", tab, () -> config.advancedFilter.filterSlots), FilterUpgradeContainer.ADVANCED_TYPE);
        register(registry, new MagnetUpgradeItem("magnet_upgrade", tab, () -> config.magnet.magnetRange, () -> config.magnet.filterSlots), MAGNET_BASIC);
        register(registry, new MagnetUpgradeItem("advanced_magnet_upgrade", tab, () -> config.advancedMagnet.magnetRange, () -> config.advancedMagnet.filterSlots), MAGNET_ADVANCED);
        register(registry, new FeedingUpgradeItem("feeding_upgrade", tab, () -> config.feeding.filterSlots), FEEDING_BASIC);
        register(registry, new FeedingUpgradeItem("advanced_feeding_upgrade", tab, () -> config.advancedFeeding.filterSlots), FEEDING_ADVANCED);
        register(registry, new CompactingUpgradeItem("compacting_upgrade", tab, false, () -> config.compacting.filterSlots), COMPACTING_BASIC);
        register(registry, new CompactingUpgradeItem("advanced_compacting_upgrade", tab, true, () -> config.advancedCompacting.filterSlots), COMPACTING_ADVANCED);
        register(registry, new VoidUpgradeItem("void_upgrade", tab, () -> config.voiding.filterSlots), VOID_BASIC);
        register(registry, new VoidUpgradeItem("advanced_void_upgrade", tab, () -> config.advancedVoiding.filterSlots), VOID_ADVANCED);
        register(registry, new RestockUpgradeItem("restock_upgrade", tab, () -> config.restock.filterSlots), RESTOCK_BASIC);
        register(registry, new RestockUpgradeItem("advanced_restock_upgrade", tab, () -> config.advancedRestock.filterSlots), RESTOCK_ADVANCED);
        register(registry, new DepositUpgradeItem("deposit_upgrade", tab, () -> config.deposit.filterSlots), DEPOSIT_BASIC);
        register(registry, new DepositUpgradeItem("advanced_deposit_upgrade", tab, () -> config.advancedDeposit.filterSlots), DEPOSIT_ADVANCED);
        register(registry, new RefillUpgradeItem("refill_upgrade", tab), REFILL);
        register(registry, new InceptionUpgradeItem("inception_upgrade", tab), INCEPTION);
        register(registry, new CraftingUpgradeItem("crafting_upgrade", tab), CRAFTING);
        register(registry, new SmeltingUpgradeItem("smelting_upgrade", tab, config.smelting), SMELTING);
        register(registry, new AutoSmeltingUpgradeItem("auto_smelting_upgrade", tab, config.autoSmelting), AUTO_SMELTING);
        // From the original: listed and craftable only when a mod adds a smoker, blast furnace or stonecutter.
        register(registry, new SmeltingUpgradeItem("smoking_upgrade", tab, config.smoking, com.exiledradio.scorelegacy.upgrades.cooking.CookingType.SMOKING), SMELTING);
        register(registry, new AutoSmeltingUpgradeItem("auto_smoking_upgrade", tab, config.autoSmoking, com.exiledradio.scorelegacy.upgrades.cooking.CookingType.SMOKING), AUTO_SMELTING);
        register(registry, new SmeltingUpgradeItem("blasting_upgrade", tab, config.blasting, com.exiledradio.scorelegacy.upgrades.cooking.CookingType.BLASTING), SMELTING);
        register(registry, new AutoSmeltingUpgradeItem("auto_blasting_upgrade", tab, config.autoBlasting, com.exiledradio.scorelegacy.upgrades.cooking.CookingType.BLASTING), AUTO_SMELTING);
        register(registry, new com.exiledradio.scorelegacy.upgrades.stonecutter.StonecutterUpgradeItem("stonecutter_upgrade", tab), STONECUTTER);
        register(registry, new ToolSwapperUpgradeItem("tool_swapper_upgrade", tab, false, false));
        register(registry, new ToolSwapperUpgradeItem("advanced_tool_swapper_upgrade", tab, true, true), ADVANCED_TOOL_SWAPPER);
        register(registry, new JukeboxUpgradeItem("jukebox_upgrade", tab), JUKEBOX);
        register(registry, new TankUpgradeItem("tank_upgrade", tab, config.tank), TANK);
        register(registry, new BatteryUpgradeItem("battery_upgrade", tab, config.battery), BATTERY);
        register(registry, new PumpUpgradeItem("pump_upgrade", tab, false, false, config.pump), PUMP_BASIC);
        register(registry, new PumpUpgradeItem("advanced_pump_upgrade", tab, true, true, config.pump), PUMP_ADVANCED);
        register(registry, new XpPumpUpgradeItem("xp_pump_upgrade", tab, config.xpPump), XP_PUMP);
        register(registry, new EverlastingUpgradeItem("everlasting_upgrade", tab));
        register(registry, new StackUpgradeItem("stack_upgrade_tier_1", tab, 2));
        register(registry, new StackUpgradeItem("stack_upgrade_tier_2", tab, 4));
        register(registry, new StackUpgradeItem("stack_upgrade_tier_3", tab, 8));
        register(registry, new StackUpgradeItem("stack_upgrade_tier_4", tab, 16));
    }

    private static void register(IForgeRegistry<Item> registry, Item item) {
        registry.register(item);
        UPGRADE_ITEMS.add(item);
    }

    private static void register(IForgeRegistry<Item> registry, Item item, UpgradeContainerType<?, ?> containerType) {
        register(registry, item);
        UpgradeContainerRegistry.register(item, containerType);
    }

    /** The plain crafting ingredient every upgrade starts from. */
    private static class BaseItem extends Item {
        BaseItem(String name, CreativeTabs tab) {
            setRegistryName(com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy.MODID, name);
            setTranslationKey(com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy.MODID + "." + name);
            setCreativeTab(tab);
        }
    }
}
