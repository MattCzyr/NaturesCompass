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
	
	public static Optional<? extends Registry<Biome>> getBiomeRegistry(Level level) {
		return level.registryAccess().lookup(Registries.BIOME);
	}

	public static Optional<Identifier> getIdForBiome(Level level, Biome biome) {
        return getBiomeRegistry(level).isPresent() && getBiomeRegistry(level).get().getResourceKey(biome).isPresent() ? Optional.of(getBiomeRegistry(level).get().getResourceKey(biome).get().identifier()) : Optional.empty();
    }

	public static Optional<Biome> getBiomeForId(Level level, Identifier key) {
		return getBiomeRegistry(level).isPresent() ? getBiomeRegistry(level).get().getOptional(key) : Optional.empty();
	}

	public static List<Identifier> getAllowedBiomes(Level level) {
		final List<Identifier> biomeIDs = new ArrayList<Identifier>();
		if (getBiomeRegistry(level).isPresent()) {
			for (Map.Entry<ResourceKey<Biome>, Biome> entry : getBiomeRegistry(level).get().entrySet()) {
                Identifier biomeID = entry.getKey().identifier();
				Biome biome = entry.getValue();
				if (biomeID != null && biome != null && !biomeIsBlacklisted(level, biomeID) && !biomeIsHidden(level, biomeID)) {
					biomeIDs.add(biomeID);
				}
			}
		}

		return biomeIDs;
	}
	
	public static int getXpLevelsForBiome(Identifier biomeId) {
		int xpLevels = NaturesCompassConfig.defaultXpLevels;
        for (String biomeRegex : NaturesCompassConfig.perBiomeXpLevels.keySet()) {
            if (biomeId.toString().matches(convertToRegex(biomeRegex))) {
                xpLevels = NaturesCompassConfig.perBiomeXpLevels.get(biomeRegex);
                if (xpLevels > 3) {
                    xpLevels = 3;
                }
                break;
            }
        }
		return xpLevels;
	}
	
	public static Map<Identifier, Integer> getXpLevelsForAllowedBiomes(List<Identifier> allowedBiomes) {
		final Map<Identifier, Integer> xpLevels = new HashMap<Identifier, Integer>();
        for (Identifier biomeId : allowedBiomes) {
            int levels = getXpLevelsForBiome(biomeId);
            xpLevels.put(biomeId, levels);
        }
		
		return xpLevels;
	}
	
	public static List<Identifier> getGeneratingDimensions(ServerLevel serverLevel, Biome biome) {
		final List<Identifier> dimensions = new ArrayList<Identifier>();
        if (getBiomeRegistry(serverLevel).isPresent()) {
            final Registry<Biome> biomeRegistry = getBiomeRegistry(serverLevel).get();
            for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
                Set<Holder<Biome>> biomeSet = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
                Holder<Biome> biomeHolder = biomeRegistry.wrapAsHolder(biome);
                if (biomeSet.contains(biomeHolder)) {
                    dimensions.add(level.dimension().identifier());
                }
            }
        }
		return dimensions;
	}

	public static ListMultimap<Identifier, Identifier> getGeneratingDimensionsForAllowedBiomes(ServerLevel serverLevel, List<Identifier> allowedBiomes) {
		ListMultimap<Identifier, Identifier> dimensionsForAllowedBiomes = ArrayListMultimap.create();
		for (Identifier biomeID : allowedBiomes) {
			Optional<Biome> optionalBiome = getBiomeForId(serverLevel, biomeID);
			if (optionalBiome.isPresent()) {
				dimensionsForAllowedBiomes.putAll(biomeID, getGeneratingDimensions(serverLevel, optionalBiome.get()));
			}
		}
		return dimensionsForAllowedBiomes;
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
	public static String getBiomeTags(Level level, Identifier biomeId) {
		if (getBiomeForId(level, biomeId).isPresent()) {
			return getBiomeTags(level, getBiomeForId(level, biomeId).get());
		}
		return I18n.get("string.naturescompass.none");
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeTags(Level level, Biome biome) {
		// Some overworld biomes have the is_overworld tag and some don't, so ignore it altogether for clarity
		List<String> tagPathsToIgnore = List.of("is_overworld");
		// This will ignore duplicates and keep things sorted alphabetically
		Set<String> biomeCategories = new TreeSet<String>();
        if (getBiomeRegistry(level).isPresent()) {
		    Registry<Biome> biomeRegistry = getBiomeRegistry(level).get();
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
				String biomeId = Util.makeDescriptionId("biome", Identifier.fromNamespaceAndPath(tag.location().getNamespace(), fixedPath));
				String translatedBiomeId = I18n.get(biomeId);
				if (!biomeId.equals(translatedBiomeId)) {
					return translatedBiomeId;
				}
				String categoryId = Util.makeDescriptionId("category", Identifier.fromNamespaceAndPath(tag.location().getNamespace(), fixedPath));
				String translatedCategoryId = I18n.get(categoryId);
				if (!categoryId.equals(translatedCategoryId)) {
					return translatedCategoryId;
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
    public static String getBiomeNameForDisplay(Level level, Identifier biomeId) {
        if (NaturesCompassConfig.fixBiomeNames) {
            final String original = getBiomeName(level, biomeId);
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
        return I18n.get(biomeId.toString());
    }

	@Environment(EnvType.CLIENT)
	public static String getBiomeName(Level level, Identifier biomeID) {
        return I18n.get(Util.makeDescriptionId("biome", biomeID));
	}

	@Environment(EnvType.CLIENT)
	public static String getBiomeSource(Level level, Identifier biomeId) {
		String registryEntry = biomeId.toString();
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
	public static String dimensionIdsToString(List<Identifier> dimensions) {
		Set<String> dimensionNames = new HashSet<String>();
		dimensions.forEach((id) -> dimensionNames.add(getDimensionName(id)));
		return String.join(", ", dimensionNames);
	}

	public static boolean biomeIsBlacklisted(Level level, Identifier biomeId) {
		final List<String> biomeBlacklist = NaturesCompassConfig.biomeBlacklist;
		for (String biomeRegex : biomeBlacklist) {
 			if (biomeId.toString().matches(convertToRegex(biomeRegex))) {
 				return true;
 			}
 		}
 		return false;
	}
	
	public static boolean biomeIsHidden(Level level, Identifier biomeId) {
		if (getBiomeRegistry(level).isPresent() && getBiomeForId(level, biomeId).isPresent()) {
			final Registry<Biome> biomeRegistry = getBiomeRegistry(level).get();
			final Biome biome = getBiomeForId(level, biomeId).get();
            return biomeRegistry.wrapAsHolder(biome).tags().anyMatch(tag -> tag.location().toString().equals("c:hidden_from_locator_selection"));
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