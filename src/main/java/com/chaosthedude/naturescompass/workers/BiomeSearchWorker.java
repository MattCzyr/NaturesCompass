package com.chaosthedude.naturescompass.workers;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

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

	private final String id = RandomStringUtils.random(8, "0123456789abcdef");

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
	private List<BlockPos> prevPos;
	private boolean checkingConnections;
	private BlockPos candidatePos;
	private int prevPosIndex;
	private int connectionsCheckSampleIndex;
	private int connectionsCheckSamples;
	private int consecutiveNonMatchingSamples;

	public BiomeSearchWorker(ServerWorld world, PlayerEntity player, ItemStack stack, Identifier biomeID, BlockPos startPos, List<BlockPos> prevPos) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.biomeID = biomeID;
		this.startPos = startPos;
		this.prevPos = prevPos;
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
		lastRadiusThreshold = 0;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
			if (maxRadius > 0 && sampleSpace > 0) {
				NaturesCompass.LOGGER.info("BiomeSearchWorker " + id + ": Starting search: " + sampleSpace + " sample space, " + maxSamples + " max samples, " + maxRadius + " max radius, " + prevPos.size() + " previous locations");
				WorldWorkerManager.addWorker(this);
			} else {
				fail();
			}
		}
	}

	@Override
	public boolean hasWork() {
		return biomeID != null && !finished && getRadius() <= maxRadius && samples <= maxSamples;
	}

	@Override
	public boolean doWork() {
		if (checkingConnections) {
			checkConnections();
			return !finished;
		}

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
					if (prevPos.isEmpty()) {
						prevPos.add(new BlockPos(x, y, z));
						succeed();
						return false;
					}

					checkingConnections = true;
					candidatePos = new BlockPos(x, y, z);
					prevPosIndex = 0;
					connectionsCheckSampleIndex = 0;
					return true;
				}
			}

			finishSample();
		}
		if (hasWork()) {
			return true;
		}
		if (!finished) {
			fail();
		}
		return false;
	}

	// Performs one operation toward checking whether the pending candidate is connected to any
	// previously found location. Calls succeed() if not connected or finishSample() and clears
	// the check state if connected
	private void checkConnections() {
		if (prevPosIndex >= prevPos.size()) {
			// If we've gotten through all previous locations without finding one that's connected,
			// accept the candidate
			prevPos.add(candidatePos);
			checkingConnections = false;
			succeed();
			return;
		}

		BlockPos currentPrevPos = prevPos.get(prevPosIndex);
		int deltaX = candidatePos.getX() - currentPrevPos.getX();
		int deltaZ = candidatePos.getZ() - currentPrevPos.getZ();

		if (connectionsCheckSampleIndex == 0) {
			// If the candidate is within 2 sample spaces of a previous location, assume connected, skip candidate
			double distance = Math.sqrt((double) (deltaX * deltaX) + (double) (deltaZ * deltaZ));
			if (distance <= sampleSpace * 2) {
				checkingConnections = false;
				finishSample();
				return;
			}

			connectionsCheckSamples = (int) (distance / sampleSpace);
			consecutiveNonMatchingSamples = 0;
			connectionsCheckSampleIndex = 1;
			return;
		}

		if (connectionsCheckSampleIndex >= connectionsCheckSamples) {
			// All line samples finished without finding large enough gap, consider connected, skip candidate
			checkingConnections = false;
			finishSample();
			return;
		}

		// Sample one point along a straight line from previous location to candidate location
		double t = (double) connectionsCheckSampleIndex / connectionsCheckSamples;
		int checkSampleX = BiomeCoords.fromBlock((int) (currentPrevPos.getX() + deltaX * t));
		int checkSampleZ = BiomeCoords.fromBlock((int) (currentPrevPos.getZ() + deltaZ * t));

		// Check configured Y values at the sample for a biome match
		boolean foundMatchingBiome = false;
		for (int y : yValues) {
			int checkSampleY = BiomeCoords.fromBlock(y);
			final Biome biomeAtCheck = world.getChunkManager().getChunkGenerator().getBiomeSource().getBiome(checkSampleX, checkSampleY, checkSampleZ, world.getChunkManager().getNoiseConfig().getMultiNoiseSampler()).value();
			final Identifier checkBiomeId = BiomeUtils.getIdentifierForBiome(world, biomeAtCheck);
			if (checkBiomeId != null && checkBiomeId.equals(biomeID)) {
				foundMatchingBiome = true;
				break;
			}
		}

		if (foundMatchingBiome) {
			// Found matching biome, reset number of consecutive non matching samples
			consecutiveNonMatchingSamples = 0;
		} else {
			consecutiveNonMatchingSamples++;
			// If there's a stretch of consecutive samples equal to 25% the total samples (max 10)
			// between the candidate location and previous location, consider the candidate not
			// connected to the previous location
			int consecutiveNonMatchingThreshold = MathHelper.clamp(connectionsCheckSamples / 4, 1, 10);
			if (consecutiveNonMatchingSamples >= consecutiveNonMatchingThreshold) {
				prevPosIndex++;
				connectionsCheckSampleIndex = 0;
				consecutiveNonMatchingSamples = 0;
				return;
			}
		}

		connectionsCheckSampleIndex++;
	}

	// Counts sample, sets up direction for next sample, and updates compass radius
	private void finishSample() {
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

	private void succeed() {
		NaturesCompass.LOGGER.info("BiomeSearchWorker " + id + ": Search succeeded: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
			((NaturesCompassItem) stack.getItem()).succeed(stack, player, biomeID, x, z, prevPos, samples, NaturesCompassConfig.displayCoordinates);
		} else {
			NaturesCompass.LOGGER.error("BiomeSearchWorker " + id + ": Invalid compass after search");
		}
		finished = true;
	}

	private void fail() {
		NaturesCompass.LOGGER.info("BiomeSearchWorker " + id + ": Search failed: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
			((NaturesCompassItem) stack.getItem()).fail(stack, player, biomeID, roundRadius(getRadius(), 500), samples);
		} else {
			NaturesCompass.LOGGER.error("BiomeSearchWorker " + id + ": Invalid compass after search");
		}
		finished = true;
	}

	public void stop() {
		NaturesCompass.LOGGER.info("BiomeSearchWorker " + id + ": Search stopped: " + getRadius() + " radius, " + samples + " samples");
		finished = true;
	}

	private int getRadius() {
		return BiomeUtils.getDistanceToBiome(startPos, x, z);
	}

	private int roundRadius(int radius, int roundTo) {
 		return ((int) radius / roundTo) * roundTo;
 	}

}
