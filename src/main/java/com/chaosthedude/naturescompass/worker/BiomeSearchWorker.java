package com.chaosthedude.naturescompass.worker;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.item.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {

	private final String id = RandomStringUtils.random(8, "0123456789abcdef");

	private final int sampleSpace;
	private final int maxSamples;
	private final int maxRadius;
	private ServerLevel level;
	private Identifier biomeId;
	private BlockPos startPos;
	private int samples;
	private int nextLength;
	private Direction direction;
	private ItemStack stack;
	private Player player;
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

	public BiomeSearchWorker(ServerLevel level, Player player, ItemStack stack, Biome biome, BlockPos startPos, List<BlockPos> prevPos) {
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.startPos = startPos;
		this.prevPos = prevPos;
		x = startPos.getX();
		z = startPos.getZ();
		yValues = Mth.outFromOrigin(startPos.getY(), level.getMinY() + 1, level.getMaxY(), 64).toArray();
		sampleSpace = ConfigHandler.GENERAL.sampleSpaceModifier.get() * BiomeUtils.getBiomeSize(level);
		maxSamples = ConfigHandler.GENERAL.maxSamples.get();
		maxRadius = ConfigHandler.GENERAL.radiusModifier.get() * BiomeUtils.getBiomeSize(level);
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		finished = false;
		biomeId = BiomeUtils.getIdForBiome(level, biome).isPresent() ? BiomeUtils.getIdForBiome(level, biome).get() : null;
		lastRadiusThreshold = 0;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
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
		return biomeId != null && !finished && getRadius() <= maxRadius && samples <= maxSamples;
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

			int sampleX = QuartPos.fromBlock(x);
			int sampleZ = QuartPos.fromBlock(z);

			for (int y : yValues) {
				int sampleY = QuartPos.fromBlock(y);
				final Biome biomeAtPos = level.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(sampleX, sampleY, sampleZ, level.getChunkSource().randomState().sampler()).value();
				final Optional<Identifier> optionalBiomeAtPosId = BiomeUtils.getIdForBiome(level, biomeAtPos);
				if (optionalBiomeAtPosId.isPresent() && optionalBiomeAtPosId.get().equals(biomeId)) {
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
		int checkSampleX = QuartPos.fromBlock((int) (currentPrevPos.getX() + deltaX * t));
		int checkSampleZ = QuartPos.fromBlock((int) (currentPrevPos.getZ() + deltaZ * t));

		// Check configured Y values at the sample for a biome match
		boolean foundMatchingBiome = false;
		for (int y : yValues) {
			int checkSampleY = QuartPos.fromBlock(y);
			final Biome biomeAtCheck = level.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(checkSampleX, checkSampleY, checkSampleZ, level.getChunkSource().randomState().sampler()).value();
			final Optional<Identifier> checkBiomeId = BiomeUtils.getIdForBiome(level, biomeAtCheck);
			if (checkBiomeId.isPresent() && checkBiomeId.get().equals(biomeId)) {
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
			int consecutiveNonMatchingThreshold = Math.clamp(connectionsCheckSamples / 4, 1, 10);
			if (consecutiveNonMatchingSamples >= consecutiveNonMatchingThreshold) {
				prevPosIndex++;
				connectionsCheckSampleIndex = 0;
				consecutiveNonMatchingSamples = 0;
				return;
			}
		}

		connectionsCheckSampleIndex++;
	}

	// Counts sample, set up direction for next sample, and updates compass radius. Called at the end of
	// a sample if a match is not found and at the end of the connections check if the candidate is skipped
	private void finishSample() {
		samples++;
		length += sampleSpace;
		if (length >= nextLength) {
			if (direction == Direction.UP) {
				direction = Direction.NORTH;
			} else {
				nextLength += sampleSpace;
				direction = direction.getClockWise();
			}
			length = 0;
		}
		
		int radius = getRadius();
		if (radius > 500 && radius / 500 > lastRadiusThreshold) {
			if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
				stack.set(NaturesCompass.SEARCH_RADIUS, roundRadius(radius, 500));
			}
			lastRadiusThreshold = radius / 500;
		}
	}

	private void succeed() {
		NaturesCompass.LOGGER.info("BiomeSearchWorker " + id + ": Search succeeded: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			((NaturesCompassItem) stack.getItem()).succeed(stack, biomeId, x, z, prevPos, samples, ConfigHandler.GENERAL.displayCoordinates.get());
		} else {
			NaturesCompass.LOGGER.error("BiomeSearchWorker " + id + ": Invalid compass after search");
		}
		finished = true;
	}

	private void fail() {
		NaturesCompass.LOGGER.info("BiomeSearchWorker " + id + ": Search failed: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			((NaturesCompassItem) stack.getItem()).fail(stack, biomeId, roundRadius(getRadius(), 500), samples);
		} else {
			NaturesCompass.LOGGER.error("Invalid compass after search");
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
