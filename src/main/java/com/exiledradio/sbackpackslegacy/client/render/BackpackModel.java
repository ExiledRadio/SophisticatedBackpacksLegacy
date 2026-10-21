/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.render;

import com.exiledradio.sbackpackslegacy.backpack.BackpackTier;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.EnumMap;
import java.util.Map;

/**
 * The backpack as worn on the back. Box-for-box the original's model; every tier shares the
 * cloth, border and fabric parts and only the clips differ (their own rows in the texture).
 */
public class BackpackModel extends ModelBase {
    private static final float SCALE = 0.0625F;
    // Each tier's clips sit three texture rows below the previous tier's.
    private static final int FIRST_CLIPS_ROW = 29;
    private static final int FIRST_BATTERY_CLIPS_ROW = 30;
    private static final int ROWS_PER_TIER = 3;

    private final Map<BackpackTier, ModelRenderer> clipsBody = new EnumMap<>(BackpackTier.class);
    private final Map<BackpackTier, ModelRenderer> clipsLeftPouches = new EnumMap<>(BackpackTier.class);
    private final Map<BackpackTier, ModelRenderer> clipsRightPouches = new EnumMap<>(BackpackTier.class);
    private final Map<BackpackTier, ModelRenderer> clipsFrontPouch = new EnumMap<>(BackpackTier.class);
    private final Map<BackpackTier, ModelRenderer> clipsBattery = new EnumMap<>(BackpackTier.class);

    private final ModelRenderer cloth;
    private final ModelRenderer rightPouchesBorder;
    private final ModelRenderer leftPouchesBorder;
    private final ModelRenderer frontPouchBorder;
    private final ModelRenderer frontPouch;
    private final ModelRenderer rightPouches;
    private final ModelRenderer leftPouches;
    private final ModelRenderer border;
    private final ModelRenderer fabricFront;
    private final ModelRenderer fabricRight;
    private final ModelRenderer fabricLeft;
    private final ModelRenderer fabric;
    private final ModelRenderer battery;
    private final ModelRenderer batteryBorder;
    private final ModelRenderer leftTank;
    private final ModelRenderer leftTankBorder;
    private final ModelRenderer rightTank;
    private final ModelRenderer rightTankBorder;

    public BackpackModel() {
        textureWidth = 64;
        textureHeight = 64;

        cloth = part(0.0F, 24.0F, 0.0F);
        box(cloth, 0, 0, -3.5F, -13.25F, -3.25F, 7, 4, 6, 0.0F, false);
        box(cloth, 0, 10, -5.0F, -13.0F, -3.0F, 10, 13, 6, 0.0F, false);

        rightPouchesBorder = part(0.0F, 24.0F, 0.0F);
        box(rightPouchesBorder, 44, 0, 5.0F, -2.0F, -2.5F, 2, 1, 5, 0.0F, true);

        leftPouchesBorder = part(0.0F, 24.0F, 0.0F);
        box(leftPouchesBorder, 44, 0, -7.0F, -2.0F, -2.5F, 2, 1, 5, 0.0F, false);

        frontPouchBorder = part(0.0F, 24.0F, 0.0F);
        box(frontPouchBorder, 44, 0, -4.0F, -2.0F, -5.0F, 8, 1, 2, 0.0F, false);

        frontPouch = part(0.0F, 24.0F, 0.0F);
        box(frontPouch, 25, 0, -4.0F, -1.0F, -5.0F, 8, 1, 2, 0.0F, false);
        box(frontPouch, 13, 2, -4.0F, -4.0F, -5.0F, 8, 2, 2, 0.0F, false);
        box(frontPouch, 13, 0, -4.0F, -6.0F, -5.0F, 8, 1, 2, 0.0F, false);

        rightPouches = part(0.0F, 24.0F, 0.0F);
        box(rightPouches, 32, 5, 5.0F, -1.0F, -2.5F, 2, 1, 5, 0.0F, false);
        box(rightPouches, 32, 13, 5.0F, -4.0F, -2.5F, 2, 2, 5, 0.0F, false);
        box(rightPouches, 32, 11, 5.0F, -6.0F, -2.5F, 2, 1, 5, 0.0F, false);
        box(rightPouches, 32, 22, 5.0F, -9.0F, -2.5F, 1, 2, 5, 0.0F, false);
        box(rightPouches, 32, 20, 5.0F, -11.0F, -2.5F, 1, 1, 5, 0.0F, false);

        leftPouches = part(0.0F, 24.0F, 0.0F);
        box(leftPouches, 32, 5, -7.0F, -1.0F, -2.5F, 2, 1, 5, 0.0F, true);
        box(leftPouches, 32, 13, -7.0F, -4.0F, -2.5F, 2, 2, 5, 0.0F, true);
        box(leftPouches, 32, 11, -7.0F, -6.0F, -2.5F, 2, 1, 5, 0.0F, true);
        box(leftPouches, 32, 22, -6.0F, -9.0F, -2.5F, 1, 2, 5, 0.0F, true);
        box(leftPouches, 32, 20, -6.0F, -11.0F, -2.5F, 1, 1, 5, 0.0F, true);

        border = part(0.0F, 24.0F, 0.0F);
        box(border, 44, 7, -3.5F, -9.25F, -3.25F, 7, 1, 1, 0.0F, false);
        box(border, 50, 20, 3.5F, -13.25F, -3.25F, 1, 5, 6, 0.0F, false);
        box(border, 50, 9, -4.5F, -13.25F, -3.25F, 1, 5, 6, 0.0F, false);

        fabricFront = part(-3.25F, 16.0F, -6.0F);
        box(fabricFront, 0, 55, -0.75F, 3.0F, 1.0F, 8, 1, 2, 0.0F, true);

        fabricRight = part(-3.25F, 16.0F, -6.0F);
        box(fabricRight, 32, 49, 8.25F, -2.0F, 3.5F, 1, 1, 5, 0.0F, true);
        box(fabricRight, 8, 45, 8.25F, 3.0F, 3.5F, 2, 1, 5, 0.0F, true);

        fabricLeft = part(-3.25F, 16.0F, -6.0F);
        box(fabricLeft, 32, 49, -2.75F, -2.0F, 3.5F, 1, 1, 5, 0.0F, false);
        box(fabricLeft, 8, 45, -3.75F, 3.0F, 3.5F, 2, 1, 5, 0.0F, false);

        fabric = part(-3.25F, 16.0F, -6.0F);
        box(fabric, 54, 0, 1.25F, -4.75F, 5.75F, 1, 1, 1, 0.0F, false);
        box(fabric, 58, 0, 4.25F, -4.75F, 5.75F, 1, 1, 1, 0.0F, false);
        box(fabric, 44, 0, 1.25F, -5.75F, 5.75F, 4, 1, 1, 0.0F, true);
        box(fabric, 16, 44, 0.0F, -5.5F, 2.5F, 1, 4, 7, 0.0F, false);
        box(fabric, 0, 44, 5.5F, -5.5F, 2.5F, 1, 4, 7, 0.0F, false);

        batteryBorder = part(0.0F, 24.0F, 0.0F);
        box(batteryBorder, 28, 38, -4.25F, -5.25F, -6.25F, 1, 1, 4, 0.0F, false);
        box(batteryBorder, 28, 43, -3.5F, -5.25F, -6.25F, 7, 1, 1, 0.0F, false);
        box(batteryBorder, 33, 38, -4.25F, -1.25F, -6.25F, 1, 1, 4, 0.0F, false);
        box(batteryBorder, 33, 38, 3.25F, -5.25F, -6.25F, 1, 1, 4, 0.0F, false);
        box(batteryBorder, 27, 45, -3.5F, -1.25F, -6.25F, 7, 1, 1, 0.0F, false);
        box(batteryBorder, 39, 37, 3.25F, -1.25F, -6.25F, 1, 1, 4, 0.0F, false);

        battery = part(0.0F, 24.0F, 0.0F);
        box(battery, 28, 29, -4.0F, -6.0F, -6.0F, 8, 6, 3, 0.0F, false);
        box(battery, 28, 53, -2.0F, -6.25F, -4.5F, 1, 1, 1, 0.0F, false);
        box(battery, 28, 53, -0.75F, -6.25F, -4.5F, 1, 1, 1, 0.0F, false);
        box(battery, 28, 53, -2.0F, -8.0F, -3.25F, 1, 1, 1, 0.0F, false);
        box(battery, 28, 53, -0.75F, -8.0F, -3.25F, 1, 1, 1, 0.0F, false);
        box(battery, 0, 58, -2.0F, -7.4F, -4.5F, 1, 2, 1, -0.2F, false);
        box(battery, 6, 58, -0.75F, -7.4F, -4.5F, 1, 2, 1, -0.2F, false);
        box(battery, 0, 61, -2.0F, -8.0F, -4.5F, 1, 1, 2, -0.2F, false);
        box(battery, 6, 61, -0.75F, -8.0F, -4.5F, 1, 1, 2, -0.2F, false);

        leftTankBorder = part(0.0F, 24.0F, 0.0F);
        box(leftTankBorder, 50, 43, -8.0F, -9.5F, -2.0F, 3, 1, 4, 0.0F, false);

        leftTank = part(0.0F, 24.0F, 0.0F);
        box(leftTank, 54, 27, -5.5F, -7.5F, -2.0F, 1, 6, 4, 0.0F, false);
        box(leftTank, 50, 37, -8.0F, -1.5F, -2.0F, 3, 1, 4, 0.0F, false);
        box(leftTank, 50, 42, -8.0F, -8.5F, -2.0F, 3, 1, 4, 0.0F, false);
        box(leftTank, 50, 37, -8.0F, -10.5F, -2.0F, 3, 1, 4, 0.0F, false);
        box(leftTank, 52, 48, -7.5F, -11.5F, -1.5F, 3, 1, 3, 0.0F, false);

        rightTankBorder = part(0.0F, 24.0F, 0.0F);
        box(rightTankBorder, 50, 43, 5.0F, -9.5F, -2.0F, 3, 1, 4, 0.0F, true);

        rightTank = part(0.0F, 24.0F, 0.0F);
        box(rightTank, 54, 27, 4.5F, -7.5F, -2.0F, 1, 6, 4, 0.0F, true);
        box(rightTank, 50, 37, 5.0F, -1.5F, -2.0F, 3, 1, 4, 0.0F, true);
        box(rightTank, 50, 42, 5.0F, -8.5F, -2.0F, 3, 1, 4, 0.0F, true);
        box(rightTank, 50, 37, 5.0F, -10.5F, -2.0F, 3, 1, 4, 0.0F, true);
        box(rightTank, 52, 48, 4.5F, -11.5F, -1.5F, 3, 1, 3, 0.0F, true);

        for (BackpackTier tier : BackpackTier.values()) {
            int row = FIRST_CLIPS_ROW + tier.ordinal() * ROWS_PER_TIER;
            clipsBody.put(tier, bodyClips(row));
            clipsLeftPouches.put(tier, leftPouchesClips(row));
            clipsRightPouches.put(tier, rightPouchesClips(row));
            clipsFrontPouch.put(tier, frontPouchClips(row));
            clipsBattery.put(tier, batteryClips(FIRST_BATTERY_CLIPS_ROW + tier.ordinal() * ROWS_PER_TIER));
        }
    }

    public void render(int clothColor, int borderColor, BackpackTier tier, boolean showLeftTank, boolean showRightTank, boolean showBattery) {
        float borderRed = (borderColor >> 16 & 255) / 255.0F;
        float borderGreen = (borderColor >> 8 & 255) / 255.0F;
        float borderBlue = (borderColor & 255) / 255.0F;
        float clothRed = (clothColor >> 16 & 255) / 255.0F;
        float clothGreen = (clothColor >> 8 & 255) / 255.0F;
        float clothBlue = (clothColor & 255) / 255.0F;

        if (showLeftTank) {
            renderPlain(leftTank);
            renderTinted(leftTankBorder, borderRed, borderGreen, borderBlue);
        } else {
            renderPlain(fabricLeft);
            renderPlain(clipsLeftPouches.get(tier));
            renderTinted(leftPouches, clothRed, clothGreen, clothBlue);
            renderTinted(leftPouchesBorder, borderRed, borderGreen, borderBlue);
        }

        if (showRightTank) {
            renderPlain(rightTank);
            renderTinted(rightTankBorder, borderRed, borderGreen, borderBlue);
        } else {
            renderPlain(fabricRight);
            renderPlain(clipsRightPouches.get(tier));
            renderTinted(rightPouches, clothRed, clothGreen, clothBlue);
            renderTinted(rightPouchesBorder, borderRed, borderGreen, borderBlue);
        }

        if (showBattery) {
            renderPlain(battery);
            renderTinted(batteryBorder, borderRed, borderGreen, borderBlue);
            renderPlain(clipsBattery.get(tier));
        } else {
            renderPlain(fabricFront);
            renderPlain(clipsFrontPouch.get(tier));
            renderTinted(frontPouch, clothRed, clothGreen, clothBlue);
            renderTinted(frontPouchBorder, borderRed, borderGreen, borderBlue);
        }

        renderPlain(fabric);
        renderPlain(clipsBody.get(tier));
        renderTinted(cloth, clothRed, clothGreen, clothBlue);
        renderTinted(border, borderRed, borderGreen, borderBlue);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderPlain(ModelRenderer renderer) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        renderer.render(SCALE);
    }

    private static void renderTinted(ModelRenderer renderer, float red, float green, float blue) {
        GlStateManager.color(red, green, blue, 1.0F);
        renderer.render(SCALE);
    }

    private ModelRenderer part(float x, float y, float z) {
        ModelRenderer renderer = new ModelRenderer(this);
        renderer.setRotationPoint(x, y, z);
        return renderer;
    }

    /** 1.16's texOffs(u, v).addBox(..., inflate, mirror) in 1.12 terms. */
    private static void box(ModelRenderer renderer, int u, int v, float x, float y, float z, int width, int height, int depth, float inflate, boolean mirror) {
        renderer.mirror = mirror;
        renderer.setTextureOffset(u, v).addBox(x, y, z, width, height, depth, inflate);
        renderer.mirror = false;
    }

    private ModelRenderer bodyClips(int row) {
        ModelRenderer temp = part(0.0F, 24.0F, 0.0F);
        box(temp, 22, row, -3.25F, -9.5F, -3.5F, 1, 2, 1, 0.0F, false);
        box(temp, 25, row, 2.25F, -9.5F, -3.5F, 1, 2, 1, 0.0F, false);
        return temp;
    }

    private ModelRenderer leftPouchesClips(int row) {
        ModelRenderer temp = part(0.0F, 24.0F, 0.0F);
        box(temp, 18, row, -6.25F, -10.0F, -0.5F, 1, 2, 1, 0.0F, false);
        box(temp, 6, row, -7.25F, -5.0F, -0.5F, 1, 2, 1, 0.0F, false);
        return temp;
    }

    private ModelRenderer rightPouchesClips(int row) {
        ModelRenderer temp = part(0.0F, 24.0F, 0.0F);
        box(temp, 6, row, 6.25F, -5.0F, -0.5F, 1, 2, 1, 0.0F, true);
        box(temp, 18, row, 5.25F, -10.0F, -0.5F, 1, 2, 1, 0.0F, true);
        return temp;
    }

    private ModelRenderer frontPouchClips(int row) {
        ModelRenderer temp = part(0.0F, 24.0F, 0.0F);
        box(temp, 0, row, 2.0F, -5.0F, -5.25F, 1, 2, 1, 0.0F, false);
        box(temp, 3, row, -3.0F, -5.0F, -5.25F, 1, 2, 1, 0.0F, false);
        return temp;
    }

    private ModelRenderer batteryClips(int row) {
        ModelRenderer temp = part(0.0F, 24.0F, 0.0F);
        box(temp, 24, row, 1.0F, -5.25F, -6.15F, 1, 1, 1, 0.2F, false);
        box(temp, 21, row, 1.0F, -1.25F, -6.15F, 1, 1, 1, 0.2F, false);
        return temp;
    }
}
