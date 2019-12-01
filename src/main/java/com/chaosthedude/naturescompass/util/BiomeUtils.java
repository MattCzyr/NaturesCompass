package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		final int maxDistance = ConfigHandler.distanceModifier * getBiomeSize(world);
		if (maxDistance <= 0 || sampleSpace <= 0) {
			return new SearchResult(0, 0, maxDistance, 0, false);
		}

		final BiomeProvider biomeProvider = world.getBiomeProvider();
		int direction = -1;
		int samples = 0;
		int nextLength = sampleSpace;
		int x = startPos.getX();
		int z = startPos.getZ();
		while (nextLength / 2 <= maxDistance && samples <= ConfigHandler.maxSamples) {
			final int fixedDirection = direction == -1 ? -1 : direction % 4;
			for (int i = 0; i < nextLength; i += sampleSpace) {
				if (fixedDirection == 0) {
					x += sampleSpace;
				} else if (fixedDirection == 1) {
					z -= sampleSpace;
				} else if (fixedDirection == 2) {
					x -= sampleSpace;
				} else if (fixedDirection == 3) {
					z += sampleSpace;
				}

				final BlockPos pos = new BlockPos(x, world.getHeight(x, z), z);
				final Biome biomeAtPos = world.getChunkFromBlockCoords(pos).getBiome(pos, world.getBiomeProvider());
				if (biomeAtPos == biome) {
					return new SearchResult(x, z, nextLength / 2, samples, true);
				}

				samples++;
			}

			if (direction >= 0) {
				nextLength += sampleSpace;
			}
			direction++;
		}

		return new SearchResult(0, 0, nextLength / 2, samples, false);
	}

	public static int getBiomeSize(World world) {
		final String settings = world.getWorldInfo().getGeneratorOptions();
		return ChunkGeneratorSettings.Factory.jsonToFactory(settings).build().biomeSize;
	}

	public static int getDistanceToBiome(EntityPlayer player, int x, int z) {
		return (int) player.getDistance(x, player.posY, z);
	}

	@SideOnly(Side.CLIENT)
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

	@SideOnly(Side.CLIENT)
	public static String getBiomeName(int biomeID) {
		return getBiomeName(Biome.getBiomeForId(biomeID));
	}

	public static boolean biomeIsBlacklisted(Biome biome) {
		final List<String> biomeBlacklist = ConfigHandler.getBiomeBlacklist();
		final ResourceLocation biomeResourceLocation = ForgeRegistries.BIOMES.getKey(biome);
		return biomeBlacklist.contains(String.valueOf(Biome.getIdForBiome(biome)))
				|| (biomeResourceLocation != null && biomeBlacklist.contains(biomeResourceLocation.toString()));
	}

}
