package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryTemperature implements ISortingCategory {

	@Override
	public int compare(Object biome1, Object biome2) {
		return Float.compare(((Biome) biome1).getTemperature(), ((Biome) biome2).getTemperature());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getTemperature();
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
