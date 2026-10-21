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
    private final IBakedModel[] variants;
    private final int variantIndex;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final ItemOverrideList overrides;

    public BackpackItemModel(IBakedModel[] variants, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
        this(variants, 0, transforms);
    }

    private BackpackItemModel(IBakedModel[] variants, int variantIndex, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
        this.variants = variants;
        this.variantIndex = variantIndex;
        this.transforms = transforms;
        this.overrides = new ItemOverrideList(ImmutableList.of()) {
            @Nonnull
            @Override
            public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                int index = BackpackRenderInfo.of(stack).getVariantIndex();
                return index == BackpackItemModel.this.variantIndex ? BackpackItemModel.this : new BackpackItemModel(variants, index, transforms);
            }
        };
    }

    private IBakedModel variant() {
        return variants[variantIndex];
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return variant().getQuads(state, side, rand);
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
