/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

/**
 * A backpack's upgrade inventory, sent just before its GUI opens. The client builds the
 * container (and its upgrade tabs) from its own copy of the contents, so that copy has to know
 * which upgrades are installed or the two sides would disagree on the slot list.
 */
public class BackpackContentsMessage implements IMessage {
    private UUID uuid = new UUID(0, 0);
    private NBTTagCompound upgradeInventory = new NBTTagCompound();

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public BackpackContentsMessage() {
    }

    public BackpackContentsMessage(UUID uuid, NBTTagCompound upgradeInventory) {
        this.uuid = uuid;
        this.upgradeInventory = upgradeInventory;
    }

    public UUID getUuid() {
        return uuid;
    }

    public NBTTagCompound getUpgradeInventory() {
        return upgradeInventory;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = new UUID(buf.readLong(), buf.readLong());
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        upgradeInventory = tag == null ? new NBTTagCompound() : tag;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        ByteBufUtils.writeTag(buf, upgradeInventory);
    }

    /** Applied by the client proxy, so this class never names a client-only type. */
    public static class Handler implements IMessageHandler<BackpackContentsMessage, IMessage> {
        @Override
        public IMessage onMessage(BackpackContentsMessage message, MessageContext ctx) {
            SophisticatedBackpacksLegacy.proxy.handleBackpackContents(message);
            return null;
        }
    }
}
