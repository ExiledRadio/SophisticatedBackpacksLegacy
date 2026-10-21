/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.everlasting;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * A dropped backpack with the Everlasting upgrade: immune to fire, lava, explosions, cactus and
 * the void, never despawns, floats up out of liquids and is moved back into the world from below it.
 */
public class EverlastingBackpackItemEntity extends EntityItem {
    private boolean wasFloatingUp = false;

    @SuppressWarnings("unused") // used by the entity registry when loading from disk or spawning on the client
    public EverlastingBackpackItemEntity(World world) {
        super(world);
        init();
    }

    public EverlastingBackpackItemEntity(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
        init();
    }

    private void init() {
        lifespan = Integer.MAX_VALUE;
        isImmuneToFire = true;
        setEntityInvulnerable(true);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote && rand.nextInt(20) == 0) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                    posX + 0.5F - rand.nextFloat(), posY + rand.nextFloat() * 0.5F, posZ + 0.5F - rand.nextFloat(), 1, 0.0D, 0.1D, 0.0D, 0.0D);
        }
        if (!world.isRemote && posY < 0) {
            rescueFromVoid();
        }
        if (!hasNoGravity()) {
            if (isInWater() || isInLava()) {
                motionY = Math.max(motionY, 0.1D);
                wasFloatingUp = true;
            } else if (wasFloatingUp) {
                setNoGravity(true);
                motionX = 0;
                motionY = 0;
                motionZ = 0;
            }
        }
        super.onUpdate();
    }

    /**
     * Below the world it would fall forever (the original's behaviour), and floating up would
     * leave it stuck under the bedrock. Instead it moves to the lowest open space with ground
     * under it in this column, or hovers at y=1 if the column has no blocks at all (the End).
     */
    private void rescueFromVoid() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int x = MathHelper.floor(posX);
        int z = MathHelper.floor(posZ);
        for (int y = 1; y < world.getActualHeight() - 1; y++) {
            if (isSolid(pos.setPos(x, y - 1, z)) && !isSolid(pos.setPos(x, y, z)) && !isSolid(pos.setPos(x, y + 1, z))) {
                setPositionAndUpdate(posX, y, posZ);
                motionX = 0;
                motionY = 0;
                motionZ = 0;
                return;
            }
        }
        setPositionAndUpdate(posX, 1, posZ);
        motionX = 0;
        motionY = 0;
        motionZ = 0;
        setNoGravity(true);
    }

    private boolean isSolid(BlockPos pos) {
        return world.getBlockState(pos).getMaterial().blocksMovement();
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void outOfWorld() {
        // Vanilla removes the entity here; this one is moved back into the world instead (see rescueFromVoid).
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }
}
