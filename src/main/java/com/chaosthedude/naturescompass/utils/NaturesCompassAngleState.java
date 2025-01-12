package com.chaosthedude.naturescompass.utils;

import org.jetbrains.annotations.Nullable;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NeedleAngleState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class NaturesCompassAngleState extends NeedleAngleState {

	private final NeedleAngleState.Angler wobbler;
	private final Random random = Random.create();

	public NaturesCompassAngleState() {
		super(true);
		wobbler = createAngler(0.8F);
	}

	@Override
	protected float getAngle(ItemStack stack, ClientWorld world, int seed, Entity entity) {
		GlobalPos pos = new GlobalPos(world.getRegistryKey(), world.getSpawnPos());
		if (stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
			NaturesCompassItem compassItem = (NaturesCompassItem) stack.getItem();
			if (compassItem.getState(stack) == CompassState.FOUND) {
				pos = new GlobalPos(world.getRegistryKey(), new BlockPos(compassItem.getFoundBiomeX(stack), 0, compassItem.getFoundBiomeZ(stack)));
			}
		}
		long gameTime = world.getTime();
		return !isValidCompassTargetPos(entity, pos) ? getRandomlySpinningRotation(seed, gameTime) : getRotationTowardsCompassTarget(entity, gameTime, pos.pos());
	}

	private float getRandomlySpinningRotation(int seed, long gameTime) {
		if (wobbler.shouldUpdate(gameTime)) {
			wobbler.update(gameTime, random.nextFloat());
		}

		float f = wobbler.getAngle() + (float) hash(seed) / 2.1474836E9F;
		return MathHelper.floorMod(f, 1.0F);
	}

	private float getRotationTowardsCompassTarget(Entity entity, long gameTime, BlockPos pos) {
		float f = (float) getAngleFromEntityToPos(entity, pos);
		float f1 = getWrappedVisualRotationY(entity);
		if (entity instanceof PlayerEntity playerEntity && playerEntity.isMainPlayer() && playerEntity.getWorld().getTickManager().shouldTick()) {
			if (wobbler.shouldUpdate(gameTime)) {
				wobbler.update(gameTime, 0.5F - (f1 - 0.25F));
			}

			float f3 = f + wobbler.getAngle();
			return MathHelper.floorMod(f3, 1.0F);
		}

		float f2 = 0.5F - (f1 - 0.25F - f);
		return MathHelper.floorMod(f2, 1.0F);
	}

	private static boolean isValidCompassTargetPos(Entity entity, @Nullable GlobalPos pos) {
		return pos != null && pos.dimension() == entity.getWorld().getRegistryKey() && !(pos.pos().getSquaredDistance(entity.getPos()) < 1.0E-5F);
	}

	private static double getAngleFromEntityToPos(Entity entity, BlockPos pos) {
		Vec3d vec3d = Vec3d.ofCenter(pos);
		return Math.atan2(vec3d.getZ() - entity.getZ(), vec3d.getX() - entity.getX()) / (float) (Math.PI * 2);
	}

	private static float getWrappedVisualRotationY(Entity entity) {
		return MathHelper.floorMod(entity.getBodyYaw() / 360.0F, 1.0F);
	}

	private static int hash(int seed) {
		return seed * 1327217883;
	}

}