/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.render;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * The worn backpack's tank glass, the fluid inside the tanks and the battery's charge bar (the
 * original's TankGlassModel and RenderHelper), and the fluid box placed backpacks draw. All
 * coordinates are model pixels, like the backpack model's own parts.
 */
public final class TankRenderHelper {
    private static final ResourceLocation TANK_GLASS_TEXTURE = new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "textures/entity/tank_glass.png");
    private static final ResourceLocation BACKPACK_TEXTURE = new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "textures/entity/backpack.png");
    private static final float SCALE = 0.0625F;
    private static final GlassModel GLASS = new GlassModel();
    /** The charge bar at one to four pixels long, made once (a model part compiles a display list). */
    private static final ModelRenderer[] CHARGE_BARS = new ModelRenderer[4];

    static {
        ModelBase backpackTexture = new ModelBase() {};
        backpackTexture.textureWidth = 64;
        backpackTexture.textureHeight = 64;
        for (int pixels = 1; pixels <= 4; pixels++) {
            ModelRenderer bar = new ModelRenderer(backpackTexture, 18, 55);
            bar.addBox(-2.0F, -3.0F, -6.01F, pixels, 1, 1, 0.0F);
            CHARGE_BARS[pixels - 1] = bar;
        }
    }

    private TankRenderHelper() {}

    /** Glass and fluid of a worn backpack's tanks, in the backpack model's frame. Leaves the block atlas bound. */
    static void renderWornTanks(BackpackRenderInfo renderInfo) {
        if (!renderInfo.hasLeftTank() && !renderInfo.hasRightTank()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.6F, 0.5F);
        mc.getTextureManager().bindTexture(TANK_GLASS_TEXTURE);
        if (renderInfo.hasLeftTank()) {
            GLASS.leftTankGlass.render(SCALE);
        }
        if (renderInfo.hasRightTank()) {
            GLASS.rightTankGlass.render(SCALE);
        }
        if (renderInfo.hasLeftTank()) {
            renderWornFluid(renderInfo.getLeftFluid(), renderInfo.getLeftFill(), -14.5F);
        }
        if (renderInfo.hasRightTank()) {
            renderWornFluid(renderInfo.getRightFluid(), renderInfo.getRightFill(), 11.0F);
        }
        GlStateManager.popMatrix();
    }

    /** The original's renderFluid: 3.5 x (fill * 10) x 4 pixels standing on the tank's floor at y 37.5. */
    private static void renderWornFluid(@Nullable Fluid fluid, float fill, float xOffset) {
        if (fluid == null || fill <= 0) {
            return;
        }
        float height = fill * 10.0F;
        renderFluidBox(fluid, xOffset * SCALE, (37.5F - height) * SCALE, -2.0F * SCALE, (xOffset + 3.5F) * SCALE, 37.5F * SCALE, 2.0F * SCALE);
    }

    /** The battery's charge along the front pouch, in quarters (the original's renderBatteryCharge). */
    static void renderWornBatteryCharge(BackpackRenderInfo renderInfo) {
        int pixels = (int) (renderInfo.getBatteryCharge() * 4);
        if (!renderInfo.hasBattery() || pixels <= 0) {
            return;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKPACK_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 1.5F, 0.0F);
        CHARGE_BARS[Math.min(4, pixels) - 1].render(SCALE);
        GlStateManager.popMatrix();
    }

    /**
     * A box of the fluid's still texture, tinted with its colour, between two corners in the
     * current GL units. Binds the block atlas.
     */
    static void renderFluidBox(Fluid fluid, float x1, float y1, float z1, float x2, float y2, float z2) {
        Minecraft mc = Minecraft.getMinecraft();
        FluidStack stack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
        ResourceLocation still = fluid.getStill(stack);
        if (still == null) {
            return;
        }
        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(still.toString());
        int color = fluid.getColor(stack);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        // Each face shows the part of the texture its size covers, as a model box would.
        float u1 = sprite.getMinU();
        float v1 = sprite.getMinV();
        float uX = sprite.getInterpolatedU(Math.min(16, Math.abs(x2 - x1) / SCALE));
        float uZ = sprite.getInterpolatedU(Math.min(16, Math.abs(z2 - z1) / SCALE));
        float vY = sprite.getInterpolatedV(Math.min(16, Math.abs(y2 - y1) / SCALE));
        float vZ = sprite.getInterpolatedV(Math.min(16, Math.abs(z2 - z1) / SCALE));
        // -z and +z
        quad(buffer, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, u1, v1, uX, vY, red, green, blue);
        quad(buffer, x1, y1, z2, x1, y2, z2, x2, y2, z2, x2, y1, z2, u1, v1, uX, vY, red, green, blue);
        // -x and +x
        quad(buffer, x1, y1, z1, x1, y2, z1, x1, y2, z2, x1, y1, z2, u1, v1, uZ, vY, red, green, blue);
        quad(buffer, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, u1, v1, uZ, vY, red, green, blue);
        // both y faces
        quad(buffer, x1, y1, z1, x1, y1, z2, x2, y1, z2, x2, y1, z1, u1, v1, uX, vZ, red, green, blue);
        quad(buffer, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, u1, v1, uX, vZ, red, green, blue);
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void quad(BufferBuilder buffer, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz,
                             float dx, float dy, float dz, float u1, float v1, float u2, float v2, float red, float green, float blue) {
        buffer.pos(ax, ay, az).tex(u1, v2).color(red, green, blue, 1.0F).endVertex();
        buffer.pos(bx, by, bz).tex(u2, v2).color(red, green, blue, 1.0F).endVertex();
        buffer.pos(cx, cy, cz).tex(u2, v1).color(red, green, blue, 1.0F).endVertex();
        buffer.pos(dx, dy, dz).tex(u1, v1).color(red, green, blue, 1.0F).endVertex();
    }

    /** The original's TankGlassModel: the glass panes around each worn tank (32x32 texture). */
    private static final class GlassModel extends ModelBase {
        final ModelRenderer leftTankGlass;
        final ModelRenderer rightTankGlass;

        GlassModel() {
            textureWidth = 32;
            textureHeight = 32;
            leftTankGlass = new ModelRenderer(this);
            leftTankGlass.setRotationPoint(0.0F, 24.0F, 0.0F);
            leftTankGlass.setTextureOffset(18, 5).addBox(-15F, 3.5F, -2.5F, 4, 10, 0, 0.0F);
            leftTankGlass.setTextureOffset(0, 0).addBox(-15F, 3.5F, -2.5F, 0, 10, 5, 0.0F);
            leftTankGlass.setTextureOffset(10, 5).addBox(-15F, 3.5F, 2.5F, 4, 10, 0, 0.0F);
            rightTankGlass = new ModelRenderer(this);
            rightTankGlass.setRotationPoint(0.0F, 24.0F, 0.0F);
            rightTankGlass.mirror = true;
            rightTankGlass.setTextureOffset(18, 5).addBox(11F, 3.5F, -2.5F, 4, 10, 0, 0.0F);
            rightTankGlass.setTextureOffset(0, 0).addBox(15F, 3.5F, -2.5F, 0, 10, 5, 0.0F);
            rightTankGlass.setTextureOffset(10, 5).addBox(11F, 3.5F, 2.5F, 4, 10, 0, 0.0F);
        }
    }
}
