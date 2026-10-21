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
 * Placed backpacks: draws the fluid in the tanks and the item display setting's item on the
 * front pouch. The backpack itself, tank glass included, is a baked block model; this only adds
 * what changes, so it does nothing for a backpack with neither.
 */
public class BackpackDisplayItemRenderer extends TileEntitySpecialRenderer<BackpackTileEntity> {
    @Override
    public void render(BackpackTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack backpack = te.getBackpack();
        if (backpack.isEmpty()) {
            return;
        }
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing facing = state.getBlock() instanceof BackpackBlock ? state.getValue(BackpackBlock.FACING) : EnumFacing.NORTH;
        renderFluids(BackpackRenderInfo.of(backpack), x, y, z, facing);
        ItemStack displayed = BackpackRenderInfo.getDisplayItem(backpack);
        if (displayed.isEmpty()) {
            return;
        }
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

    /**
     * The original's fluid boxes (BackpackDynamicModel.addFluid): 2.5 pixels wide, standing on the
     * tank floor at y 1.5 and up to 6 pixels high, in the north-facing model, turned like the
     * blockstate turns the model.
     */
    private static void renderFluids(BackpackRenderInfo renderInfo, double x, double y, double z, EnumFacing facing) {
        boolean left = renderInfo.hasLeftTank() && renderInfo.getLeftFluid() != null && renderInfo.getLeftFill() > 0;
        boolean right = renderInfo.hasRightTank() && renderInfo.getRightFluid() != null && renderInfo.getRightFill() > 0;
        if (!left && !right) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.rotate(-getModelYRotation(facing), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5, 0, -0.5);
        if (left) {
            renderFluid(renderInfo.getLeftFluid(), renderInfo.getLeftFill(), 12.85F);
        }
        if (right) {
            renderFluid(renderInfo.getRightFluid(), renderInfo.getRightFill(), 0.6F);
        }
        GlStateManager.popMatrix();
    }

    private static void renderFluid(net.minecraftforge.fluids.Fluid fluid, float fill, float xMin) {
        float px = 1 / 16F;
        TankRenderHelper.renderFluidBox(fluid, xMin * px, 1.5F * px, 6.75F * px, (xMin + 2.5F) * px, (1.5F + fill * 6) * px, 9.25F * px);
    }

    /** The blockstate's y rotation for each facing (backpack.json). */
    private static float getModelYRotation(EnumFacing facing) {
        switch (facing) {
            case EAST:
                return 90;
            case SOUTH:
                return 180;
            case WEST:
                return 270;
            default:
                return 0;
        }
    }
}
