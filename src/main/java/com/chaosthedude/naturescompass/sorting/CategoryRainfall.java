package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.BiomeGenBase;

public class CategoryRainfall implements ISortingCategory {

	@Override
	public int compare(Object biome1, Object biome2) {
		return Float.compare(((BiomeGenBase) biome1).rainfall, ((BiomeGenBase) biome2).rainfall);
	}

	@Override
	public Object getValue(BiomeGenBase biome) {
		return biome.rainfall;
	}

	@Override
	public ISortingCategory next() {
		return new CategoryName();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.rainfall");
	}

}
