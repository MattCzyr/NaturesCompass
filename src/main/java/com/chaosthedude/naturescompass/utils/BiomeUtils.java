package com.chaosthedude.naturescompass.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.workers.BiomeSearchWorker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BiomeUtils {
	
	public static Registry<Biome> getBiomeRegistry(World world) {
		return world.getRegistryManager().get(Registry.BIOME_KEY);
	}

	public static Identifier getIdentifierForBiome(World world, Biome biome) {
		return getBiomeRegistry(world).getId(biome);
	}

	public static Optional<Biome> getBiomeForIdentifier(World world, Identifier id) {
		return getBiomeRegistry(world).getOrEmpty(id);
	}

	public static List<Identifier> getAllowedBiomeIDs(World world) {
		final List<Identifier> biomeIDs = new ArrayList<Identifier>();
		for (Map.Entry<RegistryKey<Biome>, Biome> entry : getBiomeRegistry(world).getEntries()) {
			Biome biome = entry.getValue();
			if (biome != null) {
				Identifier biomeID = getIdentifierForBiome(world, biome);
				if (biomeID != null && !biomeIDIsBlacklisted(world, biomeID)) {
					biomeIDs.add(biomeID);
				}
			}
		}

		return biomeIDs;
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
		return getDistanceToBiome(player.getBlockPos(), biomeX, biomeZ);
	}

	public static int getDistanceToBiome(BlockPos startPos, int biomeX, int biomeZ) {
		return (int) MathHelper.sqrt((float) startPos.getSquaredDistance(new BlockPos(biomeX, startPos.getY(), biomeZ)));
	}
	
	@Environment(EnvType.CLIENT)
	public static String getBiomeCategoryName(World level, Biome biome) {
		String biomeKey = Util.createTranslationKey("biome", new Identifier(biome.getCategory().getName()));
		String translatedBiomeKey = I18n.translate(biomeKey);
		if (!biomeKey.equals(translatedBiomeKey)) {
			return translatedBiomeKey;
		}
		String categoryKey = Util.createTranslationKey("category", new Identifier(biome.getCategory().getName()));
		String translatedCategoryKey = I18n.translate(categoryKey);
		if (!categoryKey.equals(translatedCategoryKey)) {
			return translatedCategoryKey;
		}
		return WordUtils.capitalize(biome.getCategory().getName().replace('_', ' '));
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeNameForDisplay(World world, Biome biome) {
		if (biome != null) {
			if (NaturesCompassConfig.fixBiomeNames) {
				final String original = getBiomeName(world, biome);
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

			if (getIdentifierForBiome(world, biome) != null) {
				return I18n.translate(getIdentifierForBiome(world, biome).toString());
			}
		}

		return "";
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeName(World world, Biome biome) {
		return I18n.translate(Util.createTranslationKey("biome", getIdentifierForBiome(world, biome)));
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeName(World world, Identifier biomeID) {
		if (getBiomeForIdentifier(world, biomeID).isPresent()) {
			return getBiomeName(world, getBiomeForIdentifier(world, biomeID).get());
		}
		return "";
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeSource(World world, Biome biome) {
		String registryEntry = getIdentifierForBiome(world, biome).toString();
		String modid = registryEntry.substring(0, registryEntry.indexOf(":"));
		if (modid.equals("minecraft")) {
			return "Minecraft";
		}
		Optional<ModContainer> sourceContainer = FabricLoader.getInstance().getModContainer(modid);
		if (sourceContainer.isPresent()) {
			return sourceContainer.get().getMetadata().getName();
		}
		return modid;
	}

	public static boolean biomeIDIsBlacklisted(World world, Identifier biomeID) {
		final List<String> biomeBlacklist = NaturesCompassConfig.biomeBlacklist;
		for (String biomeKey : biomeBlacklist) {
 			if (biomeID.toString().matches(convertToRegex(biomeKey))) {
 				return true;
 			}
 		}
 		return false;
	}
	
	private static String convertToRegex(String glob) {
 		String regex = "^";
 		for (char i = 0; i < glob.length(); i++) {
 			char c = glob.charAt(i);
 			if (c == '*') {
 				regex += ".*";
 			} else if (c == '?') {
 				regex += ".";
 			} else if (c == '.') {
 				regex += "\\.";
 			} else {
 				regex += c;
 			}
 		}
 		regex += "$";
 		return regex;
 	}

}