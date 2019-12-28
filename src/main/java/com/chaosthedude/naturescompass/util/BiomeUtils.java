package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
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

	@SideOnly(Side.CLIENT)
	public static String getBiomeSource(Biome biome) {
		if (biome != null && biome.getRegistryName() != null) {
			String registryEntry = biome.getRegistryName().toString();
			String modid = registryEntry.substring(0, registryEntry.indexOf(":"));
			ModContainer sourceContainer = Loader.instance().getIndexedModList().get(modid);
			return sourceContainer != null ? sourceContainer.getName() : modid;
		}
		return "";
	}

	@SideOnly(Side.CLIENT)
	public static String getBiomeSource(int biomeID) {
		return getBiomeSource(Biome.getBiomeForId(biomeID));
	}


	public static boolean biomeIsBlacklisted(Biome biome) {
		final List<String> biomeBlacklist = ConfigHandler.getBiomeBlacklist();
		final ResourceLocation biomeResourceLocation = ForgeRegistries.BIOMES.getKey(biome);
		return biomeBlacklist.contains(String.valueOf(Biome.getIdForBiome(biome)))
				|| (biomeResourceLocation != null && biomeBlacklist.contains(biomeResourceLocation.toString()));
	}

}
