package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryRainfall implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getDownfall(), biome2.getDownfall());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getDownfall();
	}

	@Override
	public ISortingCategory next() {
		return new CategoryTopBlock();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.rainfall");
	}

}
