package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.ChunkGeneratorSettings;

public class BiomeUtils {

	public static List<Biome> getAllowedBiomes() {
		final List<Biome> biomes = new ArrayList<Biome>();
		for (Biome biome : Biome.REGISTRY) {
			if (biome != null && !biomeIsBlacklisted(biome)) {
				biomes.add(biome);
			}
		}

		return biomes;
	}

	public static SearchResult searchForBiome(World world, ItemStack stack, Biome biome, BlockPos startPos) {
		if (stack.isEmpty() || stack.getItem() != NaturesCompass.naturesCompass) {
			return null;
		}

		final int sampleSpace = ConfigHandler.sampleSpaceModifier * getBiomeSize(world);
		final int maxDist = ConfigHandler.distanceModifier * getBiomeSize(world);
		if (maxDist <= 0 || sampleSpace <= 0) {
			return new SearchResult(0, 0, maxDist, false);
		}

		final BiomeProvider chunkManager = world.getBiomeProvider();
		final double adjustedSampleSpace = sampleSpace / Math.sqrt(Math.PI);
		final double adjustedPi = 2 * Math.sqrt(Math.PI);
		double dist = 0;
		for (int i = 0; dist < maxDist; i++) {
			double root = Math.sqrt(i);
			dist = adjustedSampleSpace * root;
			double x = startPos.getX() + (dist * Math.sin(adjustedPi * root));
			double z = startPos.getZ() + (dist * Math.cos(adjustedPi * root));

			final Biome[] biomesAtSample = chunkManager.getBiomes(null, (int) x, (int) z, 1, 1, false);
			if (biomesAtSample[0] == biome) {
				return new SearchResult((int) x, (int) z, maxDist, true);
			}
		}

		return new SearchResult(0, 0, maxDist, false);
	}

	public static int getBiomeSize(World world) {
		final String settings = world.getWorldInfo().getGeneratorOptions();
		return ChunkGeneratorSettings.Factory.jsonToFactory(settings).build().biomeSize;
	}

	public static int getDistanceToBiome(EntityPlayer player, int x, int z) {
		return (int) player.getDistance(x, player.posY, z);
	}

	public static String getBiomeName(Biome biome) {
		if (biome != null && biome.getBiomeName() != null) {
			if (ConfigHandler.fixBiomeNames) {
				final String original = biome.getBiomeName();
				String fixed = "";
				char pre = ' ';
				for (int i = 0; i < original.length(); i++) {
					final char c = original.charAt(i);
					if (Character.isUpperCase(c) && Character.isLowerCase(pre) && Character.isAlphabetic(pre)) {
						fixed = fixed + " ";
					}
					fixed = fixed + String.valueOf(c);
					pre = c;
				}

				return fixed;
			}

			return biome.getBiomeName();
		}

		return "";
	}

	public static String getBiomeName(int biomeID) {
		return getBiomeName(Biome.getBiomeForId(biomeID));
	}

	public static boolean biomeIsBlacklisted(Biome biome) {
		final List<String> biomeBlacklist = ConfigHandler.getBiomeBlacklist();
		return biomeBlacklist.contains(String.valueOf(Biome.getIdForBiome(biome)))
				|| biomeBlacklist.contains(getBiomeName(biome)) || biomeBlacklist.contains(biome.getBiomeName());
	}

}
