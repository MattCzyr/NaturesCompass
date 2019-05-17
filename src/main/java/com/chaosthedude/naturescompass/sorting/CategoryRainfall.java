package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryRainfall implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getRainfall(), biome2.getRainfall());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getRainfall();
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
