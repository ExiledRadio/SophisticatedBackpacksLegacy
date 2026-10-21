package com.exiledradio.sbackpackslegacy.client;


import com.exiledradio.sbackpackslegacy.CommonProxy;
import com.exiledradio.sbackpackslegacy.client.gui.GuiBackpack;
import com.exiledradio.sbackpackslegacy.client.render.BackpackLayer;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import com.exiledradio.sbackpackslegacy.client.gui.upgrades.UpgradeTabs;
import com.exiledradio.sbackpackslegacy.upgrades.everlasting.EverlastingBackpackItemEntity;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/** Client-side setup: models, renderers, keybinds, GUIs. Never loaded on a dedicated server. */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EverlastingBackpackItemEntity.class,
                manager -> new RenderEntityItem(manager, Minecraft.getMinecraft().getRenderItem()));
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.bindTileEntitySpecialRenderer(com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity.class,
                new com.exiledradio.sbackpackslegacy.client.render.BackpackDisplayItemRenderer());
        ClientRegistry.registerKeyBinding(ClientEvents.OPEN_BACKPACK);
        ClientRegistry.registerKeyBinding(ClientEvents.TOOL_SWAP);
        UpgradeTabs.register();
    }

    /** Post-init, so renderers other mods registered in init get the layer too. */
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        for (RenderPlayer playerRenderer : renderManager.getSkinMap().values()) {
            playerRenderer.addLayer(new BackpackLayer(playerRenderer));
        }
        // Zombies, skeletons and other biped mobs can wear a backpack in their chest slot.
        for (Render<?> entityRenderer : renderManager.entityRenderMap.values()) {
            if (entityRenderer instanceof RenderLivingBase && ((RenderLivingBase<?>) entityRenderer).getMainModel() instanceof ModelBiped) {
                RenderLivingBase<?> livingRenderer = (RenderLivingBase<?>) entityRenderer;
                livingRenderer.addLayer(new BackpackLayer(livingRenderer));
            }
        }
    }


    @Override
    public void handleOtherPlayerBackpack(com.exiledradio.sbackpackslegacy.network.OtherPlayerBackpackMessage message) {
        // Scheduled like the contents message, so it lands before FML's open-GUI packet.
        Minecraft.getMinecraft().addScheduledTask(() -> com.exiledradio.sbackpackslegacy.common.gui.BackpackContext.AnotherPlayer
                .setClientStack(message.getEntityId(), message.getBackpack()));
    }

    @Override
    public void handleBackpackContents(com.exiledradio.sbackpackslegacy.network.BackpackContentsMessage message) {
        // Runs on the network thread, ahead of the open-GUI packet that FML schedules next;
        // scheduling keeps the order and moves the work onto the client thread.
        Minecraft.getMinecraft().addScheduledTask(() -> com.exiledradio.sbackpackslegacy.backpack.BackpackStorage.get()
                .getOrCreateContents(message.getUuid()).applyUpgradeInventoryFromServer(message.getUpgradeInventory()));
    }




    @Override
    public void handleBackpackTooltipContents(com.exiledradio.sbackpackslegacy.network.BackpackTooltipContentsMessage message) {
        Minecraft.getMinecraft().addScheduledTask(() -> BackpackTooltipRenderer.setSummary(message.getUuid(), message.getSummary()));
    }

    @Override
    public Object createBackpackSettingsGui(com.exiledradio.sbackpackslegacy.common.gui.BackpackSettingsContainer container) {
        return new com.exiledradio.scorelegacy.client.gui.settings.SettingsScreenBase(container);
    }

    @Override
    public Object createBackpackGui(BackpackContainer container) {
        return new GuiBackpack(container);
    }
}
