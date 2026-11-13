package com.exiledradio.sbackpackslegacy;

import com.exiledradio.scorelegacy.settings.Setting;
import net.minecraftforge.common.config.Config;
import com.exiledradio.scorelegacy.config.AutoCookingUpgradeConfig;
import com.exiledradio.scorelegacy.config.BatteryUpgradeConfig;
import com.exiledradio.scorelegacy.config.CookingUpgradeConfig;
import com.exiledradio.scorelegacy.config.FilteredUpgradeConfig;
import com.exiledradio.scorelegacy.config.MagnetUpgradeConfig;
import com.exiledradio.scorelegacy.config.PumpUpgradeConfig;
import com.exiledradio.scorelegacy.config.TankUpgradeConfig;
import com.exiledradio.scorelegacy.config.XpPumpUpgradeConfig;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = SophisticatedBackpacksLegacy.MODID)
@Mod.EventBusSubscriber(modid = SophisticatedBackpacksLegacy.MODID)
public class ModConfig {

    // Slot counts are written into each backpack's NBT the first time it is used, so changing
    // these only affects backpacks that haven't been opened yet (same as the original mod).

    @Config.Comment("Leather backpack")
    @Setting.Feature(icon = "sbackpackslegacy:backpack")
    public static final Tier leather = new Tier(27, 1);

    @Config.Comment("Copper backpack (listed when another mod adds copper)")
    @Setting.Feature(icon = "sbackpackslegacy:copper_backpack")
    public static final Tier copper = new Tier(45, 1);

    @Config.Comment("Iron backpack")
    @Setting.Feature(icon = "sbackpackslegacy:iron_backpack")
    public static final Tier iron = new Tier(54, 2);

    @Config.Comment("Gold backpack")
    @Setting.Feature(icon = "sbackpackslegacy:gold_backpack")
    public static final Tier gold = new Tier(81, 3);

    @Config.Comment("Diamond backpack")
    @Setting.Feature(icon = "sbackpackslegacy:diamond_backpack")
    public static final Tier diamond = new Tier(108, 5);

    @Config.Comment("Netherite backpack (listed when another mod adds netherite)")
    @Setting.Feature(icon = "sbackpackslegacy:netherite_backpack")
    public static final Tier netherite = new Tier(120, 7);

    @Config.Comment("Nether Star backpack (the top tier when no mod adds netherite)")
    @Setting.Feature(icon = "sbackpackslegacy:nether_star_backpack")
    public static final Tier nether_star = new Tier(120, 7);

    @Config.Comment("Add backpacks and a few upgrades to vanilla dungeon, mineshaft, temple, fortress, end city and mansion chests")
    @Config.RequiresWorldRestart
    public static boolean chestLootEnabled = true;

    @Config.Comment("Players can open the backpack another player is wearing by right-clicking it from behind")
    public static boolean allowOpeningOtherPlayerBackpacks = true;

    @Config.Comment("The backpack screen's \"Deposit to nearby chests\" and \"Merge backpacks\" buttons")
    @Setting.Feature(icon = "minecraft:chest")
    public static final NearbyActions nearbyActions = new NearbyActions();

    @Config.Comment("Upgrade settings")
    @Setting.Feature(icon = "sbackpackslegacy:upgrade_base")
    public static final Upgrades upgrades = new Upgrades();

    public static class NearbyActions {
        @Config.Comment({"How far from the player \"Deposit to nearby chests\" reaches, in blocks. Each item goes into nearby",
                "containers that already hold it; no-sort slots stay. 0 removes the button."})
        @Config.RangeInt(min = 0, max = 32)
        public int depositToNearbyChestsRange = 8;

        @Config.Comment({"How close another backpack (placed, or worn by a player) must be for \"Merge backpacks\", in blocks.",
                "Items of a kind the open backpack already holds come over; the other backpack's no-sort slots stay.",
                "Other players' worn backpacks only count while allowOpeningOtherPlayerBackpacks is on. 0 removes the button."})
        @Config.RangeInt(min = 0, max = 32)
        public int mergeBackpacksRange = 8;
    }

    public static class Upgrades {
        @Setting.Feature(icon = "sbackpackslegacy:pickup_upgrade")
        public final FilteredUpgradeConfig pickup = new FilteredUpgradeConfig(9, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_pickup_upgrade")
        public final FilteredUpgradeConfig advancedPickup = new FilteredUpgradeConfig(16, 4);
        @Setting.Feature(icon = "sbackpackslegacy:filter_upgrade")
        public final FilteredUpgradeConfig filter = new FilteredUpgradeConfig(9, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_filter_upgrade")
        public final FilteredUpgradeConfig advancedFilter = new FilteredUpgradeConfig(16, 4);
        @Setting.Feature(icon = "sbackpackslegacy:magnet_upgrade")
        public final MagnetUpgradeConfig magnet = new MagnetUpgradeConfig(9, 3, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_magnet_upgrade")
        public final MagnetUpgradeConfig advancedMagnet = new MagnetUpgradeConfig(16, 4, 5);
        @Setting.Feature(icon = "sbackpackslegacy:feeding_upgrade")
        public final FilteredUpgradeConfig feeding = new FilteredUpgradeConfig(9, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_feeding_upgrade")
        public final FilteredUpgradeConfig advancedFeeding = new FilteredUpgradeConfig(16, 4);
        @Setting.Feature(icon = "sbackpackslegacy:compacting_upgrade")
        public final FilteredUpgradeConfig compacting = new FilteredUpgradeConfig(9, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_compacting_upgrade")
        public final FilteredUpgradeConfig advancedCompacting = new FilteredUpgradeConfig(16, 4);

        @Config.Comment({"Items the compacting upgrades compact even though no recipe crafts them back, so the basic",
                "upgrade handles them too (registry name, optionally @metadata; a trailing * matches any name",
                "starting with the rest). Default: NoTreePunching's rocks."})
        @Setting.ListOf(Setting.Kind.ITEM)
        public String[] alwaysCompactedItems = {"notreepunching:rock/*"};
        @Setting.Feature(icon = "sbackpackslegacy:void_upgrade")
        public final FilteredUpgradeConfig voiding = new FilteredUpgradeConfig(9, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_void_upgrade")
        public final FilteredUpgradeConfig advancedVoiding = new FilteredUpgradeConfig(16, 4);
        @Setting.Feature(icon = "sbackpackslegacy:restock_upgrade")
        public final FilteredUpgradeConfig restock = new FilteredUpgradeConfig(9, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_restock_upgrade")
        public final FilteredUpgradeConfig advancedRestock = new FilteredUpgradeConfig(16, 4);
        @Setting.Feature(icon = "sbackpackslegacy:deposit_upgrade")
        public final FilteredUpgradeConfig deposit = new FilteredUpgradeConfig(9, 3);
        @Setting.Feature(icon = "sbackpackslegacy:advanced_deposit_upgrade")
        public final FilteredUpgradeConfig advancedDeposit = new FilteredUpgradeConfig(16, 4);
        @Setting.Feature(icon = "sbackpackslegacy:refill_upgrade")
        public final FilteredUpgradeConfig refill = new FilteredUpgradeConfig(6, 3);
        @Setting.Feature(icon = "sbackpackslegacy:smelting_upgrade")
        public final CookingUpgradeConfig smelting = new CookingUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:auto_smelting_upgrade")
        public final AutoCookingUpgradeConfig autoSmelting = new AutoCookingUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:smoking_upgrade")
        public final CookingUpgradeConfig smoking = new CookingUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:auto_smoking_upgrade")
        public final AutoCookingUpgradeConfig autoSmoking = new AutoCookingUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:blasting_upgrade")
        public final CookingUpgradeConfig blasting = new CookingUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:auto_blasting_upgrade")
        public final AutoCookingUpgradeConfig autoBlasting = new AutoCookingUpgradeConfig();

        @Setting.Feature(icon = "sbackpackslegacy:tank_upgrade")
        public final TankUpgradeConfig tank = new TankUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:battery_upgrade")
        public final BatteryUpgradeConfig battery = new BatteryUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:pump_upgrade")
        public final PumpUpgradeConfig pump = new PumpUpgradeConfig();
        @Setting.Feature(icon = "sbackpackslegacy:xp_pump_upgrade")
        public final XpPumpUpgradeConfig xpPump = new XpPumpUpgradeConfig();

        @Config.Comment({"Let other mods fill and drain a backpack's tanks while it is an item (in a tank or machine slot).",
                "On by default, as in the original mod. The original warns that some mods' item fluid handling can be",
                "abused to duplicate fluids with it; turn this off if that happens in your pack.",
                "A placed backpack always exposes its tanks."})
        public boolean itemFluidHandlerEnabled = true;

        @Config.Comment("Number of tool filter slots shown in a row in the Advanced Tool Swapper")
        @Config.RangeInt(min = 1, max = 6)
        @Setting.Client
        public int toolSwapperSlotsInRow = 4;

        @Config.Comment("Items that never stack beyond their normal size, even with stack upgrades (registry names, e.g. minecraft:shulker_box)")
        @Setting.ListOf(Setting.Kind.ITEM)
        public String[] nonStackableItems = new String[0];

        @Config.Comment("Let backpack upgrades use the inventories of backpacks inside a backpack with the Inception Upgrade")
        public boolean inceptionUsesInventoriesOfContainedBackpacks = true;

        @Config.Comment("Let upgrades inside backpacks that are inside a backpack with the Inception Upgrade keep working")
        public boolean inceptionContainedUpgradesAreFunctional = true;
    }









    public static class Tier {
        @Config.Comment("Number of inventory slots in the backpack")
        @Config.RangeInt(min = 1, max = 144)
        public int inventorySlotCount;

        @Config.Comment("Number of upgrade slots in the backpack")
        @Config.RangeInt(min = 0, max = 10)
        public int upgradeSlotCount;

        Tier(int inventorySlotCount, int upgradeSlotCount) {
            this.inventorySlotCount = inventorySlotCount;
            this.upgradeSlotCount = upgradeSlotCount;
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(SophisticatedBackpacksLegacy.MODID)) {
            ConfigManager.sync(SophisticatedBackpacksLegacy.MODID, Config.Type.INSTANCE);
        }
    }
}
