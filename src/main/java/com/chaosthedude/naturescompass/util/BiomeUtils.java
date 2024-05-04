package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class BiomeUtils {

	public static Optional<? extends Registry<Biome>> getBiomeRegistry(Level level) {
		return level.registryAccess().registry(Registries.BIOME);
	}

	public static Optional<ResourceLocation> getKeyForBiome(Level level, Biome biome) {
		return getBiomeRegistry(level).isPresent() ? Optional.of(getBiomeRegistry(level).get().getKey(biome)) : Optional.empty();
	}

	public static Optional<Biome> getBiomeForKey(Level level, ResourceLocation key) {
		return getBiomeRegistry(level).isPresent() ? getBiomeRegistry(level).get().getOptional(key) : Optional.empty();
	}

	public static List<ResourceLocation> getAllowedBiomeKeys(Level level) {
		final List<ResourceLocation> biomeKeys = new ArrayList<ResourceLocation>();
		if (getBiomeRegistry(level).isPresent()) {
			for (Map.Entry<ResourceKey<Biome>, Biome> entry : getBiomeRegistry(level).get().entrySet()) {
				Biome biome = entry.getValue();
				if (biome != null) {
					Optional<ResourceLocation> optionalBiomeKey = getKeyForBiome(level, biome);
					if (biome != null && optionalBiomeKey.isPresent() && !biomeKeyIsBlacklisted(level, optionalBiomeKey.get())) {
						biomeKeys.add(optionalBiomeKey.get());
					}
				}
			}
		}

		return biomeKeys;
	}

	public static List<ResourceLocation> getGeneratingDimensionKeys(ServerLevel serverLevel, Biome biome) {
		final List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
		final Registry<Biome> biomeRegistry = getBiomeRegistry(serverLevel).get();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			Set<Holder<Biome>> biomeSet = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
			Holder<Biome> biomeHolder = biomeRegistry.getHolder(biomeRegistry.getResourceKey(biome).get()).get();
			if (biomeSet.contains(biomeHolder)) {
				dimensions.add(level.dimension().location());
			}
		}
		return dimensions;
	}
	
	public static ListMultimap<ResourceLocation, ResourceLocation> getGeneratingDimensionsForAllowedBiomes(ServerLevel serverLevel) {
		ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (ResourceLocation biomeKey : getAllowedBiomeKeys(serverLevel)) {
			Optional<Biome> optionalBiome = getBiomeForKey(serverLevel, biomeKey);
			if (optionalBiome.isPresent()) {
				dimensionsForAllowedStructures.putAll(biomeKey, getGeneratingDimensionKeys(serverLevel, optionalBiome.get()));
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

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeTags(Level level, Biome biome) {
		// Some overworld biomes have the is_overworld tag and some don't, so ignore it
		// altogether for clarity
		List<String> tagPathsToIgnore = List.of("is_overworld");
		// This will ignore duplicates and keep things sorted alphabetically
		Set<String> biomeCategories = new TreeSet<String>();
		if (getBiomeRegistry(level).isPresent()) {
			Registry<Biome> biomeRegistry = getBiomeRegistry(level).get();
			if (biomeRegistry.getResourceKey(biome).isPresent() && biomeRegistry.getHolder(biomeRegistry.getResourceKey(biome).get()).isPresent()) {
				Holder<Biome> biomeHolder = biomeRegistry.getHolder(biomeRegistry.getResourceKey(biome).get()).get();
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
					String biomeKey = Util.makeDescriptionId("biome", new ResourceLocation(tag.location().getNamespace(), fixedPath));
					String translatedBiomeKey = I18n.get(biomeKey);
					if (!biomeKey.equals(translatedBiomeKey)) {
						return translatedBiomeKey;
					}
					String categoryKey = Util.makeDescriptionId("category", new ResourceLocation(tag.location().getNamespace(), fixedPath));
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

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeNameForDisplay(Level level, ResourceLocation biome) {
		if (getBiomeForKey(level, biome).isPresent()) {
			return getBiomeNameForDisplay(level, getBiomeForKey(level, biome).get());
		}
		return "";
	}

	@OnlyIn(Dist.CLIENT)
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
			if (getKeyForBiome(level, biome) != null) {
				return I18n.get(getKeyForBiome(level, biome).toString());
			}
		}
		return "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeName(Level level, Biome biome) {
		return getKeyForBiome(level, biome).isPresent() ? I18n.get(Util.makeDescriptionId("biome", getKeyForBiome(level, biome).get())) : "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeName(Level level, ResourceLocation key) {
		if (getBiomeForKey(level, key).isPresent()) {
			return getBiomeName(level, getBiomeForKey(level, key).get());
		}
		return "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeSource(Level level, Biome biome) {
		if (getKeyForBiome(level, biome).isEmpty()) {
			return "";
		}
		String modid = getKeyForBiome(level, biome).get().getNamespace();
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
