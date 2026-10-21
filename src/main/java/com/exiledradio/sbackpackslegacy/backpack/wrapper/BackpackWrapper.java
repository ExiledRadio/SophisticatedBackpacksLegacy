/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack.wrapper;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsCategory;
import com.exiledradio.scorelegacy.util.SortBy;
import com.exiledradio.sbackpackslegacy.backpack.BackpackContentsPolicy;

import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.inventory.StorageFluidHandler;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;

import com.exiledradio.scorelegacy.inventory.StorageContents;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackStorage;
import net.minecraft.item.ItemStack;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeWrapper;
import net.minecraftforge.energy.IEnergyStorage;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Everything about one backpack stack: its contents (via {@link BackpackStorage}), its slot
 * counts and its colours. One wrapper per ItemStack instance, created by the item's capability
 * provider. Contents are shared between all wrappers with the same UUID.
 */
public class BackpackWrapper implements IStorageWrapper {
    public static final int DEFAULT_CLOTH_COLOR = 13394234;
    public static final int DEFAULT_BORDER_COLOR = 6434330;
    private static final String CLOTH_COLOR_TAG = "clothColor";
    private static final String BORDER_COLOR_TAG = "borderColor";
    private static final String CONTENTS_UUID_TAG = "contentsUuid";
    private static final String INVENTORY_SLOTS_TAG = "inventorySlots";
    private static final String UPGRADE_SLOTS_TAG = "upgradeSlots";
    private static final String OPEN_TAB_ID_TAG = "openTabId";

    private final ItemStack backpack;
    private Runnable backpackSaveHandler = () -> {};
    @Nullable
    private StorageContents contents = null;

    public BackpackWrapper(ItemStack backpack) {
        this.backpack = backpack;
    }

    public ItemStack getBackpack() {
        return backpack;
    }

    /** Called whenever the stack's own NBT changes; a placed backpack uses it to re-sync. */
    public void setBackpackSaveHandler(Runnable saveHandler) {
        backpackSaveHandler = saveHandler;
    }

    private StorageContents getContents() {
        if (contents == null) {
            UUID uuid = getOrCreateContentsUuid();
            // The client only ever sees stacks the server already gave a UUID. Should one slip
            // through, it gets throwaway contents instead of inventing a UUID of its own.
            contents = uuid != null ? BackpackStorage.get().getOrCreateContents(uuid) : StorageContents.temporary(BackpackContentsPolicy.INSTANCE);
        }
        return contents;
    }

    public InventoryHandler getInventoryHandler() {
        // Upgrades and settings first: the inventory's insert hooks, slot limit and memory slots come from them.
        getUpgradeHandler();
        getSettingsHandler();
        return getContents().getInventoryHandler(getNumberOfInventorySlots());
    }

    @Override
    public SettingsHandler getSettingsHandler() {
        return getContents().getSettingsHandler(this);
    }

    @Override
    public void addSettingsCategories(SettingsHandler handler) {
        IStorageWrapper.super.addSettingsCategories(handler);
        // Reached from getInventoryHandler before the settings exist, so the inventory is taken from
        // the contents directly rather than through getInventoryHandler.
        InventoryHandler inventory = getContents().getInventoryHandler(getNumberOfInventorySlots());
        handler.addCategory(ItemDisplaySettingsCategory.NAME, (nbt, save) -> {
            ItemDisplaySettingsCategory category = new ItemDisplaySettingsCategory(() -> inventory, this::setItemDisplay, nbt, save);
            inventory.addListener(category::itemChanged);
            return category;
        });
    }

    @Override
    public String getPlayerSettingsTagName() {
        return "sophisticatedBackpackSettings";
    }

    @Override
    public SortBy getSortBy() {
        return getContents().getSortBy();
    }

    @Override
    public void setSortBy(SortBy sortBy) {
        getContents().setSortBy(sortBy);
    }

    /** The item display setting changed: what the front pouch shows lives in the stack's render info. */
    private void setItemDisplay(ItemStack displayed, int rotation) {
        if (BackpackRenderInfo.setItemDisplay(backpack, displayed, rotation)) {
            backpackSaveHandler.run();
        }
    }

    public UpgradeHandler getUpgradeHandler() {
        return getContents().getUpgradeHandler(getNumberOfUpgradeSlots(), this);
    }

    /** The inventory upgrades work on: this backpack, plus nested backpacks with Inception. */
    public IItemHandlerSimpleInserter getInventoryForUpgradeProcessing() {
        getInventoryHandler();
        return getContents().getInventoryForUpgradeProcessing(this);
    }

    /** What hoppers, pipes and other mods see: the upgrade inventory behind any filter upgrades. */
    public IItemHandlerSimpleInserter getInventoryForInputOutput() {
        getInventoryHandler();
        return getContents().getInventoryForInputOutput(this);
    }

    public void refreshInventoryForInputOutput() {
        getContents().refreshInventoryForInputOutput();
    }

    public void refreshInventoryForUpgradeProcessing() {
        getContents().refreshInventoryForUpgradeProcessing();
    }

    public int getOpenTabId() {
        NBTTagCompound tag = backpack.getTagCompound();
        return tag != null && tag.hasKey(OPEN_TAB_ID_TAG) ? tag.getInteger(OPEN_TAB_ID_TAG) : -1;
    }

    /** Which upgrade tab was open, so reopening the GUI (or the backpack) shows it again. */
    public void setOpenTabId(int openTabId) {
        if (openTabId < 0) {
            NBTTagCompound tag = backpack.getTagCompound();
            if (tag != null) {
                tag.removeTag(OPEN_TAB_ID_TAG);
            }
        } else {
            getOrCreateTag().setInteger(OPEN_TAB_ID_TAG, openTabId);
        }
        backpackSaveHandler.run();
    }

    public int getNumberOfInventorySlots() {
        NBTTagCompound tag = backpack.getTagCompound();
        if (tag != null && tag.hasKey(INVENTORY_SLOTS_TAG)) {
            return tag.getInteger(INVENTORY_SLOTS_TAG);
        }
        int itemInventorySlots = ((BackpackItem) backpack.getItem()).getTier().getInventorySlots();
        // Pinned into the NBT (server only) so later config changes don't resize existing backpacks.
        if (isServerSide()) {
            setInt(INVENTORY_SLOTS_TAG, itemInventorySlots);
        }
        return itemInventorySlots;
    }

    public int getNumberOfUpgradeSlots() {
        NBTTagCompound tag = backpack.getTagCompound();
        if (tag != null && tag.hasKey(UPGRADE_SLOTS_TAG)) {
            return tag.getInteger(UPGRADE_SLOTS_TAG);
        }
        int itemUpgradeSlots = ((BackpackItem) backpack.getItem()).getTier().getUpgradeSlots();
        if (isServerSide()) {
            setInt(UPGRADE_SLOTS_TAG, itemUpgradeSlots);
        }
        return itemUpgradeSlots;
    }




    public void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots) {
        setInt(INVENTORY_SLOTS_TAG, numberOfInventorySlots);
        setInt(UPGRADE_SLOTS_TAG, numberOfUpgradeSlots);
    }

    @Nullable
    public UUID getContentsUuid() {
        NBTTagCompound tag = backpack.getTagCompound();
        return tag != null && tag.hasUniqueId(CONTENTS_UUID_TAG) ? tag.getUniqueId(CONTENTS_UUID_TAG) : null;
    }

    /** Points this stack at existing contents (the admin give command). */
    public void setContentsUuid(UUID uuid) {
        getOrCreateTag().setUniqueId(CONTENTS_UUID_TAG, uuid);
        contents = null;
        backpackSaveHandler.run();
    }

    /** Assigns a UUID on the server the first time the contents are needed. Null on the client. */
    @Nullable
    public UUID getOrCreateContentsUuid() {
        UUID uuid = getContentsUuid();
        if (uuid != null || !isServerSide()) {
            return uuid;
        }
        uuid = UUID.randomUUID();
        getOrCreateTag().setUniqueId(CONTENTS_UUID_TAG, uuid);
        contents = null;
        backpackSaveHandler.run();
        return uuid;
    }

    private static boolean isServerSide() {
        return FMLCommonHandler.instance().getSide().isServer() || FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    @Override
    public int getMainColor() {
        return getClothColor();
    }

    @Override
    public int getAccentColor() {
        return getBorderColor();
    }

    @Override
    public ItemStack getWrappedStorageStack() {
        return backpack;
    }

    public int getClothColor() {
        NBTTagCompound tag = backpack.getTagCompound();
        return tag != null && tag.hasKey(CLOTH_COLOR_TAG) ? tag.getInteger(CLOTH_COLOR_TAG) : DEFAULT_CLOTH_COLOR;
    }

    public int getBorderColor() {
        NBTTagCompound tag = backpack.getTagCompound();
        return tag != null && tag.hasKey(BORDER_COLOR_TAG) ? tag.getInteger(BORDER_COLOR_TAG) : DEFAULT_BORDER_COLOR;
    }

    public void setColors(int clothColor, int borderColor) {
        NBTTagCompound tag = getOrCreateTag();
        tag.setInteger(CLOTH_COLOR_TAG, clothColor);
        tag.setInteger(BORDER_COLOR_TAG, borderColor);
        backpackSaveHandler.run();
    }

    /**
     * A copy with a new UUID and its own copy of the contents. Only used where the game itself
     * hands out a free copy (creative mode), never in survival paths.
     */
    public ItemStack cloneBackpack() {
        ItemStack copy = backpack.copy();
        NBTTagCompound tag = copy.getTagCompound();
        if (tag != null) {
            tag.removeTag(CONTENTS_UUID_TAG + "Most");
            tag.removeTag(CONTENTS_UUID_TAG + "Least");
        }
        BackpackWrapper copyWrapper = new BackpackWrapper(copy);
        if (getContentsUuid() != null) {
            InventoryHandler from = getInventoryHandler();
            InventoryHandler to = copyWrapper.getInventoryHandler();
            for (int slot = 0; slot < from.getSlots() && slot < to.getSlots(); slot++) {
                to.setStackInSlot(slot, from.getStackInSlot(slot).copy());
            }
            UpgradeHandler upgradesFrom = getUpgradeHandler();
            UpgradeHandler upgradesTo = copyWrapper.getUpgradeHandler();
            for (int slot = 0; slot < upgradesFrom.getSlots() && slot < upgradesTo.getSlots(); slot++) {
                upgradesTo.setStackInSlot(slot, upgradesFrom.getStackInSlot(slot).copy());
            }
        }
        return copy;
    }

    private void setInt(String key, int value) {
        getOrCreateTag().setInteger(key, value);
        backpackSaveHandler.run();
    }

    private NBTTagCompound getOrCreateTag() {
        NBTTagCompound tag = backpack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            backpack.setTagCompound(tag);
        }
        return tag;
    }
}
