package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeUtils {

	public static List<BiomeGenBase> getAllowedBiomes() {
		final List<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null && !biomeIsBlacklisted(biome)) {
				biomes.add(biome);
			}
		}

		return biomes;
	}

	public static SearchResult searchForBiome(World world, ItemStack stack, BiomeGenBase biome, int startX, int startY) {
		if (stack.getItem() != NaturesCompass.naturesCompass) {
			return null;
		}

		final int sampleSpace = ConfigHandler.sampleSpace;
		final int maxDist = ConfigHandler.maxSearchDistance;
		if (maxDist <= 0 || sampleSpace <= 0) {
			return new SearchResult(0, 0, maxDist, false);
		}

		final double adjustedSampleSpace = sampleSpace / Math.sqrt(Math.PI);
		final double adjustedPi = 2 * Math.sqrt(Math.PI);
		double dist = 0;
		for (int i = 0; dist < maxDist; i++) {
			double root = Math.sqrt(i);
			dist = adjustedSampleSpace * root;
			double x = startX + (dist * Math.sin(adjustedPi * root));
			double z = startY + (dist * Math.cos(adjustedPi * root));

			final BiomeGenBase biomeAtSample = world.getBiomeGenForCoords((int) x, (int) z);
			if (biomeAtSample == biome) {
				return new SearchResult((int) x, (int) z, maxDist, true);
			}
		}

		return new SearchResult(0, 0, maxDist, false);
	}

	public static int getDistanceToBiome(EntityPlayer player, int x, int z) {
		return (int) player.getDistance(x, player.posY, z);
	}

	public static String getBiomeName(BiomeGenBase biome) {
		if (ConfigHandler.fixBiomeNames) {
			final String original = biome.biomeName;
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

		return biome.biomeName;
	}

	public static String getBiomeName(int biomeID) {
		return getBiomeName(BiomeGenBase.getBiome(biomeID));
	}

	public static boolean biomeIsBlacklisted(BiomeGenBase biome) {
		final List<String> biomeBlacklist = ConfigHandler.getBiomeBlacklist();
		return biomeBlacklist.contains(String.valueOf(biome.biomeID)) || biomeBlacklist.contains(getBiomeName(biome)) || biomeBlacklist.contains(biome.biomeName);
	}

}
