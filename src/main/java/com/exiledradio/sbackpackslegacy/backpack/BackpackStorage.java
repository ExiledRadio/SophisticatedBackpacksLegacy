/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.scorelegacy.inventory.StorageContents;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Backpack contents, keyed by the UUID stored on each backpack stack. Keeping contents out of the
 * item NBT keeps the stack tiny (no oversized packets) and means every copy of a stack with the
 * same UUID shares one {@link StorageContents}: copying a stack never copies items.
 */
public class BackpackStorage extends WorldSavedData {
    private static final String SAVED_DATA_NAME = SophisticatedBackpacksLegacy.MODID;

    private static BackpackStorage clientStorageCopy = new BackpackStorage(SAVED_DATA_NAME);

    private final Map<UUID, NBTTagCompound> backpackContentsNbt = new HashMap<>();
    // Live handlers, one per UUID, built on demand from the NBT above and writing back into it.
    private final Map<UUID, StorageContents> liveContents = new HashMap<>();
    private final Map<UUID, AccessLogRecord> accessLogs = new HashMap<>();

    // Called reflectively by MapStorage, so it has to stay public with this signature.
    public BackpackStorage(String name) {
        super(name);
    }

    public static BackpackStorage get() {
        if (isServerThread()) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
                World overworld = server.getWorld(0);
                //noinspection ConstantConditions - the overworld is always loaded while the server runs
                MapStorage storage = overworld.getMapStorage();
                BackpackStorage data = (BackpackStorage) storage.getOrLoadData(BackpackStorage.class, SAVED_DATA_NAME);
                if (data == null) {
                    data = new BackpackStorage(SAVED_DATA_NAME);
                    storage.setData(SAVED_DATA_NAME, data);
                }
                return data;
            }
        }
        return clientStorageCopy;
    }

    private static boolean isServerThread() {
        // A dedicated server has no client storage at all, whatever thread asks. In singleplayer
        // both sides share the JVM, so the thread decides which copy is meant.
        return FMLCommonHandler.instance().getSide().isServer() || FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    /** Dropped when the client leaves a world, so one server's contents never show on another. */
    public static void clearClientCopy() {
        clientStorageCopy = new BackpackStorage(SAVED_DATA_NAME);
    }

    public StorageContents getOrCreateContents(UUID backpackUuid) {
        return liveContents.computeIfAbsent(backpackUuid, uuid -> new StorageContents(uuid, getOrCreateContentsNbt(uuid), this::markDirty, BackpackContentsPolicy.INSTANCE));
    }

    private NBTTagCompound getOrCreateContentsNbt(UUID backpackUuid) {
        return backpackContentsNbt.computeIfAbsent(backpackUuid, uuid -> {
            markDirty();
            return new NBTTagCompound();
        });
    }

    public void putAccessLog(AccessLogRecord record) {
        accessLogs.put(record.getBackpackUuid(), record);
        markDirty();
    }

    public Map<UUID, AccessLogRecord> getAccessLogs() {
        return java.util.Collections.unmodifiableMap(accessLogs);
    }

    /**
     * Drops the contents of backpacks no player ever opened (loot, mob gear), optionally only
     * those that are empty. Returns how many were removed.
     */
    public int removeNonPlayerBackpackContents(boolean onlyWithEmptyInventory) {
        int removed = 0;
        java.util.Iterator<Map.Entry<UUID, NBTTagCompound>> it = backpackContentsNbt.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, NBTTagCompound> entry = it.next();
            if (accessLogs.containsKey(entry.getKey())) {
                continue;
            }
            if (onlyWithEmptyInventory && !entry.getValue().getCompoundTag(com.exiledradio.scorelegacy.inventory.InventoryHandler.INVENTORY_TAG)
                    .getTagList("Items", Constants.NBT.TAG_COMPOUND).isEmpty()) {
                continue;
            }
            it.remove();
            liveContents.remove(entry.getKey());
            removed++;
        }
        if (removed > 0) {
            markDirty();
        }
        return removed;
    }

    public boolean hasContents(UUID backpackUuid) {
        return backpackContentsNbt.containsKey(backpackUuid);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        backpackContentsNbt.clear();
        liveContents.clear();
        accessLogs.clear();
        NBTTagList logs = nbt.getTagList("accessLogRecords", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < logs.tagCount(); i++) {
            AccessLogRecord record = AccessLogRecord.deserializeNBT(logs.getCompoundTagAt(i));
            accessLogs.put(record.getBackpackUuid(), record);
        }
        NBTTagList list = nbt.getTagList("backpackContents", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound uuidContentsPair = list.getCompoundTagAt(i);
            UUID uuid = uuidContentsPair.getUniqueId("uuid");
            if (uuid != null) {
                backpackContentsNbt.put(uuid, uuidContentsPair.getCompoundTag("contents"));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, NBTTagCompound> entry : backpackContentsNbt.entrySet()) {
            NBTTagCompound uuidContentsPair = new NBTTagCompound();
            uuidContentsPair.setUniqueId("uuid", entry.getKey());
            uuidContentsPair.setTag("contents", entry.getValue());
            list.appendTag(uuidContentsPair);
        }
        compound.setTag("backpackContents", list);
        NBTTagList logs = new NBTTagList();
        for (AccessLogRecord record : accessLogs.values()) {
            logs.appendTag(record.serializeNBT());
        }
        compound.setTag("accessLogRecords", logs);
        return compound;
    }
}
