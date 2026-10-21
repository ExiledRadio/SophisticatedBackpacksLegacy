/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.render;

import com.exiledradio.sbackpackslegacy.backpack.BackpackBlock;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

/**
 * Placed backpacks: draws the item display setting's item on the front pouch. The backpack
 * itself is a baked block model; this only adds the item, so it does nothing without one.
 */
public class BackpackDisplayItemRenderer extends TileEntitySpecialRenderer<BackpackTileEntity> {
    @Override
    public void render(BackpackTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack backpack = te.getBackpack();
        ItemStack displayed = backpack.isEmpty() ? ItemStack.EMPTY : BackpackRenderInfo.getDisplayItem(backpack);
        if (displayed.isEmpty()) {
            return;
        }
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing facing = state.getBlock() instanceof BackpackBlock ? state.getValue(BackpackBlock.FACING) : EnumFacing.NORTH;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        // Turns +z (where the pouch is drawn) towards the block's facing, which is its front.
        GlStateManager.rotate(-facing.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.6F, 0.25F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F + BackpackRenderInfo.getDisplayRotation(backpack), 0.0F, 0.0F, 1.0F);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(displayed, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
