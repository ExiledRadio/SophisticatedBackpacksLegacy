/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.client.render;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

import net.minecraft.client.Minecraft;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.WornBackpack;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/** Draws a worn backpack (Baubles body slot or chest slot) on the wearer's back. */
public class BackpackLayer implements LayerRenderer<EntityLivingBase> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "textures/entity/backpack.png");
    private static final float CHILD_Y_OFFSET = 0.3F;
    private static final float CHILD_Z_OFFSET = 0.1F;
    private static final float CHILD_SCALE = 0.55F;
    private static final BackpackModel MODEL = new BackpackModel();

    private final RenderLivingBase<?> renderer;

    public BackpackLayer(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack backpack;
        boolean wearsArmor;
        if (entity instanceof EntityPlayer) {
            WornBackpack worn = WornBackpack.find((EntityPlayer) entity);
            if (worn == null) {
                return;
            }
            backpack = worn.getBackpack();
            // Worn from Baubles over a chestplate: sit a little further out.
            wearsArmor = !worn.isInChestSlot() && !entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty();
        } else {
            backpack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (!(backpack.getItem() instanceof BackpackItem)) {
                return;
            }
            wearsArmor = false;
        }
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(backpack);
        if (wrapper == null) {
            return;
        }

        GlStateManager.pushMatrix();
        ModelRenderer animatedBody = getAnimatedBody();
        if (animatedBody != null) {
            // Mo' Bends animates the body itself (its sneak pose is nothing like vanilla's). The body part
            // bends at the waist and its postRender ends with a post-offset back up to the shoulders, so it
            // leaves us where vanilla's body starts. (Its elytra layer adds -12 itself only because it goes
            // through the transform data, which has no post-offset.)
            animatedBody.postRender(scale);
        } else if (entity.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
            GlStateManager.rotate(90F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
        }
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        float zOffset = wearsArmor ? -0.35F : -0.3F;
        float yOffset = -0.75F;
        if (entity.isChild()) {
            zOffset += CHILD_Z_OFFSET;
            yOffset = CHILD_Y_OFFSET;
        }
        GlStateManager.translate(0.0F, yOffset, zOffset);
        if (entity.isChild()) {
            GlStateManager.scale(CHILD_SCALE, CHILD_SCALE, CHILD_SCALE);
        }
        if (entity instanceof EntityEnderman) {
            GlStateManager.translate(0.0F, -0.8F, 0.0F);
        }

        renderer.bindTexture(TEXTURE);
        GlStateManager.disableCull();
        BackpackRenderInfo renderInfo = BackpackRenderInfo.of(backpack);
        MODEL.render(wrapper.getClothColor(), wrapper.getBorderColor(), ((BackpackItem) backpack.getItem()).getTier(),
                renderInfo.hasLeftTank(), renderInfo.hasRightTank(), renderInfo.hasBattery());
        GlStateManager.enableCull();
        renderDisplayItem(backpack);
        GlStateManager.popMatrix();
    }

    /** The body part when an animation mod (Mo' Bends) has replaced the model's own, else null. */
    @Nullable
    private ModelRenderer getAnimatedBody() {
        ModelBase model = renderer.getMainModel();
        if (model instanceof ModelBiped) {
            ModelRenderer body = ((ModelBiped) model).bipedBody;
            if (body != null && body.getClass().getName().startsWith("goblinbob.mobends.")) {
                return body;
            }
        }
        return null;
    }

    /** The item display setting: the chosen item on the front pouch, as the original places it. */
    private static void renderDisplayItem(ItemStack backpack) {
        ItemStack displayed = BackpackRenderInfo.getDisplayItem(backpack);
        if (displayed.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.9F, -0.25F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180.0F + BackpackRenderInfo.getDisplayRotation(backpack), 0.0F, 0.0F, 1.0F);
        Minecraft.getMinecraft().getRenderItem().renderItem(displayed, ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
