package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryHeightVariation implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getHeightVariation(), biome2.getHeightVariation());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getHeightVariation();
	}

	@Override
	public ISortingCategory next() {
		return new CategoryTemperature();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.heightVariation");
	}

}
