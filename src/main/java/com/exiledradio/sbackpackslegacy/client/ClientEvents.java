package com.exiledradio.sbackpackslegacy.client;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackBlock;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackStorage;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.client.render.BackpackItemModel;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import com.exiledradio.sbackpackslegacy.init.ModContent;
import com.exiledradio.sbackpackslegacy.network.OpenBackpackMessage;
import com.exiledradio.sbackpackslegacy.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import com.exiledradio.scorelegacy.init.ModFluids;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeContainer;
import com.exiledradio.sbackpackslegacy.network.BlockToolSwapMessage;
import com.exiledradio.sbackpackslegacy.network.EntityToolSwapMessage;
import com.exiledradio.sbackpackslegacy.upgrades.toolswapper.ToolSwapperFilterContainer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import com.exiledradio.sbackpackslegacy.backpack.WornBackpack;
import com.exiledradio.sbackpackslegacy.client.gui.GuiBackpack;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = SophisticatedBackpacksLegacy.MODID, value = Side.CLIENT)
public final class ClientEvents {
    // B is taken in RLCraft-style packs (pets, Wearable Backpacks); K is free in the common ones.
    public static final KeyBinding OPEN_BACKPACK = new KeyBinding("keybind.sbackpackslegacy.open_backpack",
            Keyboard.KEY_K, "keybind.sbackpackslegacy.category");
    public static final KeyBinding TOOL_SWAP = new KeyBinding("keybind.sbackpackslegacy.tool_swap",
            KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, "keybind.sbackpackslegacy.category");

    private static final int TINT_CLOTH = 0;
    private static boolean pendingOpenFromInventory;
    @Nullable
    private static GuiScreen openedFromInventory;
    private static final int TINT_BORDER = 1;

    private ClientEvents() {}

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (BackpackItem item : ModContent.getBackpackItems()) {
            //noinspection ConstantConditions - registered items always have a registry name
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        for (net.minecraft.item.Item item : com.exiledradio.sbackpackslegacy.init.ModUpgrades.getUpgradeItems()) {
            //noinspection ConstantConditions - registered items always have a registry name
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    /**
     * Swaps each backpack's item model for one that reuses the block's eight baked variants. The
     * item JSON is still loaded, but only for its display transforms.
     */
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (BackpackItem item : ModContent.getBackpackItems()) {
            //noinspection ConstantConditions - registered items always have a registry name
            ModelResourceLocation itemLocation = new ModelResourceLocation(item.getRegistryName(), "inventory");
            IBakedModel jsonModel = event.getModelRegistry().getObject(itemLocation);
            if (jsonModel == null) {
                continue;
            }
            IBakedModel[] variants = new IBakedModel[8];
            for (int index = 0; index < variants.length; index++) {
                String variant = String.format("battery=%s,facing=north,left_tank=%s,right_tank=%s", (index & 4) != 0, (index & 1) != 0, (index & 2) != 0);
                IBakedModel blockModel = event.getModelRegistry().getObject(new ModelResourceLocation(item.getRegistryName(), variant));
                variants[index] = blockModel != null ? blockModel : jsonModel;
            }
            event.getModelRegistry().putObject(itemLocation,
                    new BackpackItemModel(variants, PerspectiveMapWrapper.getTransforms(jsonModel.getItemCameraTransforms())));
        }
    }

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Pre event) {
        if (event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
            event.getMap().registerSprite(new ResourceLocation(ToolSwapperFilterContainer.EMPTY_WEAPON_SLOT_SPRITE));
            for (String toolType : ToolSwapperFilterContainer.TOOL_TYPES_WITH_BACKGROUND) {
                event.getMap().registerSprite(new ResourceLocation(ToolSwapperFilterContainer.getEmptyToolSlotSprite(toolType)));
            }
        }
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == com.exiledradio.sbackpackslegacy.client.render.BackpackItemModel.TINT_LEFT_FLUID
                    || tintIndex == com.exiledradio.sbackpackslegacy.client.render.BackpackItemModel.TINT_RIGHT_FLUID) {
                com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo renderInfo = com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo.of(stack);
                net.minecraftforge.fluids.Fluid fluid = tintIndex == com.exiledradio.sbackpackslegacy.client.render.BackpackItemModel.TINT_LEFT_FLUID
                        ? renderInfo.getLeftFluid() : renderInfo.getRightFluid();
                return fluid == null ? -1 : fluid.getColor(new net.minecraftforge.fluids.FluidStack(fluid, net.minecraftforge.fluids.Fluid.BUCKET_VOLUME));
            }
            BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
            return getTint(wrapper, tintIndex);
        }, ModContent.getBackpackItems().toArray(new BackpackItem[0]));
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event) {
        Block[] blocks = ModContent.getBackpackItems().stream().map(item -> BackpackBlock.forTier(item.getTier())).toArray(Block[]::new);
        event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return -1;
            }
            BackpackTileEntity te = world.getTileEntity(pos) instanceof BackpackTileEntity ? (BackpackTileEntity) world.getTileEntity(pos) : null;
            return getTint(te == null ? null : te.getBackpackWrapper(), tintIndex);
        }, blocks);
    }

    private static int getTint(BackpackWrapper wrapper, int tintIndex) {
        if (tintIndex == TINT_CLOTH) {
            return wrapper == null ? BackpackWrapper.DEFAULT_CLOTH_COLOR : wrapper.getClothColor();
        }
        if (tintIndex == TINT_BORDER) {
            return wrapper == null ? BackpackWrapper.DEFAULT_BORDER_COLOR : wrapper.getBorderColor();
        }
        return -1;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        while (OPEN_BACKPACK.isPressed()) {
            if (mc.player != null && mc.currentScreen == null) {
                PacketHandler.CHANNEL.sendToServer(new OpenBackpackMessage());
            }
        }
        while (TOOL_SWAP.isPressed()) {
            sendToolSwapMessage(mc);
        }
    }

    /**
     * The open-backpack key in an inventory screen: over a backpack in the player's inventory (or
     * worn slot) it opens that backpack, as the original does. In a backpack opened that way, the
     * key pressed anywhere else goes back to the inventory.
     */
    @SubscribeEvent
    public static void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!Keyboard.getEventKeyState() || !(event.getGui() instanceof GuiContainer)) {
            return;
        }
        int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        if (!OPEN_BACKPACK.isActiveAndMatches(key)) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        GuiContainer gui = (GuiContainer) event.getGui();
        boolean inventoryScreen = gui instanceof GuiInventory
                || gui instanceof GuiContainerCreative && !hasSearchBar((GuiContainerCreative) gui);
        boolean backpackScreen = gui instanceof GuiBackpack;
        if (mc.player == null || !inventoryScreen && !backpackScreen || !mc.player.inventory.getItemStack().isEmpty()) {
            return;
        }
        Slot hovered = gui.getSlotUnderMouse();
        ItemStack stack = hovered == null ? ItemStack.EMPTY : hovered.getStack();
        BackpackContext target = stack.getItem() instanceof BackpackItem ? locate(mc.player, stack) : null;
        if (target != null) {
            event.setCanceled(true);
            if (backpackScreen && ((BackpackContainer) gui.inventorySlots).getOpenedStack() == stack) {
                return;
            }
            pendingOpenFromInventory = inventoryScreen || backpackScreen && openedFromInventory == gui;
            PacketHandler.CHANNEL.sendToServer(new OpenBackpackMessage(BackpackContext.InventoryKind.values()[target.getX()], target.getY()));
        } else if (backpackScreen && openedFromInventory == gui) {
            event.setCanceled(true);
            mc.player.closeScreen();
            // Switches itself to the creative inventory in creative mode.
            mc.displayGuiScreen(new GuiInventory(mc.player));
        }
    }

    /** A creative tab with a search bar takes every key, K included. */
    private static boolean hasSearchBar(GuiContainerCreative gui) {
        int index = gui.getSelectedTabIndex();
        return index < 0 || index >= CreativeTabs.CREATIVE_TAB_ARRAY.length || CreativeTabs.CREATIVE_TAB_ARRAY[index].hasSearchBar();
    }

    /** Where the hovered stack sits in the player's inventory or worn slots, matched by identity. */
    @Nullable
    private static BackpackContext locate(EntityPlayer player, ItemStack stack) {
        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            if (player.inventory.mainInventory.get(slot) == stack) {
                return new BackpackContext.Item(BackpackContext.InventoryKind.MAIN, slot);
            }
        }
        if (player.inventory.armorInventory.get(EntityEquipmentSlot.CHEST.getIndex()) == stack) {
            return new BackpackContext.Item(BackpackContext.InventoryKind.ARMOR, 0);
        }
        if (player.inventory.offHandInventory.get(0) == stack) {
            return new BackpackContext.Item(BackpackContext.InventoryKind.OFFHAND, 0);
        }
        WornBackpack worn = WornBackpack.find(player);
        return worn != null && worn.getBackpack() == stack ? worn.getContext() : null;
    }

    /** Remembers whether the backpack screen about to open was asked for from the inventory screen. */
    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        openedFromInventory = pendingOpenFromInventory && event.getGui() instanceof GuiBackpack ? event.getGui() : null;
        pendingOpenFromInventory = false;
    }

    private static void sendToolSwapMessage(Minecraft mc) {
        RayTraceResult target = mc.objectMouseOver;
        if (mc.player == null || mc.currentScreen != null || target == null) {
            return;
        }
        if (mc.player.getHeldItemMainhand().getItem() instanceof BackpackItem) {
            mc.player.sendStatusMessage(new TextComponentTranslation("gui.sbackpackslegacy.status.unable_to_swap_tool_for_backpack"), true);
            return;
        }
        if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
            PacketHandler.CHANNEL.sendToServer(new BlockToolSwapMessage(target.getBlockPos()));
        } else if (target.typeOfHit == RayTraceResult.Type.ENTITY && target.entityHit != null) {
            PacketHandler.CHANNEL.sendToServer(new EntityToolSwapMessage(target.entityHit.getEntityId()));
        }
    }

    @SubscribeEvent
    public static void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        BackpackStorage.clearClientCopy();
        BackpackTooltipRenderer.clear();
    }
}
