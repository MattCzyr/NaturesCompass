package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class NameCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return BiomeUtils.getBiomeName(biome1).compareTo(BiomeUtils.getBiomeName(biome2));
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
		return I18n.format("string.naturescompass.name");
	}

}
