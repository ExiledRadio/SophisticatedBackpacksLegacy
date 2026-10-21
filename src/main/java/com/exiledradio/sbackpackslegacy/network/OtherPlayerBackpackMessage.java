package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * The backpack another player wears, sent just before its GUI opens for someone standing behind
 * them. The client builds the container from this copy: its own view of other players'
 * equipment (and Baubles) may not have caught up yet.
 */
public class OtherPlayerBackpackMessage implements IMessage {
    private int entityId;
    private ItemStack backpack = ItemStack.EMPTY;

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public OtherPlayerBackpackMessage() {
    }

    public OtherPlayerBackpackMessage(int entityId, ItemStack backpack) {
        this.entityId = entityId;
        this.backpack = backpack;
    }

    public int getEntityId() {
        return entityId;
    }

    public ItemStack getBackpack() {
        return backpack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        backpack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        ByteBufUtils.writeItemStack(buf, backpack);
    }

    /** Applied by the client proxy, so this class never names a client-only type. */
    public static class Handler implements IMessageHandler<OtherPlayerBackpackMessage, IMessage> {
        @Override
        public IMessage onMessage(OtherPlayerBackpackMessage message, MessageContext ctx) {
            SophisticatedBackpacksLegacy.proxy.handleOtherPlayerBackpack(message);
            return null;
        }
    }
}
