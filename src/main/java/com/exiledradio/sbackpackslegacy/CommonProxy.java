package com.exiledradio.sbackpackslegacy;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import com.exiledradio.sbackpackslegacy.compat.baubles.BaublesCompat;
import com.exiledradio.sbackpackslegacy.common.gui.GuiHandler;
import com.exiledradio.sbackpackslegacy.network.PacketHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;

/** Setup shared by both sides. Also the proxy on a dedicated server. */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        CapabilityBackpackWrapper.register();
        PacketHandler.init();
        com.exiledradio.scorelegacy.compat.jei.StorageContainerClasses.register(BackpackContainer.class);
        BaublesCompat.init();
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(SophisticatedBackpacksLegacy.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {
    }


    /** Client only: the upgrade inventory of a backpack about to be opened. */
    public void handleBackpackContents(com.exiledradio.sbackpackslegacy.network.BackpackContentsMessage message) {
        //noop on the server
    }

    /** Client only: the backpack another player wears, just before its GUI opens. */
    public void handleOtherPlayerBackpack(com.exiledradio.sbackpackslegacy.network.OtherPlayerBackpackMessage message) {
        //noop on the server
    }




    /** Client only: a backpack contents summary for the Shift tooltip preview. */
    public void handleBackpackTooltipContents(com.exiledradio.sbackpackslegacy.network.BackpackTooltipContentsMessage message) {
        //noop on the server
    }

    /** The client settings screen of a backpack. Never called on a dedicated server. */
    @Nullable
    public Object createBackpackSettingsGui(com.exiledradio.sbackpackslegacy.common.gui.BackpackSettingsContainer container) {
        return null;
    }

    /** The client screen for a backpack container. Never called on a dedicated server. */
    @Nullable
    public Object createBackpackGui(BackpackContainer container) {
        return null;
    }
}
