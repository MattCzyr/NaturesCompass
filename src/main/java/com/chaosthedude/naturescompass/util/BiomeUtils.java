package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeUtils {

	public static int getIDForBiome(Biome biome) {
		return WorldGenRegistries.BIOME.getId(biome);
	}

	public static Biome getBiomeForID(int id) {
		return WorldGenRegistries.BIOME.getByValue(id);
	}

	public static List<Biome> getAllowedBiomes() {
		final List<Biome> biomes = new ArrayList<Biome>();
		for (Biome biome : WorldGenRegistries.BIOME) {
			if (biome != null && !biomeIsBlacklisted(biome)) {
				biomes.add(biome);
			}
		}

		return biomes;
	}

	public static void searchForBiome(World world, PlayerEntity player, ItemStack stack, Biome biome, BlockPos startPos) {
		BiomeSearchWorker worker = new BiomeSearchWorker(world, player, stack, biome, startPos);
		worker.start();
	}

	public static int getBiomeSize(World world) {
		// TODO
		return 4;
	}

	public static int getDistanceToBiome(PlayerEntity player, int biomeX, int biomeZ) {
		return getDistanceToBiome(player.getPosition(), biomeX, biomeZ);
	}

	public static int getDistanceToBiome(BlockPos startPos, int biomeX, int biomeZ) {
		return (int) MathHelper.sqrt(startPos.distanceSq(new BlockPos(biomeX, startPos.getY(), biomeZ)));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeNameForDisplay(Biome biome) {
		if (biome != null) {
			if (ConfigHandler.CLIENT.fixBiomeNames.get()) {
				final String original = getBiomeName(biome);
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

			return I18n.format(WorldGenRegistries.BIOME.getKey(biome).toString());
		}

		return "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeName(Biome biome) {
		return I18n.format(Util.makeTranslationKey("biome", WorldGenRegistries.BIOME.getKey(biome)));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeName(int biomeID) {
		return getBiomeName(getBiomeForID(biomeID));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeSource(Biome biome) {
		String registryEntry = WorldGenRegistries.BIOME.getKey(biome).toString();
		String modid = registryEntry.substring(0, registryEntry.indexOf(":"));
		if (modid.equals("minecraft")) {
			return "Minecraft";
		}
		Optional<? extends ModContainer> sourceContainer = ModList.get().getModContainerById(modid);
		if (sourceContainer.isPresent()) {
			return sourceContainer.get().getModInfo().getDisplayName();
		}
		return modid;
	}

	public static boolean biomeIsBlacklisted(Biome biome) {
		final List<String> biomeBlacklist = ConfigHandler.GENERAL.biomeBlacklist.get();
		final ResourceLocation biomeResourceLocation = WorldGenRegistries.BIOME.getKey(biome);
		return biomeBlacklist.contains(String.valueOf(BiomeUtils.getIDForBiome(biome))) || (biomeResourceLocation != null && biomeBlacklist.contains(biomeResourceLocation.toString()));
	}

}
