package com.chaosthedude.naturescompass.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class BiomeUtils {
	
	public static Registry<Biome> getBiomeRegistry(Level level) {
		return level.registryAccess().lookupOrThrow(Registries.BIOME);
	}

	public static Identifier getIdentifierForBiome(Level level, Biome biome) {
		return getBiomeRegistry(level).getKey(biome);
	}

	public static Optional<Biome> getBiomeForIdentifier(Level level, Identifier id) {
		return getBiomeRegistry(level).getOptional(id);
	}

	public static List<Identifier> getAllowedBiomeIDs(Level level) {
		final List<Identifier> biomeIDs = new ArrayList<Identifier>();
		for (Map.Entry<ResourceKey<Biome>, Biome> entry : getBiomeRegistry(level).entrySet()) {
			Biome biome = entry.getValue();
			if (biome != null) {
				Identifier biomeID = getIdentifierForBiome(level, biome);
				if (biomeID != null && !biomeIDIsBlacklisted(level, biomeID) && !biomeIDIsHidden(level, biomeID)) {
					biomeIDs.add(biomeID);
				}
			}
		}

		return biomeIDs;
	}
	
	public static List<Identifier> getGeneratingDimensionIDs(ServerLevel serverLevel, Biome biome) {
		final List<Identifier> dimensions = new ArrayList<Identifier>();
		final Registry<Biome> biomeRegistry = getBiomeRegistry(serverLevel);
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			Set<Holder<Biome>> biomeSet = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
			Holder<Biome> biomeHolder = biomeRegistry.wrapAsHolder(biome);
			if (biomeSet.contains(biomeHolder)) {
				dimensions.add(level.dimension().identifier());
			}
		}
		return dimensions;
	}

	public static ListMultimap<Identifier, Identifier> getGeneratingDimensionsForAllowedBiomes(ServerLevel serverLevel) {
		ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (Identifier biomeID : getAllowedBiomeIDs(serverLevel)) {
			Optional<Biome> optionalBiome = getBiomeForIdentifier(serverLevel, biomeID);
			if (optionalBiome.isPresent()) {
				dimensionsForAllowedStructures.putAll(biomeID, getGeneratingDimensionIDs(serverLevel, optionalBiome.get()));
			}
		}
		return dimensionsForAllowedStructures;
	}

	public static int getBiomeSize(Level level) {
		// TODO
		return 4;
	}

	public static int getDistanceToBiome(Player player, int biomeX, int biomeZ) {
		return getDistanceToBiome(player.blockPosition(), biomeX, biomeZ);
	}

	public static int getDistanceToBiome(BlockPos startPos, int biomeX, int biomeZ) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(biomeX, startPos.getY(), biomeZ)));
	}
	
	@Environment(EnvType.CLIENT)
	public static String getBiomeTags(Level level, Biome biome) {
		// Some overworld biomes have the is_overworld tag and some don't, so ignore it altogether for clarity
		List<String> tagPathsToIgnore = List.of("is_overworld");
		// This will ignore duplicates and keep things sorted alphabetically
		Set<String> biomeCategories = new TreeSet<String>();
		Registry<Biome> biomeRegistry = getBiomeRegistry(level);
		if (biomeRegistry.wrapAsHolder(biome) != null) {
			Holder<Biome> biomeEntry = biomeRegistry.wrapAsHolder(biome);
			// Extremely hacky way of extracting a biome's categories from its tags
			List<TagKey<Biome>> categoryTags = biomeEntry.tags().filter(tag -> tag.location().getPath().startsWith("is_")).collect(Collectors.toList());
			for (TagKey<Biome> tag : categoryTags) {
				if (tagPathsToIgnore.contains(tag.location().getPath())) {
					continue;
				}
				String fixedPath = tag.location().getPath().replaceFirst("is_", "");
				if (fixedPath.contains("/")) {
					fixedPath = fixedPath.substring(0, fixedPath.indexOf("/"));
				}
				String biomeKey = Util.makeDescriptionId("biome", Identifier.fromNamespaceAndPath(tag.location().getNamespace(), fixedPath));
				String translatedBiomeKey = I18n.get(biomeKey);
				if (!biomeKey.equals(translatedBiomeKey)) {
					return translatedBiomeKey;
				}
				String categoryKey = Util.makeDescriptionId("category", Identifier.fromNamespaceAndPath(tag.location().getNamespace(), fixedPath));
				String translatedCategoryKey = I18n.get(categoryKey);
				if (!categoryKey.equals(translatedCategoryKey)) {
					return translatedCategoryKey;
				}
				biomeCategories.add(WordUtils.capitalize(fixedPath.replace('_', ' ')));
			}
		}
		if (biomeCategories.isEmpty()) {
			biomeCategories.add(I18n.get("string.naturescompass.none"));
		}
		return String.join(", ", biomeCategories);
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeNameForDisplay(Level level, Biome biome) {
		if (biome != null) {
			if (NaturesCompassConfig.fixBiomeNames) {
				final String original = getBiomeName(level, biome);
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

			if (getIdentifierForBiome(level, biome) != null) {
				return I18n.get(getIdentifierForBiome(level, biome).toString());
			}
		}

		return "";
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeName(Level level, Biome biome) {
		return I18n.get(Util.makeDescriptionId("biome", getIdentifierForBiome(level, biome)));
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeName(Level level, Identifier biomeID) {
		if (getBiomeForIdentifier(level, biomeID).isPresent()) {
			return getBiomeName(level, getBiomeForIdentifier(level, biomeID).get());
		}
		return "";
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeSource(Level level, Biome biome) {
		String registryEntry = getIdentifierForBiome(level, biome).toString();
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
	
	@Environment(EnvType.CLIENT)
	private static String getDimensionName(Identifier dimensionID) {
		String name = I18n.get(Util.makeDescriptionId("dimension", dimensionID));
		if (name.equals(Util.makeDescriptionId("dimension", dimensionID))) {
			name = dimensionID.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@Environment(EnvType.CLIENT)
	public static String dimensionKeysToString(List<Identifier> dimensions) {
		Set<String> dimensionNames = new HashSet<String>();
		dimensions.forEach((key) -> dimensionNames.add(getDimensionName(key)));
		return String.join(", ", dimensionNames);
	}

	public static boolean biomeIDIsBlacklisted(Level level, Identifier biomeID) {
		final List<String> biomeBlacklist = NaturesCompassConfig.biomeBlacklist;
		for (String biomeKey : biomeBlacklist) {
 			if (biomeID.toString().matches(convertToRegex(biomeKey))) {
 				return true;
 			}
 		}
 		return false;
	}
	
	public static boolean biomeIDIsHidden(Level level, Identifier biomeID) {
		final Registry<Biome> biomeRegistry = getBiomeRegistry(level);
		final Biome biome = getBiomeForIdentifier(level, biomeID).get();
		if (biomeRegistry.wrapAsHolder(biome) != null) {
			return biomeRegistry.wrapAsHolder(biome).tags().anyMatch(tag -> tag.location().getPath().equals("c:hidden_from_locator_selection"));
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