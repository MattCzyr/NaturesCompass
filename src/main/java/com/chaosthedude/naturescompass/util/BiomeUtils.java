package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

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
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class BiomeUtils {

	public static Optional<? extends Registry<Biome>> getBiomeRegistry(Level level) {
		return level.registryAccess().lookup(Registries.BIOME);
	}

	public static Optional<Identifier> getIdForBiome(Level level, Biome biome) {
		return getBiomeRegistry(level).isPresent() ? Optional.of(getBiomeRegistry(level).get().getKey(biome)) : Optional.empty();
	}

	public static Optional<Biome> getBiomeForId(Level level, Identifier key) {
		return getBiomeRegistry(level).isPresent() ? getBiomeRegistry(level).get().getOptional(key) : Optional.empty();
	}

	public static List<Identifier> getAllowedBiomeIds(Level level) {
		final List<Identifier> biomeKeys = new ArrayList<Identifier>();
		if (getBiomeRegistry(level).isPresent()) {
			for (Map.Entry<ResourceKey<Biome>, Biome> entry : getBiomeRegistry(level).get().entrySet()) {
				Biome biome = entry.getValue();
				if (biome != null) {
					Optional<Identifier> optionalBiomeKey = getIdForBiome(level, biome);
					if (biome != null && optionalBiomeKey.isPresent() && !biomeIsBlacklisted(level, optionalBiomeKey.get()) && !biomeIsHidden(level, optionalBiomeKey.get())) {
						biomeKeys.add(optionalBiomeKey.get());
					}
				}
			}
		}

		return biomeKeys;
	}
	
	public static int getXpLevelsForBiome(ServerLevel serverLevel, Identifier biomeKey) {
		int xpLevels = ConfigHandler.GENERAL.defaultXpLevels.get();
		if (getBiomeRegistry(serverLevel).isPresent()) {
			final Map<String, Integer> xpLevelOverrides = parseXpLevelOverridesConfig();
			for (String biomeRegex : xpLevelOverrides.keySet()) {
				if (biomeKey.toString().matches(convertToRegex(biomeRegex))) {
					xpLevels = xpLevelOverrides.get(biomeRegex);
					if (xpLevels > 3) {
						xpLevels = 3;
					}
					break;
				}
			}
		}
		return xpLevels;
	}
	
	public static Map<String, Integer> parseXpLevelOverridesConfig() {
		final List<String> xpLevelOverrides = ConfigHandler.GENERAL.xpLevelOverrides.get();
		Map<String, Integer> parsedOverrides = new HashMap<String, Integer>();
		for (String override : xpLevelOverrides) {
			String[] split = override.split(",");
			if (split.length != 2) {
				// Invalid entry
				continue;
			}
			
			String biomeRegex = split[0];
			String xpLevelsStr = split[1];
			try {
		        int xpLevels = Integer.valueOf(xpLevelsStr);
		        parsedOverrides.put(biomeRegex, xpLevels);
		    } catch (NumberFormatException e) {
		        // Invalid entry
		        continue;
		    }
		}
		
		return parsedOverrides;
	}
	
	public static Map<Identifier, Integer> getXpLevelsForAllowedBiomes(ServerLevel serverLevel, List<Identifier> allowedBiomes) {
		final Map<Identifier, Integer> xpLevels = new HashMap<Identifier, Integer>();
		if (getBiomeRegistry(serverLevel).isPresent()) {
			for (Identifier biomeKey : allowedBiomes) {
				int levels = getXpLevelsForBiome(serverLevel, biomeKey);
				xpLevels.put(biomeKey, levels);
			}
		}
		
		return xpLevels;
	}

	public static List<Identifier> getGeneratingDimensions(ServerLevel serverLevel, Biome biome) {
		final List<Identifier> dimensions = new ArrayList<Identifier>();
		final Registry<Biome> biomeRegistry = getBiomeRegistry(serverLevel).get();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			Set<Holder<Biome>> biomeSet = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
			Holder<Biome> biomeHolder = biomeRegistry.wrapAsHolder(biome);
			if (biomeSet.contains(biomeHolder)) {
				dimensions.add(level.dimension().identifier());
			}
		}
		return dimensions;
	}
	
	public static ListMultimap<Identifier, Identifier> getGeneratingDimensionsForAllowedBiomes(ServerLevel serverLevel, List<Identifier> allowedBiomes) {
		ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (Identifier biomeKey : allowedBiomes) {
			Optional<Biome> optionalBiome = getBiomeForId(serverLevel, biomeKey);
			if (optionalBiome.isPresent()) {
				dimensionsForAllowedStructures.putAll(biomeKey, getGeneratingDimensions(serverLevel, optionalBiome.get()));
			}
		}
		return dimensionsForAllowedStructures;
	}

	public static int getBiomeSize(Level world) {
		// TODO
		return 4;
	}

	public static int getDistanceToBiome(Player player, int biomeX, int biomeZ) {
		return getDistanceToBiome(player.blockPosition(), biomeX, biomeZ);
	}

	public static int getDistanceToBiome(BlockPos startPos, int biomeX, int biomeZ) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(biomeX, startPos.getY(), biomeZ)));
	}

	public static String getBiomeTags(Level level, Biome biome) {
		// Some overworld biomes have the is_overworld tag and some don't, so ignore it
		// altogether for clarity
		List<String> tagPathsToIgnore = List.of("is_overworld");
		// This will ignore duplicates and keep things sorted alphabetically
		Set<String> biomeCategories = new TreeSet<String>();
		if (getBiomeRegistry(level).isPresent()) {
			Registry<Biome> biomeRegistry = getBiomeRegistry(level).get();
			if (biomeRegistry.wrapAsHolder(biome) != null) {
				Holder<Biome> biomeHolder = biomeRegistry.wrapAsHolder(biome);
				// Extremely hacky way of extracting a biome's categories from its tags
				List<TagKey<Biome>> categoryTags = biomeHolder.tags().filter(tag -> tag.location().getPath().startsWith("is_")).collect(Collectors.toList());
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
		}
		if (biomeCategories.isEmpty()) {
			biomeCategories.add(I18n.get("string.naturescompass.none"));
		}
		return String.join(", ", biomeCategories);
	}

	public static String getBiomeNameForDisplay(Level level, Identifier biome) {
		if (getBiomeForId(level, biome).isPresent()) {
			return getBiomeNameForDisplay(level, getBiomeForId(level, biome).get());
		}
		return "";
	}

	public static String getBiomeNameForDisplay(Level level, Biome biome) {
		if (biome != null) {
			if (ConfigHandler.CLIENT.fixBiomeNames.get()) {
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
			if (getIdForBiome(level, biome) != null) {
				return I18n.get(getIdForBiome(level, biome).toString());
			}
		}
		return "";
	}

	public static String getBiomeName(Level level, Biome biome) {
		return getIdForBiome(level, biome).isPresent() ? I18n.get(Util.makeDescriptionId("biome", getIdForBiome(level, biome).get())) : "";
	}

	public static String getBiomeName(Level level, Identifier key) {
		if (getBiomeForId(level, key).isPresent()) {
			return getBiomeName(level, getBiomeForId(level, key).get());
		}
		return "";
	}

	public static String getBiomeSource(Level level, Biome biome) {
		if (getIdForBiome(level, biome).isEmpty()) {
			return "";
		}
		String modid = getIdForBiome(level, biome).get().getNamespace();
		if (modid.equals("minecraft")) {
			return "Minecraft";
		}
		Optional<? extends ModContainer> sourceContainer = ModList.get().getModContainerById(modid);
		if (sourceContainer.isPresent()) {
			return sourceContainer.get().getModInfo().getDisplayName();
		}
		return modid;
	}
	
	private static String getDimensionName(Identifier dimensionKey) {
		String name = I18n.get(Util.makeDescriptionId("dimension", dimensionKey));
		if (name.equals(Util.makeDescriptionId("dimension", dimensionKey))) {
			name = dimensionKey.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}
	
	public static String dimensionIdsToString(List<Identifier> dimensions) {
		Set<String> dimensionNames = new HashSet<String>();
		dimensions.forEach((key) -> dimensionNames.add(getDimensionName(key)));
		return String.join(", ", dimensionNames);
	}

	public static boolean biomeIsBlacklisted(Level level, Identifier biomeKey) {
		final List<String> biomeBlacklist = ConfigHandler.GENERAL.biomeBlacklist.get();
		for (String key : biomeBlacklist) {
			if (biomeKey.toString().matches(convertToRegex(key))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean biomeIsHidden(Level level, Identifier biomeKey) {
		if (getBiomeRegistry(level).isPresent() && getBiomeForId(level, biomeKey).isPresent()) {
			final Registry<Biome> biomeRegistry = getBiomeRegistry(level).get();
			final Biome biome = getBiomeForId(level, biomeKey).get();
			if (biomeRegistry.wrapAsHolder(biome) != null) {
				return biomeRegistry.wrapAsHolder(biome).tags().anyMatch(tag -> tag.location().getPath().equals("c:hidden_from_locator_selection"));
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
