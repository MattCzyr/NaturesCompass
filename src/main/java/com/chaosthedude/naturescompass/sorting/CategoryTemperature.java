package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.BiomeGenBase;

public class CategoryTemperature implements ISortingCategory {

	@Override
	public int compare(Object biome1, Object biome2) {
		return Float.compare(((BiomeGenBase) biome1).temperature, ((BiomeGenBase) biome2).temperature);
	}

	@Override
	public Object getValue(BiomeGenBase biome) {
		return biome.temperature;
	}

	@Override
	public ISortingCategory next() {
		return new CategoryRainfall();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.temperature");
	}

}
