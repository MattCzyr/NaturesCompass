package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.WorldWorkerManager;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {
	
	public final int sampleSpace;
	public final int maxDistance;
	public final double sampleMomentum;
	public World world;
	public Biome biome;
	public Biome lastBiome;
	public BlockPos startPos;
	public int samples;
	public int lastStep;
	public int nextLength;
	public EnumFacing direction;
	public ItemStack stack;
	public EntityPlayer player;
	public int x;
	public int z;
	public int length;
	public boolean finished;
	
	public BiomeSearchWorker(World world, EntityPlayer player, ItemStack stack, Biome biome, BlockPos startPos) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.biome = biome;
		this.startPos = startPos;
		x = startPos.getX();
		z = startPos.getZ();
		sampleSpace = ConfigHandler.sampleSpaceModifier * BiomeUtils.getBiomeSize(world);
		maxDistance = ConfigHandler.distanceModifier * BiomeUtils.getBiomeSize(world);
		sampleMomentum = ConfigHandler.sampleMomentumModifier;
		lastBiome = biome;
		lastStep = 0;
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = EnumFacing.UP;
		finished = false;
	}
	
	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (maxDistance > 0 && sampleSpace > 0) {
				NaturesCompass.logger.info("Starting search: " + sampleSpace + " sample space, " + maxDistance + " max distance");
				WorldWorkerManager.addWorker(this);
			} else {
				finish(false);
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() < maxDistance && samples < ConfigHandler.maxSamples;
	}

	@Override
	public boolean doWork() {
		if (hasWork()) {
			final int step = sampleSpace + (int)(lastStep * sampleMomentum);
		  int stepsRemaining = step;
			while (stepsRemaining > 0) {
				final int unseenLength = nextLength - length;
				final int segment = Math.min(unseenLength, stepsRemaining);
				if (direction == EnumFacing.NORTH) {
					z -= segment;
				} else if (direction == EnumFacing.EAST) {
					x += segment;
				} else if (direction == EnumFacing.SOUTH) {
					z += segment;
				} else if (direction == EnumFacing.WEST) {
					x -= segment;
				}
				length += segment;
				if (length >= nextLength) {
					rotate();
				}
				stepsRemaining -= segment;
			}

			final BlockPos pos = new BlockPos(x, world.getHeight(), z);
			final Biome biomeAtPos = world.getBiomeForCoordsBody(pos);
			if (biomeAtPos == biome) {
				finish(true);
				return false;
			} else if (biomeAtPos != lastBiome) {
				lastBiome = biomeAtPos;
				lastStep = 0;
			}

			samples++;
			lastStep = step;
		}
		if (hasWork()) {
			return true;
		}
		finish(false);
		return false;
	}

	private void rotate() {
		if (direction != EnumFacing.UP) {
			nextLength += sampleSpace;
			direction = direction.rotateY();
		} else {
			direction = EnumFacing.NORTH;
		}
		length = 0;
	}
	
	private void finish(boolean found) {
		if (found) {
			NaturesCompass.logger.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
			((ItemNaturesCompass) stack.getItem()).setFound(stack, x, z, samples, player);
		} else {
			NaturesCompass.logger.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
			((ItemNaturesCompass) stack.getItem()).setNotFound(stack, player, getRadius(), samples);
		}
		finished = true;
	}
	
	private int getRadius() {
		return (int) startPos.getDistance(x, startPos.getY(), z);
	}

}
