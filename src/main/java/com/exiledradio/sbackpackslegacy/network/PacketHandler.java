package com.exiledradio.sbackpackslegacy.network;


import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(SophisticatedBackpacksLegacy.MODID);

    private PacketHandler() {}

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(OpenBackpackMessage.Handler.class, OpenBackpackMessage.class, id++, Side.SERVER);

        CHANNEL.registerMessage(BackpackContentsMessage.Handler.class, BackpackContentsMessage.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(BlockToolSwapMessage.Handler.class, BlockToolSwapMessage.class, id++, Side.SERVER);
        CHANNEL.registerMessage(EntityToolSwapMessage.Handler.class, EntityToolSwapMessage.class, id++, Side.SERVER);
        CHANNEL.registerMessage(RequestBackpackContentsMessage.Handler.class, RequestBackpackContentsMessage.class, id++, Side.SERVER);
        CHANNEL.registerMessage(BackpackTooltipContentsMessage.Handler.class, BackpackTooltipContentsMessage.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(OtherPlayerBackpackMessage.Handler.class, OtherPlayerBackpackMessage.class, id++, Side.CLIENT);



    }
}
