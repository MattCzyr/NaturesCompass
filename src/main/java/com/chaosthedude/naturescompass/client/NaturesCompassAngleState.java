package com.chaosthedude.naturescompass.client;

import javax.annotation.Nullable;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.CompassState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NaturesCompassAngleState extends NeedleDirectionHelper {

	private final NeedleDirectionHelper.Wobbler wobbler;
	private final RandomSource random = RandomSource.create();

	public NaturesCompassAngleState() {
		super(true);
		wobbler = newWobbler(0.8F);
	}

	@Override
	protected float calculate(ItemStack stack, ClientLevel level, int seed, Entity entity) {
		GlobalPos pos = new GlobalPos(level.dimension(), level.getSharedSpawnPos());
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			NaturesCompassItem compassItem = (NaturesCompassItem) stack.getItem();
			if (compassItem.getState(stack) == CompassState.FOUND) {
				pos = new GlobalPos(level.dimension(), new BlockPos(compassItem.getFoundBiomeX(stack), 0, compassItem.getFoundBiomeZ(stack)));
			}
		}
		long gameTime = level.getGameTime();
		return !isValidCompassTargetPos(entity, pos) ? getRandomlySpinningRotation(seed, gameTime) : getRotationTowardsCompassTarget(entity, gameTime, pos.pos());
	}

	private float getRandomlySpinningRotation(int seed, long gameTime) {
		if (wobbler.shouldUpdate(gameTime)) {
			wobbler.update(gameTime, random.nextFloat());
		}

		float f = wobbler.rotation() + (float) hash(seed) / 2.1474836E9F;
		return Mth.positiveModulo(f, 1.0F);
	}

	private float getRotationTowardsCompassTarget(Entity entity, long gameTime, BlockPos pos) {
		float f = (float) getAngleFromEntityToPos(entity, pos);
		float f1 = getWrappedVisualRotationY(entity);
		if (entity instanceof Player player && player.isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
			if (wobbler.shouldUpdate(gameTime)) {
				wobbler.update(gameTime, 0.5F - (f1 - 0.25F));
			}

			float f3 = f + wobbler.rotation();
			return Mth.positiveModulo(f3, 1.0F);
		}

		float f2 = 0.5F - (f1 - 0.25F - f);
		return Mth.positiveModulo(f2, 1.0F);
	}

	private static boolean isValidCompassTargetPos(Entity entity, @Nullable GlobalPos pos) {
		return pos != null && pos.dimension() == entity.level().dimension() && !(pos.pos().distToCenterSqr(entity.position()) < 1.0E-5F);
	}

	private static double getAngleFromEntityToPos(Entity entity, BlockPos pos) {
		Vec3 vec3 = Vec3.atCenterOf(pos);
		return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX()) / (float) (Math.PI * 2);
	}

	private static float getWrappedVisualRotationY(Entity entity) {
		return Mth.positiveModulo(entity.getVisualRotationYInDegrees() / 360.0F, 1.0F);
	}

	private static int hash(int seed) {
		return seed * 1327217883;
	}

}