package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class NameCategory implements ISortingCategory {
	
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	@Override
	public int compare(Biome biome1, Biome biome2) {
		if (CLIENT.world != null) {
			return BiomeUtils.getBiomeName(CLIENT.world, biome1).compareTo(BiomeUtils.getBiomeName(CLIENT.world, biome2));
		}
		return 0;
	}

	@Override
	public Object getValue(Biome biome) {
		return null;
	}

	@Override
	public ISortingCategory next() {
		return new SourceCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.name");
	}

}