package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeUtils {
	
	public static MutableRegistry<Biome> getBiomeRegistry(World world) {
		return world.func_241828_r().getRegistry(ForgeRegistries.Keys.BIOMES);
	}

	public static ResourceLocation getKeyForBiome(World world, Biome biome) {
		return getBiomeRegistry(world).getKey(biome);
	}

	public static Optional<Biome> getBiomeForKey(World world, ResourceLocation key) {
		return getBiomeRegistry(world).getOptional(key);
	}

	public static List<Biome> getAllowedBiomes(World world) {
		final List<Biome> biomes = new ArrayList<Biome>();
		for (Map.Entry<RegistryKey<Biome>, Biome> entry : getBiomeRegistry(world).getEntries()) {
			Biome biome = entry.getValue();
			if (biome != null && getKeyForBiome(world, biome) != null && !biomeIsBlacklisted(world, biome)) {
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
	public static String getBiomeNameForDisplay(World world, ResourceLocation biome) {
		if (getBiomeForKey(world, biome).isPresent()) {
			return getBiomeNameForDisplay(world, getBiomeForKey(world, biome).get());
		}
		return "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeNameForDisplay(World world, Biome biome) {
		if (biome != null) {
			if (ConfigHandler.CLIENT.fixBiomeNames.get()) {
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
			if (getKeyForBiome(world, biome) != null) {
				return I18n.format(getKeyForBiome(world, biome).toString());
			}
		}

		return "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeName(World world, Biome biome) {
		return I18n.format(Util.makeTranslationKey("biome", getKeyForBiome(world, biome)));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeName(World world, ResourceLocation key) {
		if (getBiomeForKey(world, key).isPresent()) {
			return getBiomeName(world, getBiomeForKey(world, key).get());
		}
		return "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getBiomeSource(World world, Biome biome) {
		if (getKeyForBiome(world, biome) == null) {
			return "";
		}
		String registryEntry = getKeyForBiome(world, biome).toString();
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

	public static boolean biomeIsBlacklisted(World world, Biome biome) {
		final List<String> biomeBlacklist = ConfigHandler.GENERAL.biomeBlacklist.get();
		return biomeBlacklist.contains(getKeyForBiome(world, biome).toString());
	}

}
