package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {

	public final int sampleSpace;
	public final int maxRadius;
	public World world;
	public Biome biome;
	public ResourceLocation biomeKey;
	public BlockPos startPos;
	public int samples;
	public int nextLength;
	public Direction direction;
	public ItemStack stack;
	public PlayerEntity player;
	public int x;
	public int z;
	public int length;
	public boolean finished;

	public BiomeSearchWorker(World world, PlayerEntity player, ItemStack stack, Biome biome, BlockPos startPos) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.biome = biome;
		this.startPos = startPos;
		x = startPos.getX();
		z = startPos.getZ();
		sampleSpace = ConfigHandler.GENERAL.sampleSpaceModifier.get() * BiomeUtils.getBiomeSize(world);
		maxRadius = ConfigHandler.GENERAL.radiusModifier.get() * BiomeUtils.getBiomeSize(world);
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		finished = false;
		biomeKey = ForgeRegistries.BIOMES.getKey(biome);
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
			if (direction == Direction.NORTH) {
				z -= sampleSpace;
			} else if (direction == Direction.EAST) {
				x += sampleSpace;
			} else if (direction == Direction.SOUTH) {
				z += sampleSpace;
			} else if (direction == Direction.WEST) {
				x -= sampleSpace;
			}

			final BlockPos pos = new BlockPos(x, world.getHeight(), z);
			final Biome biomeAtPos = world.getBiomeManager().getBiome(pos);
			final ResourceLocation biomeAtPosKey = world.func_241828_r().getRegistry(Registry.BIOME_KEY).getKey(biomeAtPos);
			if (biomeAtPosKey != null && biomeAtPosKey.equals(biomeKey)) {
				finish(true);
				return false;
			}

			samples++;
			length += sampleSpace;
			if (length >= nextLength) {
				if (direction != Direction.UP) {
					nextLength += sampleSpace;
					direction = direction.rotateY();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
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
				NaturesCompass.logger.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
				((NaturesCompassItem) stack.getItem()).setFound(stack, x, z, samples, player);
			} else {
				NaturesCompass.logger.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
				((NaturesCompassItem) stack.getItem()).setNotFound(stack, player, getRadius(), samples);
			}
		} else {
			NaturesCompass.logger.error("Invalid compass after search");
		}
		finished = true;
	}

	private int getRadius() {
		return BiomeUtils.getDistanceToBiome(startPos, x, z);
	}

}
