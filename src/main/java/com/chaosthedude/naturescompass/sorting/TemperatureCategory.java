package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class TemperatureCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.func_242445_k(), biome2.func_242445_k());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.func_242445_k();
	}

	@Override
	public ISortingCategory next() {
		return new RainfallCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.temperature");
	}

}
