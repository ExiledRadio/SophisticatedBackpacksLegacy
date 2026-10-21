/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

/** Who last opened a backpack, and enough about it to hand out a copy that opens the same contents. */
public class AccessLogRecord {
    private final ResourceLocation backpackItem;
    private final UUID backpackUuid;
    private final String playerName;
    private final String backpackName;
    private final int clothColor;
    private final int trimColor;
    private final long accessTime;

    public AccessLogRecord(ResourceLocation backpackItem, UUID backpackUuid, String playerName, String backpackName, int clothColor, int trimColor, long accessTime) {
        this.backpackItem = backpackItem;
        this.backpackUuid = backpackUuid;
        this.playerName = playerName;
        this.backpackName = backpackName;
        this.clothColor = clothColor;
        this.trimColor = trimColor;
        this.accessTime = accessTime;
    }

    public ResourceLocation getBackpackItem() {
        return backpackItem;
    }

    public UUID getBackpackUuid() {
        return backpackUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getBackpackName() {
        return backpackName;
    }

    public int getClothColor() {
        return clothColor;
    }

    public int getTrimColor() {
        return trimColor;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setString("backpackItem", backpackItem.toString());
        ret.setUniqueId("backpackUuid", backpackUuid);
        ret.setString("playerName", playerName);
        ret.setString("backpackName", backpackName);
        ret.setInteger("clothColor", clothColor);
        ret.setInteger("trimColor", trimColor);
        ret.setLong("accessTime", accessTime);
        return ret;
    }

    public static AccessLogRecord deserializeNBT(NBTTagCompound nbt) {
        return new AccessLogRecord(new ResourceLocation(nbt.getString("backpackItem")), nbt.getUniqueId("backpackUuid"), nbt.getString("playerName"),
                nbt.getString("backpackName"), nbt.getInteger("clothColor"), nbt.getInteger("trimColor"), nbt.getLong("accessTime"));
    }
}
