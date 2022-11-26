package com.chaosthedude.naturescompass.workers;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {

	private final int sampleSpace;
	private final int maxSamples;
	public final int maxRadius;
	private ServerWorld world;
	private Identifier biomeID;
	private BlockPos startPos;
	private int samples;
	private int nextLength;
	private Direction direction;
	private ItemStack stack;
	private PlayerEntity player;
	private int x;
	private int z;
	private int[] yValues;
	private int length;
	private boolean finished;
	private int lastRadiusThreshold;

	public BiomeSearchWorker(ServerWorld world, PlayerEntity player, ItemStack stack, Biome biome, BlockPos startPos) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.startPos = startPos;
		x = startPos.getX();
		z = startPos.getZ();
		yValues = MathHelper.stream(startPos.getY(), world.getBottomY() + 1, world.getTopY(), 64).toArray();
		sampleSpace = NaturesCompassConfig.sampleSpaceModifier * BiomeUtils.getBiomeSize(world);
		maxSamples = NaturesCompassConfig.maxSamples;
		maxRadius = NaturesCompassConfig.radiusModifier * BiomeUtils.getBiomeSize(world);
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		finished = false;
		biomeID = BiomeUtils.getIdentifierForBiome(world, biome);
		lastRadiusThreshold = 0;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
			if (maxRadius > 0 && sampleSpace > 0) {
				NaturesCompass.LOGGER.info("Starting search: " + sampleSpace + " sample space, " + maxSamples + " max samples, " + maxRadius + " max radius");
				WorldWorkerManager.addWorker(this);
			} else {
				fail();
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() <= maxRadius && samples <= maxSamples;
	}

	@Override
	public boolean doWork() {
		if (hasWork()) {
			if (direction == Direction.NORTH) {
				z -= sampleSpace;
			} else if (direction == Direction.EAST) {
				x += sampleSpace;
			} else if (direction == Direction.SOUTH) {
				z += sampleSpace;
			} else if (direction == Direction.WEST) {
				x -= sampleSpace;
			}
			
			int sampleX = BiomeCoords.fromBlock(x);
			int sampleZ = BiomeCoords.fromBlock(z);

			for (int y : yValues) {
				int sampleY = BiomeCoords.fromBlock(y);
				final Biome biomeAtPos = world.getChunkManager().getChunkGenerator().getBiomeSource().getBiome(sampleX, sampleY, sampleZ, world.getChunkManager().getNoiseConfig().getMultiNoiseSampler()).value();
				final Identifier biomeAtPosID = BiomeUtils.getIdentifierForBiome(world, biomeAtPos);
				if (biomeAtPosID != null && biomeAtPosID.equals(biomeID)) {
					succeed();
					return false;
				}
			}

			samples++;
			length += sampleSpace;
			if (length >= nextLength) {
				if (direction != Direction.UP) {
					nextLength += sampleSpace;
					direction = direction.rotateYClockwise();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}
			int radius = getRadius();
 			if (radius > 500 && radius / 500 > lastRadiusThreshold) {
 				if (!stack.isEmpty() && stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
 					((NaturesCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 500), player);
 				}
 				lastRadiusThreshold = radius / 500;
 			}
		}
		if (hasWork()) {
			return true;
		}
		if (!finished) {
			fail();
		}
		return false;
	}

	private void succeed() {
		NaturesCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
			((NaturesCompassItem) stack.getItem()).succeed(stack, player, x, z, samples, NaturesCompassConfig.displayCoordinates);
		} else {
			NaturesCompass.LOGGER.error("Invalid compass after search");
		}
		finished = true;
	}
	
	private void fail() {
		NaturesCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
			((NaturesCompassItem) stack.getItem()).fail(stack, player, roundRadius(getRadius(), 500), samples);
		} else {
			NaturesCompass.LOGGER.error("Invalid compass after search");
		}
		finished = true;
	}
	
	public void stop() {
		NaturesCompass.LOGGER.info("Search stopped: " + getRadius() + " radius, " + samples + " samples");
		finished = true;
	}

	private int getRadius() {
		return BiomeUtils.getDistanceToBiome(startPos, x, z);
	}
	
	private int roundRadius(int radius, int roundTo) {
 		return ((int) radius / roundTo) * roundTo;
 	}

}
