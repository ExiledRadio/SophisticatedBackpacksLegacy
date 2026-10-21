package com.exiledradio.sbackpackslegacy.client.render;

import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

/**
 * The backpack item. Reuses the eight baked block variants (pouch or tank on each side, pouch or
 * battery at the front) facing north, so item and placed block can never drift apart, and picks
 * the variant from the stack's render info. Display transforms come from the item JSON.
 */
public class BackpackItemModel implements IBakedModel {
    /** Tint indexes of the tank fluids, read by the item colour handler. */
    public static final int TINT_LEFT_FLUID = 2;
    public static final int TINT_RIGHT_FLUID = 3;
    private static final net.minecraft.client.renderer.block.model.FaceBakery FACE_BAKERY = new net.minecraft.client.renderer.block.model.FaceBakery();

    private final IBakedModel[] variants;
    private final int variantIndex;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final ItemOverrideList overrides;
    private final String fluidKey;
    private final List<BakedQuad> fluidQuads;
    /** One model per look, shared between stacks. */
    private final java.util.Map<String, BackpackItemModel> looks;

    public BackpackItemModel(IBakedModel[] variants, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
        this(variants, 0, transforms, "", java.util.Collections.emptyList(), new java.util.concurrent.ConcurrentHashMap<>());
    }

    private BackpackItemModel(IBakedModel[] variants, int variantIndex, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms,
                              String fluidKey, List<BakedQuad> fluidQuads, java.util.Map<String, BackpackItemModel> looks) {
        this.variants = variants;
        this.variantIndex = variantIndex;
        this.transforms = transforms;
        this.fluidKey = fluidKey;
        this.fluidQuads = fluidQuads;
        this.looks = looks;
        this.overrides = new ItemOverrideList(ImmutableList.of()) {
            @Nonnull
            @Override
            public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                BackpackRenderInfo renderInfo = BackpackRenderInfo.of(stack);
                int index = renderInfo.getVariantIndex();
                String key = getFluidKey(renderInfo);
                if (index == BackpackItemModel.this.variantIndex && key.equals(BackpackItemModel.this.fluidKey)) {
                    return BackpackItemModel.this;
                }
                return looks.computeIfAbsent(index + "|" + key,
                        k -> new BackpackItemModel(variants, index, transforms, key, makeFluidQuads(renderInfo), looks));
            }
        };
    }

    private static String getFluidKey(BackpackRenderInfo renderInfo) {
        net.minecraftforge.fluids.Fluid left = renderInfo.hasLeftTank() ? renderInfo.getLeftFluid() : null;
        net.minecraftforge.fluids.Fluid right = renderInfo.hasRightTank() ? renderInfo.getRightFluid() : null;
        return (left == null ? "" : left.getName() + renderInfo.getLeftFill()) + "|" + (right == null ? "" : right.getName() + renderInfo.getRightFill());
    }

    /** The original's fluid boxes (BackpackDynamicModel.addFluid) in the north-facing model. */
    private static List<BakedQuad> makeFluidQuads(BackpackRenderInfo renderInfo) {
        List<BakedQuad> quads = new java.util.ArrayList<>();
        if (renderInfo.hasLeftTank() && renderInfo.getLeftFluid() != null && renderInfo.getLeftFill() > 0) {
            addFluidBox(quads, renderInfo.getLeftFluid(), renderInfo.getLeftFill(), 12.85F, TINT_LEFT_FLUID);
        }
        if (renderInfo.hasRightTank() && renderInfo.getRightFluid() != null && renderInfo.getRightFill() > 0) {
            addFluidBox(quads, renderInfo.getRightFluid(), renderInfo.getRightFill(), 0.6F, TINT_RIGHT_FLUID);
        }
        return quads;
    }

    private static void addFluidBox(List<BakedQuad> quads, net.minecraftforge.fluids.Fluid fluid, float fill, float xMin, int tintIndex) {
        net.minecraft.util.ResourceLocation still = fluid.getStill(new net.minecraftforge.fluids.FluidStack(fluid, net.minecraftforge.fluids.Fluid.BUCKET_VOLUME));
        if (still == null) {
            return;
        }
        TextureAtlasSprite sprite = net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still.toString());
        org.lwjgl.util.vector.Vector3f from = new org.lwjgl.util.vector.Vector3f(xMin, 1.5F, 6.75F);
        org.lwjgl.util.vector.Vector3f to = new org.lwjgl.util.vector.Vector3f(xMin + 2.5F, 1.5F + fill * 6, 9.25F);
        for (EnumFacing face : EnumFacing.values()) {
            float width = face.getAxis() == EnumFacing.Axis.X ? 2.5F : 2.5F;
            float height = face.getAxis() == EnumFacing.Axis.Y ? 2.5F : fill * 6;
            net.minecraft.client.renderer.block.model.BlockFaceUV uv = new net.minecraft.client.renderer.block.model.BlockFaceUV(new float[] {0, 0, width, height}, 0);
            net.minecraft.client.renderer.block.model.BlockPartFace partFace = new net.minecraft.client.renderer.block.model.BlockPartFace(null, tintIndex, "", uv);
            quads.add(FACE_BAKERY.makeBakedQuad(from, to, partFace, sprite, face, net.minecraftforge.common.model.TRSRTransformation.identity(), null, false, true));
        }
    }

    private IBakedModel variant() {
        return variants[variantIndex];
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null || fluidQuads.isEmpty()) {
            return variant().getQuads(state, side, rand);
        }
        List<BakedQuad> quads = new java.util.ArrayList<>(variant().getQuads(state, null, rand));
        quads.addAll(fluidQuads);
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return variant().isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return variant().getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    @Nonnull
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
        return PerspectiveMapWrapper.handlePerspective(this, transforms, cameraTransformType);
    }
}
