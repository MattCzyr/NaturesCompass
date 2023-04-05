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
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BiomeUtils {
	
	public static Registry<Biome> getBiomeRegistry(World world) {
		return world.getRegistryManager().get(RegistryKeys.BIOME);
	}

	public static Identifier getIdentifierForBiome(World world, Biome biome) {
		return getBiomeRegistry(world).getId(biome);
	}

	public static Optional<Biome> getBiomeForIdentifier(World world, Identifier id) {
		return getBiomeRegistry(world).getOrEmpty(id);
	}

	public static List<Identifier> getAllowedBiomeIDs(World world) {
		final List<Identifier> biomeIDs = new ArrayList<Identifier>();
		for (Map.Entry<RegistryKey<Biome>, Biome> entry : getBiomeRegistry(world).getEntrySet()) {
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
	
	public static List<Identifier> getGeneratingDimensionIDs(ServerWorld serverWorld, Biome biome) {
		final List<Identifier> dimensions = new ArrayList<Identifier>();
		final Registry<Biome> biomeRegistry = getBiomeRegistry(serverWorld);
		for (ServerWorld world : serverWorld.getServer().getWorlds()) {
			Set<RegistryEntry<Biome>> biomeSet = world.getChunkManager().getChunkGenerator().getBiomeSource().getBiomes();
			RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biomeRegistry.getKey(biome).get()).get();
			if (biomeSet.contains(biomeEntry)) {
				dimensions.add(world.getRegistryKey().getValue());
			}
		}
		return dimensions;
	}

	public static ListMultimap<Identifier, Identifier> getGeneratingDimensionsForAllowedBiomes(ServerWorld serverWorld) {
		ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (Identifier biomeID : getAllowedBiomeIDs(serverWorld)) {
			Optional<Biome> optionalBiome = getBiomeForIdentifier(serverWorld, biomeID);
			if (optionalBiome.isPresent()) {
				dimensionsForAllowedStructures.putAll(biomeID, getGeneratingDimensionIDs(serverWorld, optionalBiome.get()));
			}
		}
		return dimensionsForAllowedStructures;
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
	public static String getBiomeTags(World world, Biome biome) {
		// Some overworld biomes have the is_overworld tag and some don't, so ignore it altogether for clarity
		List<String> tagPathsToIgnore = List.of("is_overworld");
		// This will ignore duplicates and keep things sorted alphabetically
		Set<String> biomeCategories = new TreeSet<String>();
		Registry<Biome> biomeRegistry = getBiomeRegistry(world);
		if (biomeRegistry.getKey(biome).isPresent() && biomeRegistry.getEntry(biomeRegistry.getKey(biome).get()).isPresent()) {
			RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biomeRegistry.getKey(biome).get()).get();
			// Extremely hacky way of extracting a biome's categories from its tags
			List<TagKey<Biome>> categoryTags = biomeEntry.streamTags().filter(tag -> tag.id().getPath().startsWith("is_")).collect(Collectors.toList());
			for (TagKey<Biome> tag : categoryTags) {
				if (tagPathsToIgnore.contains(tag.id().getPath())) {
					continue;
				}
				String fixedPath = tag.id().getPath().replaceFirst("is_", "");
				if (fixedPath.contains("/")) {
					fixedPath = fixedPath.substring(0, fixedPath.indexOf("/"));
				}
				String biomeKey = Util.createTranslationKey("biome", new Identifier(tag.id().getNamespace(), fixedPath));
				String translatedBiomeKey = I18n.translate(biomeKey);
				if (!biomeKey.equals(translatedBiomeKey)) {
					return translatedBiomeKey;
				}
				String categoryKey = Util.createTranslationKey("category", new Identifier(tag.id().getNamespace(), fixedPath));
				String translatedCategoryKey = I18n.translate(categoryKey);
				if (!categoryKey.equals(translatedCategoryKey)) {
					return translatedCategoryKey;
				}
				biomeCategories.add(WordUtils.capitalize(fixedPath.replace('_', ' ')));
			}
		}
		if (biomeCategories.isEmpty()) {
			biomeCategories.add(I18n.translate("string.naturescompass.none"));
		}
		return String.join(", ", biomeCategories);
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
	
	@Environment(EnvType.CLIENT)
	private static String getDimensionName(Identifier dimensionID) {
		String name = I18n.translate(Util.createTranslationKey("dimension", dimensionID));
		if (name.equals(Util.createTranslationKey("dimension", dimensionID))) {
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