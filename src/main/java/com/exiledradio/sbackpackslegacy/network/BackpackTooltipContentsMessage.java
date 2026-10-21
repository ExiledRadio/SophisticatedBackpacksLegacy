package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.UUID;

/** The answer to RequestBackpackContentsMessage: a summary for the Shift tooltip preview. */
public class BackpackTooltipContentsMessage implements IMessage {
    private UUID uuid;
    private NBTTagCompound summary = new NBTTagCompound();

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public BackpackTooltipContentsMessage() {
    }

    public BackpackTooltipContentsMessage(UUID uuid, NBTTagCompound summary) {
        this.uuid = uuid;
        this.summary = summary;
    }

    public UUID getUuid() {
        return uuid;
    }

    public NBTTagCompound getSummary() {
        return summary;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = new UUID(buf.readLong(), buf.readLong());
        NBTTagCompound read = ByteBufUtils.readTag(buf);
        summary = read == null ? new NBTTagCompound() : read;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        ByteBufUtils.writeTag(buf, summary);
    }

    public static class Handler implements IMessageHandler<BackpackTooltipContentsMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(BackpackTooltipContentsMessage message, MessageContext ctx) {
            SophisticatedBackpacksLegacy.proxy.handleBackpackTooltipContents(message);
            return null;
        }
    }
}
