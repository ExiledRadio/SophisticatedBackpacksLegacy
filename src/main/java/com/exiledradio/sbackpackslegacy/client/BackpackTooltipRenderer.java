/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.network.PacketHandler;
import com.exiledradio.sbackpackslegacy.network.RequestBackpackContentsMessage;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hold Shift over a backpack to see what is in it: a panel under the tooltip with its upgrades
 * and its contents merged by kind, most first. The summary comes from the server and is asked
 * for again every second while the tooltip is shown.
 */
@Mod.EventBusSubscriber(modid = SophisticatedBackpacksLegacy.MODID, value = Side.CLIENT)
public final class BackpackTooltipRenderer {
    private static final String TOOLTIP = "item." + SophisticatedBackpacksLegacy.MODID + ".backpack.tooltip.";
    private static final int REFRESH_INTERVAL = 20;

    private static final Map<UUID, NBTTagCompound> SUMMARIES = new HashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST = new HashMap<>();

    private BackpackTooltipRenderer() {}

    public static void setSummary(UUID uuid, NBTTagCompound summary) {
        SUMMARIES.put(uuid, summary);
    }

    public static void clear() {
        SUMMARIES.clear();
        LAST_REQUEST.clear();
    }

    /** The "Press <Left Shift> to View Contents" hint, for the item's own tooltip. */
    public static String getShiftHint() {
        return TextFormatting.GRAY + I18n.format(TOOLTIP + "press_for_contents", TextFormatting.WHITE + I18n.format(TOOLTIP + "shift") + TextFormatting.GRAY);
    }

    @SubscribeEvent
    public static void onTooltipDrawn(RenderTooltipEvent.PostText event) {
        ItemStack stack = event.getStack();
        Minecraft mc = Minecraft.getMinecraft();
        if (!(stack.getItem() instanceof BackpackItem) || !GuiScreen.isShiftKeyDown() || mc.player == null) {
            return;
        }
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
        UUID uuid = wrapper == null ? null : wrapper.getContentsUuid();
        NBTTagCompound summary = null;
        if (uuid != null) {
            long now = mc.world.getTotalWorldTime();
            Long last = LAST_REQUEST.get(uuid);
            if (last == null || now - last >= REFRESH_INTERVAL || now < last) {
                LAST_REQUEST.put(uuid, now);
                PacketHandler.CHANNEL.sendToServer(new RequestBackpackContentsMessage(uuid));
            }
            summary = SUMMARIES.get(uuid);
        }
        com.exiledradio.scorelegacy.client.gui.ContentsTooltipPanel.draw(mc, event.getFontRenderer(), event.getX(), event.getY(), event.getHeight(), summary, TOOLTIP);
    }

}
