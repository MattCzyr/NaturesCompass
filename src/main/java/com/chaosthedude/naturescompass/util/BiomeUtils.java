package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

	public static void searchForBiome(Level level, Player player, ItemStack stack, Biome biome, BlockPos startPos) {
		BiomeSearchWorker worker = new BiomeSearchWorker(level, player, stack, biome, startPos);
		worker.start();
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
		if (getKeyForBiome(level, biome) == null) {
			return "";
		}
		String registryEntry = getKeyForBiome(level, biome).toString();
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
