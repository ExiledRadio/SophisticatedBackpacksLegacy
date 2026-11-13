package com.exiledradio.sbackpackslegacy.compat.retro;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.BackpackStorage;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import com.exiledradio.scorelegacy.util.SortBy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Reads Retro Sophisticated Backpacks' stack data (written by its BackpackWrapper and upgrade
 * wrappers) into ours. Retro kept everything on the stack; we keep a backpack's contents in
 * world storage under a UUID, so a backpack's data waits on the stack (under
 * {@link #RETRO_BACKPACK_TAG}) until the server first needs its contents.
 */
public final class RetroBackpackImporter {
    public static final String RETRO_BACKPACK_TAG = "retroSophisticatedBackpack";
    private static final String[] RETRO_SORT_TO_OURS = {"name", "name", "count", "tags"};
    private static final int CRAFTING_GRID_SLOTS = 9;

    private RetroBackpackImporter() {}

    // --- backpacks --------------------------------------------------------------------------

    public static void stashBackpackData(ItemStack backpack, NBTTagCompound retroData) {
        NBTTagCompound tag = backpack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            backpack.setTagCompound(tag);
        }
        // Already ours (contents UUID) or already waiting: leave it.
        if (!tag.hasUniqueId("contentsUuid") && !tag.hasKey(RETRO_BACKPACK_TAG)) {
            tag.setTag(RETRO_BACKPACK_TAG, retroData.copy());
        }
    }

    public static boolean hasRetroData(ItemStack backpack) {
        NBTTagCompound tag = backpack.getTagCompound();
        return tag != null && tag.hasKey(RETRO_BACKPACK_TAG, Constants.NBT.TAG_COMPOUND);
    }

    /**
     * Server side, from {@link BackpackWrapper#getOrCreateContentsUuid()}: moves the waiting Retro
     * data into our storage and returns the contents UUID.
     *
     * <p>The UUID is derived from the Retro data itself. Copies of one Retro stack (made before
     * either was used) therefore land on the same contents, as linked views, and never import
     * the same items twice.
     */
    public static UUID importBackpack(BackpackWrapper wrapper) {
        ItemStack backpack = wrapper.getBackpack();
        //noinspection ConstantConditions - checked by hasRetroData
        NBTTagCompound retro = backpack.getTagCompound().getCompoundTag(RETRO_BACKPACK_TAG);
        backpack.getTagCompound().removeTag(RETRO_BACKPACK_TAG);
        UUID uuid = UUID.nameUUIDFromBytes(("retro_sophisticated_backpacks:" + canonical(retro)).getBytes(StandardCharsets.UTF_8));
        boolean alreadyImported = BackpackStorage.get().hasContents(uuid);

        // Slot counts and colors live on the stack, so every copy gets them.
        List<ItemStack> items = readSlots(retro.getCompoundTag("BackpackInventory"));
        List<ItemStack> upgrades = readSlots(retro.getCompoundTag("UpgradeSlots"));
        int inventorySlots = Math.max(wrapper.getNumberOfInventorySlots(), Math.max(retro.getInteger("BackpackInventorySize"), items.size()));
        int upgradeSlots = Math.max(wrapper.getNumberOfUpgradeSlots(), Math.max(retro.getInteger("UpgradeSlotsSize"), upgrades.size()));
        wrapper.setSlotNumbers(inventorySlots, upgradeSlots);
        if (retro.hasKey("MainColor") && retro.hasKey("AccentColor")) {
            // Retro stores ARGB, ours RGB; Retro's defaults are ours with an alpha byte.
            wrapper.setColors(retro.getInteger("MainColor") & 0xFFFFFF, retro.getInteger("AccentColor") & 0xFFFFFF);
        }
        wrapper.setContentsUuid(uuid);
        if (alreadyImported) {
            return uuid;
        }

        // Upgrades first: the stack upgrades set how much each inventory slot takes.
        UpgradeHandler upgradeHandler = wrapper.getUpgradeHandler();
        for (int slot = 0; slot < upgrades.size() && slot < upgradeHandler.getSlots(); slot++) {
            if (!upgrades.get(slot).isEmpty()) {
                upgradeHandler.setStackInSlot(slot, upgrades.get(slot));
            }
        }
        InventoryHandler inventory = wrapper.getInventoryHandler();
        for (int slot = 0; slot < items.size() && slot < inventory.getSlots(); slot++) {
            if (!items.get(slot).isEmpty()) {
                inventory.setStackInSlot(slot, items.get(slot));
            }
        }
        spreadOversizedStacks(inventory);
        importSettings(wrapper, retro);
        SophisticatedBackpacksLegacy.LOGGER.info("Converted a Retro Sophisticated Backpacks backpack ({} stacks, {} upgrades) to contents {}",
                items.stream().filter(s -> !s.isEmpty()).count(), upgrades.stream().filter(s -> !s.isEmpty()).count(), uuid);
        return uuid;
    }

    /**
     * Retro's largest stack upgrade holds twice what ours does. Stacks over our limit move their
     * excess into empty slots; whatever doesn't fit stays where it is, over the limit, rather
     * than being lost (it can still be taken out, just not added to).
     */
    private static void spreadOversizedStacks(InventoryHandler inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            int limit = stack.isEmpty() ? 0 : inventory.getBaseStackLimit(stack);
            if (stack.isEmpty() || limit <= 0 || stack.getCount() <= limit) {
                continue;
            }
            int excess = stack.getCount() - limit;
            for (int target = 0; target < inventory.getSlots() && excess > 0; target++) {
                if (target != slot && inventory.getStackInSlot(target).isEmpty()) {
                    ItemStack moved = stack.copy();
                    moved.setCount(Math.min(excess, limit));
                    inventory.setStackInSlot(target, moved);
                    excess -= moved.getCount();
                }
            }
            ItemStack kept = stack.copy();
            kept.setCount(limit + excess);
            inventory.setStackInSlot(slot, kept);
        }
    }

    private static void importSettings(BackpackWrapper wrapper, NBTTagCompound retro) {
        SettingsHandler settings = wrapper.getSettingsHandler();
        MemorySettingsCategory memory = settings.getTypeCategory(MemorySettingsCategory.class);
        if (memory != null) {
            List<ItemStack> memorized = readSlots(retro.getCompoundTag("MemoryItems"));
            for (int slot = 0; slot < memorized.size(); slot++) {
                if (!memorized.get(slot).isEmpty()) {
                    memory.setFilter(slot, memorized.get(slot));
                }
            }
        }
        NoSortSettingsCategory noSort = settings.getTypeCategory(NoSortSettingsCategory.class);
        if (noSort != null) {
            byte[] locked = retro.getByteArray("LockedSlots");
            for (int slot = 0; slot < locked.length; slot++) {
                if (locked[slot] != 0) {
                    noSort.selectSlot(slot);
                }
            }
        }
        int sortType = retro.getByte("SortType");
        if (sortType >= 0 && sortType < RETRO_SORT_TO_OURS.length) {
            wrapper.setSortBy(SortBy.fromName(RETRO_SORT_TO_OURS[sortType]));
        }
    }

    /** Retro's slot list: {Items: [{Slot: byte, ..., Count: int}]}, counts past 127 as ints. */
    private static List<ItemStack> readSlots(NBTTagCompound slotsNbt) {
        List<ItemStack> stacks = new ArrayList<>();
        NBTTagList list = slotsNbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemNbt = list.getCompoundTagAt(i);
            int slot = itemNbt.getByte("Slot") & 255;
            ItemStack stack = new ItemStack(itemNbt);
            if (stack.isEmpty() && itemNbt.getInteger("Count") <= 0) {
                continue;
            }
            stack.setCount(itemNbt.getInteger("Count"));
            if (stack.isEmpty()) {
                SophisticatedBackpacksLegacy.LOGGER.warn("Dropped an unknown item from a Retro Sophisticated Backpacks backpack: {}", itemNbt);
                continue;
            }
            while (stacks.size() <= slot) {
                stacks.add(ItemStack.EMPTY);
            }
            stacks.set(slot, stack);
        }
        return stacks;
    }

    /** NBT as text with compound keys sorted, so equal data always gives the same string. */
    private static String canonical(NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            StringBuilder builder = new StringBuilder("{");
            for (String key : new TreeSet<>(compound.getKeySet())) {
                builder.append(key).append(':').append(canonical(compound.getTag(key))).append(',');
            }
            return builder.append('}').toString();
        }
        if (nbt instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) nbt;
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < list.tagCount(); i++) {
                builder.append(canonical(list.get(i))).append(',');
            }
            return builder.append(']').toString();
        }
        return nbt.toString();
    }

    // --- upgrades ---------------------------------------------------------------------------

    /**
     * Rewrites a Retro upgrade's settings (its capability data) into the keys our upgrades keep
     * in the stack tag. Settings our upgrade doesn't have are simply not read.
     */
    static void convertUpgradeSettings(ItemStack upgrade, NBTTagCompound retro) {
        // Runs once: the capability data isn't written back, so the next save drops it.
        NBTTagCompound tag = upgrade.getTagCompound() == null ? new NBTTagCompound() : upgrade.getTagCompound().copy();
        if (retro.hasKey("Enabled")) {
            tag.setBoolean("enabled", retro.getBoolean("Enabled"));
        }
        if (retro.hasKey("FilterItems", Constants.NBT.TAG_COMPOUND)) {
            tag.setTag("filters", retro.getCompoundTag("FilterItems").copy());
        }
        if (retro.hasKey("FilterType")) {
            // WHITELIST, BLACKLIST
            tag.setBoolean("isAllowList", retro.getByte("FilterType") == 0);
        }
        if (retro.hasKey("MatchType")) {
            // ITEM, MOD, ORE_DICT
            String[] primaryMatch = {"item", "mod", "tags"};
            int matchType = retro.getByte("MatchType");
            tag.setString("primaryMatch", matchType >= 0 && matchType < primaryMatch.length ? primaryMatch[matchType] : "item");
        }
        if (retro.hasKey("IgnoreDurability")) {
            tag.setBoolean("matchDurability", !retro.getBoolean("IgnoreDurability"));
        }
        if (retro.hasKey("IgnoreNbt")) {
            tag.setBoolean("matchNbt", !retro.getBoolean("IgnoreNbt"));
        }
        if (retro.hasKey("OreDict", Constants.NBT.TAG_LIST)) {
            NBTTagList oreNames = retro.getTagList("OreDict", Constants.NBT.TAG_STRING);
            NBTTagList tags = new NBTTagList();
            for (int i = 0; i < oreNames.tagCount(); i++) {
                tags.appendTag(new NBTTagString(oreNames.getStringTagAt(i)));
            }
            tag.setTag("tags", tags);
        }
        if (retro.hasKey("FilterWay")) {
            // IN_OUT, IN, OUT
            String[] direction = {"both", "input", "output"};
            int way = retro.getByte("FilterWay");
            tag.setString("direction", way >= 0 && way < direction.length ? direction[way] : "both");
        }
        if (retro.hasKey("HungerFeedingStrategy")) {
            // FULL, HALF, ALWAYS
            String[] hungerLevel = {"full", "half", "any"};
            int strategy = retro.getByte("HungerFeedingStrategy");
            tag.setString("feedAtHungerLevel", strategy >= 0 && strategy < hungerLevel.length ? hungerLevel[strategy] : "half");
        }
        if (retro.hasKey("HurtFeedingStrategy")) {
            // ALWAYS, IGNORE
            tag.setBoolean("feedImmediatelyWhenHurt", retro.getByte("HurtFeedingStrategy") == 0);
        }
        if (retro.hasKey("CraftingDest")) {
            // BACKPACK, INVENTORY
            tag.setBoolean("shiftClickIntoBackpack", retro.getByte("CraftingDest") == 0);
        }
        if (retro.hasKey("Matrix", Constants.NBT.TAG_COMPOUND)) {
            tag.setTag("craftingInventory", craftingGrid(retro.getCompoundTag("Matrix")));
        }
        // Keys only Retro's base class writes (the open-tab state) don't make it a tagged stack.
        if (!tag.isEmpty()) {
            upgrade.setTagCompound(tag);
        }
    }

    /** Retro's grid has a tenth slot for the result, which ours works out instead of storing. */
    private static NBTTagCompound craftingGrid(NBTTagCompound matrix) {
        NBTTagList items = new NBTTagList();
        NBTTagList retroItems = matrix.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < retroItems.tagCount(); i++) {
            NBTTagCompound item = retroItems.getCompoundTagAt(i);
            if (item.getInteger("Slot") < CRAFTING_GRID_SLOTS) {
                items.appendTag(item.copy());
            }
        }
        NBTTagCompound grid = new NBTTagCompound();
        grid.setTag("Items", items);
        grid.setInteger("Size", CRAFTING_GRID_SLOTS);
        return grid;
    }
}
