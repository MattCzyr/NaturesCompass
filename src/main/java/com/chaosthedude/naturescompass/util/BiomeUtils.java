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

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeUtils {

	public static Optional<? extends Registry<Biome>> getBiomeRegistry(Level level) {
		return level.registryAccess().registry(ForgeRegistries.Keys.BIOMES);
	}

	public static Optional<ResourceLocation> getKeyForBiome(Level level, Biome biome) {
		if (!getBiomeRegistry(level).isPresent()) {
			return Optional.empty();
		}
		return getBiomeRegistry(level).get().getResourceKey(biome).map(ResourceKey::location);
	}

	public static Optional<Biome> getBiomeForKey(Level level, ResourceLocation key) {
		return getBiomeRegistry(level).isPresent() ? getBiomeRegistry(level).get().getOptional(key) : Optional.empty();
	}

	public static List<ResourceLocation> getAllowedBiomeKeys(Level level) {
		final List<ResourceLocation> biomeKeys = new ArrayList<ResourceLocation>();
		if (getBiomeRegistry(level).isPresent()) {
			for (Map.Entry<ResourceKey<Biome>, Biome> entry : getBiomeRegistry(level).get().entrySet()) {
				ResourceLocation biomeKey = entry.getKey().location();
				Biome biome = entry.getValue();
				if (biomeKey != null && biome != null && !biomeKeyIsBlacklisted(level, biomeKey)) {
					biomeKeys.add(biomeKey);
				}
			}
		}
		return biomeKeys;
	}

	public static int getXpLevelsForBiome(ResourceLocation biomeKey) {
		int xpLevels = ConfigHandler.GENERAL.defaultXpLevels.get();
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
		return xpLevels;
	}

	public static Map<String, Integer> parseXpLevelOverridesConfig() {
		final List<String> xpLevelOverrides = ConfigHandler.GENERAL.perBiomeXpLevels.get();
		Map<String, Integer> parsedOverrides = new HashMap<String, Integer>();
		for (String override : xpLevelOverrides) {
			String[] split = override.split(",");
			if (split.length != 2) {
				continue;
			}
			String biomeRegex = split[0];
			String xpLevelsStr = split[1];
			try {
				int xpLevels = Integer.valueOf(xpLevelsStr);
				parsedOverrides.put(biomeRegex, xpLevels);
			} catch (NumberFormatException e) {
				continue;
			}
		}
		return parsedOverrides;
	}

	public static Map<ResourceLocation, Integer> getXpLevelsForAllowedBiomes(List<ResourceLocation> allowedBiomes) {
		final Map<ResourceLocation, Integer> xpLevels = new HashMap<ResourceLocation, Integer>();
		for (ResourceLocation biomeKey : allowedBiomes) {
			xpLevels.put(biomeKey, getXpLevelsForBiome(biomeKey));
		}
		return xpLevels;
	}

	public static List<ResourceLocation> getGeneratingDimensionKeys(ServerLevel serverLevel, ResourceLocation biomeKey) {
		final List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
		if (getBiomeRegistry(serverLevel).isPresent()) {
			final Registry<Biome> biomeRegistry = getBiomeRegistry(serverLevel).get();
			Optional<Biome> optionalBiome = getBiomeForKey(serverLevel, biomeKey);
			if (optionalBiome.isPresent()) {
				for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
					Set<Holder<Biome>> biomeSet = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
					Holder<Biome> biomeHolder = biomeRegistry.wrapAsHolder(optionalBiome.get());
					if (biomeSet.contains(biomeHolder)) {
						dimensions.add(level.dimension().location());
					}
				}
			}
		}
		return dimensions;
	}

	public static ListMultimap<ResourceLocation, ResourceLocation> getGeneratingDimensionsForAllowedBiomes(ServerLevel serverLevel) {
		ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedBiomes = ArrayListMultimap.create();
		for (ResourceLocation biomeKey : getAllowedBiomeKeys(serverLevel)) {
			dimensionsForAllowedBiomes.putAll(biomeKey, getGeneratingDimensionKeys(serverLevel, biomeKey));
		}
		return dimensionsForAllowedBiomes;
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

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeTags(Level level, ResourceLocation biomeKey) {
		// Some overworld biomes have the is_overworld tag and some don't, so ignore it
		// altogether for clarity
		List<String> tagPathsToIgnore = List.of("is_overworld");
		// This will ignore duplicates and keep things sorted alphabetically
		Set<String> biomeCategories = new TreeSet<String>();
		if (getBiomeRegistry(level).isPresent()) {
			Registry<Biome> biomeRegistry = getBiomeRegistry(level).get();
			Optional<Biome> optionalBiome = getBiomeForKey(level, biomeKey);
			if (optionalBiome.isPresent()) {
				Holder<Biome> biomeHolder = biomeRegistry.wrapAsHolder(optionalBiome.get());
				List<TagKey<Biome>> categoryTags = biomeHolder.getTagKeys().filter(tag -> tag.location().getPath().startsWith("is_")).collect(Collectors.toList());
				for (TagKey<Biome> tag : categoryTags) {
					if (tagPathsToIgnore.contains(tag.location().getPath())) {
						continue;
					}
					String fixedPath = tag.location().getPath().replaceFirst("is_", "");
					if (fixedPath.contains("/")) {
						fixedPath = fixedPath.substring(0, fixedPath.indexOf("/"));
					}
					String biomeTranslationKey = Util.makeDescriptionId("biome", new ResourceLocation(tag.location().getNamespace(), fixedPath));
					String translatedBiome = I18n.get(biomeTranslationKey);
					if (!biomeTranslationKey.equals(translatedBiome)) {
						return translatedBiome;
					}
					String categoryTranslationKey = Util.makeDescriptionId("category", new ResourceLocation(tag.location().getNamespace(), fixedPath));
					String translatedCategory = I18n.get(categoryTranslationKey);
					if (!categoryTranslationKey.equals(translatedCategory)) {
						return translatedCategory;
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

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeNameForDisplay(Level level, ResourceLocation biomeKey) {
		if (ConfigHandler.CLIENT.fixBiomeNames.get()) {
			final String original = getBiomeName(level, biomeKey);
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
		return getBiomeName(level, biomeKey);
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeName(Level level, ResourceLocation biomeKey) {
		return I18n.get(Util.makeDescriptionId("biome", biomeKey));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeSource(Level level, ResourceLocation biomeKey) {
		String modid = biomeKey.getNamespace();
		if (modid.equals("minecraft")) {
			return "Minecraft";
		}
		Optional<? extends ModContainer> sourceContainer = ModList.get().getModContainerById(modid);
		if (sourceContainer.isPresent()) {
			return sourceContainer.get().getModInfo().getDisplayName();
		}
		return modid;
	}

	@OnlyIn(Dist.CLIENT)
	private static String getDimensionName(ResourceLocation dimensionKey) {
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

	@OnlyIn(Dist.CLIENT)
	public static String dimensionKeysToString(List<ResourceLocation> dimensions) {
		Set<String> dimensionNames = new HashSet<String>();
		dimensions.forEach((key) -> dimensionNames.add(getDimensionName(key)));
		return String.join(", ", dimensionNames);
	}

	public static boolean biomeKeyIsBlacklisted(Level level, ResourceLocation biomeKey) {
		final List<String> biomeBlacklist = ConfigHandler.GENERAL.biomeBlacklist.get();
		for (String key : biomeBlacklist) {
			if (biomeKey.toString().matches(convertToRegex(key))) {
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
