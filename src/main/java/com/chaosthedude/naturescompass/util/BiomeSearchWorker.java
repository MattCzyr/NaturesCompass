package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.WorldWorkerManager;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {

	public final int sampleSpace;
	public final int maxStep;
	public final int maxRadius;
	public final double sampleMomentum;
	public World world;
	public Biome biome;
	public ResourceLocation biomeKey;
	public ResourceLocation lastBiomeKey;
	public BlockPos startPos;
	public int samples;
	public int lastStep;
	public int nextLength;
	public Direction direction;
	public ItemStack stack;
	public PlayerEntity player;
	public int x;
	public int z;
	public int length;
	public boolean finished;
	public int lastRadiusThreshold;

	public BiomeSearchWorker(World world, PlayerEntity player, ItemStack stack, Biome biome, BlockPos startPos) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.biome = biome;
		this.startPos = startPos;
		x = startPos.getX();
		z = startPos.getZ();
		sampleSpace = ConfigHandler.GENERAL.sampleSpaceModifier.get() * BiomeUtils.getBiomeSize(world);
		maxStep =  ConfigHandler.GENERAL.sampleStepMaximum.get();
		maxRadius = ConfigHandler.GENERAL.radiusModifier.get() * BiomeUtils.getBiomeSize(world);
		sampleMomentum = ConfigHandler.GENERAL.sampleMomentumModifier.get();
		lastStep = 0;
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		finished = false;
		biomeKey = BiomeUtils.getKeyForBiome(world, biome);
		lastRadiusThreshold = 0;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (maxRadius > 0 && sampleSpace > 0) {
				NaturesCompass.logger.info("Starting search: " + sampleSpace + " sample space, " + maxRadius + " max radius");
				WorldWorkerManager.addWorker(this);
			} else {
				finish(false);
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() <= maxRadius && samples <= ConfigHandler.GENERAL.maxSamples.get();
	}

	@Override
	public boolean doWork() {
		if (hasWork()) {
			final int step = Math.min(maxStep, sampleSpace + (int)(lastStep * sampleMomentum));
			int stepsRemaining = step;
			while (stepsRemaining > 0) {
				final int unseenLength = nextLength - length;
				final int segment = Math.min(unseenLength, stepsRemaining);
				if (direction == Direction.NORTH) {
					z -= segment;
				} else if (direction == Direction.EAST) {
					x += segment;
				} else if (direction == Direction.SOUTH) {
					z += segment;
				} else if (direction == Direction.WEST) {
					x -= segment;
				}
				length += segment;
				if (length >= nextLength) {
					rotate();
				}
				stepsRemaining -= segment;
			}

			final BlockPos pos = new BlockPos(x, world.getHeight(), z);
			final Biome biomeAtPos = world.getBiomeManager().getBiome(pos);
			final ResourceLocation biomeAtPosKey = BiomeUtils.getKeyForBiome(world, biomeAtPos);
			if (biomeAtPosKey != null && biomeAtPosKey.equals(biomeKey)) {
				finish(true);
				return false;
			} else if (biomeAtPosKey != lastBiomeKey) {
				lastBiomeKey = biomeAtPosKey;
				lastStep = 0;
			}

			samples++;
			lastStep = step;
			int radius = getRadius();
			if (radius > 500 && radius / 500 > lastRadiusThreshold) {
				((NaturesCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 500), player);
				lastRadiusThreshold = radius / 500;
			}
		}
		if (hasWork()) {
			return true;
		}
		finish(false);
		return false;
	}

	private void rotate() {
		if (direction != Direction.UP) {
			nextLength += sampleSpace;
			direction = direction.rotateY();
		} else {
			direction = Direction.NORTH;
		}
		length = 0;
	}
	
	private void finish(boolean found) {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (found) {
				NaturesCompass.logger.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
				((NaturesCompassItem) stack.getItem()).setFound(stack, x, z, samples, player);
			} else {
				NaturesCompass.logger.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
				((NaturesCompassItem) stack.getItem()).setNotFound(stack, player, roundRadius(getRadius(), 500), samples);
			}
		} else {
			NaturesCompass.logger.error("Invalid compass after search");
		}
		finished = true;
	}

	private int getRadius() {
		return BiomeUtils.getDistanceToBiome(startPos, x, z);
	}
	
	private int roundRadius(int radius, int roundTo) {
		return ((int) radius / roundTo) * roundTo;
	}

}
