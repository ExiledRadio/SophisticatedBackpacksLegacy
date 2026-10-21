package com.exiledradio.sbackpackslegacy.backpack.wrapper;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;

import net.minecraft.item.ItemStack;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeItem;
import net.minecraft.nbt.NBTTagCompound;

/**
 * What the backpack looks like beyond its tier and colours: tanks instead of side pouches and a
 * battery instead of the front pouch. Kept in the stack NBT (it's tiny) so every client renders
 * it without needing the contents. Written from the installed tank and battery upgrades.
 */
public final class BackpackRenderInfo {
    private static final String RENDER_INFO_TAG = "renderInfo";
    private static final String LEFT_TANK_TAG = "leftTank";
    private static final String RIGHT_TANK_TAG = "rightTank";
    private static final String BATTERY_TAG = "battery";
    private static final String ITEM_DISPLAY_TAG = "itemDisplay";
    private static final String ITEM_TAG = "item";
    private static final String ROTATION_TAG = "rotation";

    public static final BackpackRenderInfo DEFAULT = new BackpackRenderInfo(false, false, false);

    private final boolean leftTank;
    private final boolean rightTank;
    private final boolean battery;

    public BackpackRenderInfo(boolean leftTank, boolean rightTank, boolean battery) {
        this.leftTank = leftTank;
        this.rightTank = rightTank;
        this.battery = battery;
    }

    public static BackpackRenderInfo of(ItemStack backpack) {
        NBTTagCompound tag = backpack.getTagCompound();
        if (tag == null || !tag.hasKey(RENDER_INFO_TAG)) {
            return DEFAULT;
        }
        NBTTagCompound info = tag.getCompoundTag(RENDER_INFO_TAG);
        return new BackpackRenderInfo(info.getBoolean(LEFT_TANK_TAG), info.getBoolean(RIGHT_TANK_TAG), info.getBoolean(BATTERY_TAG));
    }

    /** Writes what the installed upgrades look like into the stack; true if that changed anything. */
    public static boolean update(ItemStack backpack, UpgradeHandler upgradeHandler) {
        int tanks = upgradeHandler.getTypeWrappers(TankUpgradeItem.TYPE).size();
        boolean battery = upgradeHandler.hasUpgrade(BatteryUpgradeItem.TYPE);
        BackpackRenderInfo current = of(backpack);
        if (current.leftTank == tanks >= 1 && current.rightTank == tanks >= 2 && current.battery == battery) {
            return false;
        }
        NBTTagCompound tag = backpack.getTagCompound();
        // Keep the item display that lives in the same compound.
        NBTTagCompound info = tag != null && tag.hasKey(RENDER_INFO_TAG) ? tag.getCompoundTag(RENDER_INFO_TAG) : new NBTTagCompound();
        info.setBoolean(LEFT_TANK_TAG, tanks >= 1);
        info.setBoolean(RIGHT_TANK_TAG, tanks >= 2);
        info.setBoolean(BATTERY_TAG, battery);
        backpack.setTagInfo(RENDER_INFO_TAG, info);
        return true;
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

    /** 0-7, used to pick one of the eight baked model variants. */
    public int getVariantIndex() {
        return (leftTank ? 1 : 0) | (rightTank ? 2 : 0) | (battery ? 4 : 0);
    }
}
