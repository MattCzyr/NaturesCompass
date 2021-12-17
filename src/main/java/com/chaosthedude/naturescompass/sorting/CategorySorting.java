package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;

public class CategorySorting implements ISorting<String> {
	
	private static final MinecraftClient client = MinecraftClient.getInstance();
	
	@Override
	public int compare(Biome biome1, Biome biome2) {
		return getValue(biome1).compareTo(getValue(biome2));
	}

	@Override
	public String getValue(Biome biome) {
		if (client.world != null) {
			return BiomeUtils.getBiomeCategoryName(client.world, biome);
		}
		return "";
	}

	@Override
	public ISorting<?> next() {
		return new RainfallSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.category");
	}

}