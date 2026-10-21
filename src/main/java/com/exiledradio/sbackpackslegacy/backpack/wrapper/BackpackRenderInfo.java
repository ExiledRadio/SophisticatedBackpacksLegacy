package com.exiledradio.sbackpackslegacy.backpack.wrapper;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;

import net.minecraft.item.ItemStack;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeWrapper;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeWrapper;
import net.minecraft.nbt.NBTTagCompound;

/**
 * What the backpack looks like beyond its tier and colours: tanks instead of side pouches (with
 * their fluid and how full they are, in tenths) and a battery instead of the front pouch (with
 * its charge, in quarters), as the original's render info. Kept in the stack NBT (it's tiny) so
 * every client renders it without needing the contents. Written from the installed tank and
 * battery upgrades whenever one changes.
 */
public final class BackpackRenderInfo {
    private static final String RENDER_INFO_TAG = "renderInfo";
    private static final String LEFT_TANK_TAG = "leftTank";
    private static final String RIGHT_TANK_TAG = "rightTank";
    private static final String BATTERY_TAG = "battery";
    private static final String LEFT_FLUID_TAG = "leftFluid";
    private static final String LEFT_FILL_TAG = "leftFill";
    private static final String RIGHT_FLUID_TAG = "rightFluid";
    private static final String RIGHT_FILL_TAG = "rightFill";
    private static final String BATTERY_CHARGE_TAG = "batteryCharge";
    private static final String ITEM_DISPLAY_TAG = "itemDisplay";
    private static final String ITEM_TAG = "item";
    private static final String ROTATION_TAG = "rotation";

    public static final BackpackRenderInfo DEFAULT = new BackpackRenderInfo(false, false, false);

    private final boolean leftTank;
    private final boolean rightTank;
    private final boolean battery;
    private final String leftFluid;
    private final float leftFill;
    private final String rightFluid;
    private final float rightFill;
    private final float batteryCharge;

    public BackpackRenderInfo(boolean leftTank, boolean rightTank, boolean battery) {
        this(leftTank, rightTank, battery, "", 0, "", 0, 0);
    }

    private BackpackRenderInfo(boolean leftTank, boolean rightTank, boolean battery, String leftFluid, float leftFill, String rightFluid,
                               float rightFill, float batteryCharge) {
        this.leftTank = leftTank;
        this.rightTank = rightTank;
        this.battery = battery;
        this.leftFluid = leftFluid;
        this.leftFill = leftFill;
        this.rightFluid = rightFluid;
        this.rightFill = rightFill;
        this.batteryCharge = batteryCharge;
    }

    public static BackpackRenderInfo of(ItemStack backpack) {
        NBTTagCompound tag = backpack.getTagCompound();
        if (tag == null || !tag.hasKey(RENDER_INFO_TAG)) {
            return DEFAULT;
        }
        NBTTagCompound info = tag.getCompoundTag(RENDER_INFO_TAG);
        return new BackpackRenderInfo(info.getBoolean(LEFT_TANK_TAG), info.getBoolean(RIGHT_TANK_TAG), info.getBoolean(BATTERY_TAG),
                info.getString(LEFT_FLUID_TAG), info.getFloat(LEFT_FILL_TAG), info.getString(RIGHT_FLUID_TAG), info.getFloat(RIGHT_FILL_TAG),
                info.getFloat(BATTERY_CHARGE_TAG));
    }

    /** Writes what the installed upgrades look like into the stack; true if that changed anything. */
    public static boolean update(ItemStack backpack, UpgradeHandler upgradeHandler) {
        java.util.List<TankUpgradeWrapper> tanks = upgradeHandler.getTypeWrappers(TankUpgradeItem.TYPE);
        java.util.List<BatteryUpgradeWrapper> batteries = upgradeHandler.getTypeWrappers(BatteryUpgradeItem.TYPE);
        BackpackRenderInfo updated = new BackpackRenderInfo(!tanks.isEmpty(), tanks.size() >= 2, !batteries.isEmpty(),
                tanks.isEmpty() ? "" : getFluidName(tanks.get(0)), tanks.isEmpty() ? 0 : getFillRatio(tanks.get(0)),
                tanks.size() < 2 ? "" : getFluidName(tanks.get(1)), tanks.size() < 2 ? 0 : getFillRatio(tanks.get(1)),
                batteries.isEmpty() ? 0 : getChargeRatio(batteries.get(0)));
        if (updated.sameLookAs(of(backpack))) {
            return false;
        }
        NBTTagCompound tag = backpack.getTagCompound();
        // Keep the item display that lives in the same compound.
        NBTTagCompound info = tag != null && tag.hasKey(RENDER_INFO_TAG) ? tag.getCompoundTag(RENDER_INFO_TAG) : new NBTTagCompound();
        info.setBoolean(LEFT_TANK_TAG, updated.leftTank);
        info.setBoolean(RIGHT_TANK_TAG, updated.rightTank);
        info.setBoolean(BATTERY_TAG, updated.battery);
        setOrRemove(info, LEFT_FLUID_TAG, LEFT_FILL_TAG, updated.leftFluid, updated.leftFill);
        setOrRemove(info, RIGHT_FLUID_TAG, RIGHT_FILL_TAG, updated.rightFluid, updated.rightFill);
        if (updated.battery) {
            info.setFloat(BATTERY_CHARGE_TAG, updated.batteryCharge);
        } else {
            info.removeTag(BATTERY_CHARGE_TAG);
        }
        backpack.setTagInfo(RENDER_INFO_TAG, info);
        return true;
    }

    private static void setOrRemove(NBTTagCompound info, String fluidTag, String fillTag, String fluid, float fill) {
        if (fluid.isEmpty() || fill <= 0) {
            info.removeTag(fluidTag);
            info.removeTag(fillTag);
        } else {
            info.setString(fluidTag, fluid);
            info.setFloat(fillTag, fill);
        }
    }

    private boolean sameLookAs(BackpackRenderInfo other) {
        return leftTank == other.leftTank && rightTank == other.rightTank && battery == other.battery
                && leftFluid.equals(other.leftFluid) && leftFill == other.leftFill && rightFluid.equals(other.rightFluid)
                && rightFill == other.rightFill && batteryCharge == other.batteryCharge;
    }

    private static String getFluidName(TankUpgradeWrapper tank) {
        net.minecraftforge.fluids.FluidStack contents = tank.getContents();
        return contents == null || contents.getFluid() == null ? "" : contents.getFluid().getName();
    }

    /** In tenths, as the original rounds it, so filling a tank doesn't re-sync the stack every millibucket. */
    private static float getFillRatio(TankUpgradeWrapper tank) {
        net.minecraftforge.fluids.FluidStack contents = tank.getContents();
        int capacity = tank.getTankCapacity();
        return contents == null || capacity <= 0 ? 0 : Math.round((float) contents.amount / capacity * 10) / 10F;
    }

    /** In quarters, as the original rounds it (the charge bar is four pixels long). */
    private static float getChargeRatio(BatteryUpgradeWrapper battery) {
        int capacity = battery.getMaxEnergyStored();
        return capacity <= 0 ? 0 : Math.round((float) battery.getEnergyStored() / capacity * 4) / 4F;
    }

    /**
     * Writes the item the front pouch shows (EMPTY for none) and its rotation in degrees; true if
     * that changed anything. Kept next to the tank/battery flags so every client can draw it.
     */
    public static boolean setItemDisplay(ItemStack backpack, ItemStack displayed, int rotation) {
        NBTTagCompound tag = backpack.getTagCompound();
        NBTTagCompound info = tag != null && tag.hasKey(RENDER_INFO_TAG) ? tag.getCompoundTag(RENDER_INFO_TAG) : new NBTTagCompound();
        NBTTagCompound old = info.getCompoundTag(ITEM_DISPLAY_TAG);
        NBTTagCompound display = new NBTTagCompound();
        if (!displayed.isEmpty()) {
            display.setTag(ITEM_TAG, displayed.writeToNBT(new NBTTagCompound()));
            display.setInteger(ROTATION_TAG, rotation);
        }
        if (display.equals(old) || (display.isEmpty() && !info.hasKey(ITEM_DISPLAY_TAG))) {
            return false;
        }
        if (display.isEmpty()) {
            info.removeTag(ITEM_DISPLAY_TAG);
        } else {
            info.setTag(ITEM_DISPLAY_TAG, display);
        }
        backpack.setTagInfo(RENDER_INFO_TAG, info);
        return true;
    }

    /** The item shown on the front pouch, or EMPTY. */
    public static ItemStack getDisplayItem(ItemStack backpack) {
        NBTTagCompound tag = backpack.getTagCompound();
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        NBTTagCompound display = tag.getCompoundTag(RENDER_INFO_TAG).getCompoundTag(ITEM_DISPLAY_TAG);
        return display.hasKey(ITEM_TAG) ? new ItemStack(display.getCompoundTag(ITEM_TAG)) : ItemStack.EMPTY;
    }

    public static int getDisplayRotation(ItemStack backpack) {
        NBTTagCompound tag = backpack.getTagCompound();
        return tag == null ? 0 : tag.getCompoundTag(RENDER_INFO_TAG).getCompoundTag(ITEM_DISPLAY_TAG).getInteger(ROTATION_TAG);
    }

    public boolean hasLeftTank() {
        return leftTank;
    }

    public boolean hasRightTank() {
        return rightTank;
    }

    public boolean hasBattery() {
        return battery;
    }

    /** The left tank's fluid, or null if it's empty or the fluid is gone. */
    @javax.annotation.Nullable
    public net.minecraftforge.fluids.Fluid getLeftFluid() {
        return leftFluid.isEmpty() ? null : net.minecraftforge.fluids.FluidRegistry.getFluid(leftFluid);
    }

    public float getLeftFill() {
        return leftFill;
    }

    /** The right tank's fluid, or null if it's empty or the fluid is gone. */
    @javax.annotation.Nullable
    public net.minecraftforge.fluids.Fluid getRightFluid() {
        return rightFluid.isEmpty() ? null : net.minecraftforge.fluids.FluidRegistry.getFluid(rightFluid);
    }

    public float getRightFill() {
        return rightFill;
    }

    /** 0 to 1 in quarters. */
    public float getBatteryCharge() {
        return batteryCharge;
    }

    /** 0-7, used to pick one of the eight baked model variants. */
    public int getVariantIndex() {
        return (leftTank ? 1 : 0) | (rightTank ? 2 : 0) | (battery ? 4 : 0);
    }
}
