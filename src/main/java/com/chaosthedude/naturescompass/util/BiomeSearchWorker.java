package com.chaosthedude.naturescompass.util;

import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.WorldWorkerManager;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {

	public final int sampleSpace;
	public final int maxSamples;
	public final int maxRadius;
	public Level level;
	public Biome biome;
	public ResourceLocation biomeKey;
	public BlockPos startPos;
	public int samples;
	public int nextLength;
	public Direction direction;
	public ItemStack stack;
	public Player player;
	public int x;
	public int y;
	public int z;
	public int length;
	public boolean finished;
	public int lastRadiusThreshold;

	public BiomeSearchWorker(Level level, Player player, ItemStack stack, Biome biome, BlockPos startPos) {
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.biome = biome;
		this.startPos = startPos;
		x = startPos.getX();
		y = startPos.getY();
		z = startPos.getZ();
		sampleSpace = ConfigHandler.GENERAL.sampleSpaceModifier.get() * BiomeUtils.getBiomeSize(level);
		maxSamples = ConfigHandler.GENERAL.maxSamples.get();
		maxRadius = ConfigHandler.GENERAL.radiusModifier.get() * BiomeUtils.getBiomeSize(level);
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		finished = false;
		biomeKey = BiomeUtils.getKeyForBiome(level, biome).isPresent() ? BiomeUtils.getKeyForBiome(level, biome).get() : null;
		lastRadiusThreshold = 0;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (maxRadius > 0 && sampleSpace > 0) {
				NaturesCompass.LOGGER.info("Starting search: " + sampleSpace + " sample space, " + maxSamples + " max samples, " + maxRadius + " max radius");
				WorldWorkerManager.addWorker(this);
			} else {
				finish(false);
			}
		}
	}

	@Override
	public boolean hasWork() {
		return biomeKey != null && !finished && getRadius() <= maxRadius && samples <= maxSamples;
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

			final Biome biomeAtPos = level.getBiomeManager().getNoiseBiomeAtPosition(new BlockPos(x, y, z));
			final Optional<ResourceLocation> optionalBiomeAtPosKey = BiomeUtils.getKeyForBiome(level, biomeAtPos);
			if (optionalBiomeAtPosKey.isPresent() && optionalBiomeAtPosKey.get().equals(biomeKey)) {
				finish(true);
				return false;
			}

			samples++;
			length += sampleSpace;
			if (length >= nextLength) {
				if (direction != Direction.UP) {
					nextLength += sampleSpace;
					direction = direction.getClockWise();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}
			int radius = getRadius();
			if (radius > 500 && radius / 500 > lastRadiusThreshold) {
				if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
					((NaturesCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 500), player);
				}
				lastRadiusThreshold = radius / 500;
			}
		}
		if (hasWork()) {
			return true;
		}
		finish(false);
		return false;
	}

	private void finish(boolean found) {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (found) {
				NaturesCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
				((NaturesCompassItem) stack.getItem()).setFound(stack, x, z, samples, player);
				((NaturesCompassItem) stack.getItem()).setDisplayCoordinates(stack, ConfigHandler.GENERAL.displayCoordinates.get());
			} else {
				NaturesCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
				((NaturesCompassItem) stack.getItem()).setNotFound(stack, player, roundRadius(getRadius(), 500), samples);
			}
		} else {
			NaturesCompass.LOGGER.error("Invalid compass after search");
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