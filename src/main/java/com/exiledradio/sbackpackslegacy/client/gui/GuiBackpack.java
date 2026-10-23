/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.gui;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.NearbyBackpackActions;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import com.exiledradio.sbackpackslegacy.network.BackpackActionMessage;
import com.exiledradio.sbackpackslegacy.network.PacketHandler;
import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

/**
 * The backpack screen: Core's storage screen for a {@link BackpackContainer}, plus two loot
 * buttons left of the sort buttons: "Deposit to nearby chests", and "Merge backpacks", which only
 * shows while another backpack is close enough (see {@link NearbyBackpackActions}).
 */
public class GuiBackpack extends StorageScreenBase {
    private static final ResourceLocation ACTIONS_TEXTURE = new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "textures/gui/backpack_actions.png");
    private static final Dimension ACTIONS_TEXTURE_SIZE = new Dimension(32, 16);
    private static final ButtonDefinition DEPOSIT = new ButtonDefinition(Dimension.SQUARE_12, GuiHelper.SMALL_BUTTON_BACKGROUND, GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND,
            new TextureBlitData(ACTIONS_TEXTURE, ACTIONS_TEXTURE_SIZE, new UV(0, 0), Dimension.SQUARE_12),
            TranslationHelper.tooltip("gui.sbackpackslegacy.buttons.deposit_nearby", "gui.sbackpackslegacy.buttons.deposit_nearby.detail"));
    private static final ButtonDefinition MERGE = new ButtonDefinition(Dimension.SQUARE_12, GuiHelper.SMALL_BUTTON_BACKGROUND, GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND,
            new TextureBlitData(ACTIONS_TEXTURE, ACTIONS_TEXTURE_SIZE, new UV(12, 0), Dimension.SQUARE_12),
            TranslationHelper.tooltip("gui.sbackpackslegacy.buttons.merge_backpacks", "gui.sbackpackslegacy.buttons.merge_backpacks.detail"));
    /** Ticks between looks for a backpack to merge from. */
    private static final int MERGE_CHECK_INTERVAL = 10;

    private final BackpackContainer backpackContainer;
    private Button depositButton;
    private Button mergeButton;
    private boolean canMerge = false;
    private int mergeCheckCooldown = 0;

    public GuiBackpack(BackpackContainer container) {
        super(container);
        backpackContainer = container;
    }

    @Override
    public void initGui() {
        super.initGui();
        depositButton = new Button(new Position(guiLeft + xSize - 48, guiTop + 4), DEPOSIT,
                button -> PacketHandler.CHANNEL.sendToServer(new BackpackActionMessage(BackpackActionMessage.DEPOSIT_TO_NEARBY_CHESTS)));
        mergeButton = new Button(new Position(guiLeft + xSize - 62, guiTop + 4), MERGE,
                button -> PacketHandler.CHANNEL.sendToServer(new BackpackActionMessage(BackpackActionMessage.MERGE_BACKPACKS)));
        mergeCheckCooldown = 0;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (--mergeCheckCooldown <= 0) {
            mergeCheckCooldown = MERGE_CHECK_INTERVAL;
            canMerge = NearbyBackpackActions.hasMergeSource(mc.player, backpackContainer);
        }
    }

    private boolean showDeposit() {
        return ModConfig.nearbyActions.depositToNearbyChestsRange > 0;
    }

    private boolean showMerge() {
        return canMerge;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (showDeposit()) {
            depositButton.render(mouseX, mouseY, partialTicks);
        }
        if (showMerge()) {
            mergeButton.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (showDeposit() && depositButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        if (showMerge() && mergeButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public List<Rectangle> getExtraAreas() {
        List<Rectangle> areas = super.getExtraAreas();
        if (showDeposit()) {
            areas.add(new Rectangle(depositButton.getX(), depositButton.getY(), 12, 12));
        }
        if (showMerge()) {
            areas.add(new Rectangle(mergeButton.getX(), mergeButton.getY(), 12, 12));
        }
        return areas;
    }
}
