package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryName implements ISortingCategory {

	@Override
	public int compare(Object biome1, Object biome2) {
		return BiomeUtils.getBiomeName((Biome) biome1).compareTo(BiomeUtils.getBiomeName((Biome) biome2));
	}

	@Override
	public Object getValue(Biome biome) {
		return null;
	}

	@Override
	public ISortingCategory next() {
		return new CategoryBaseHeight();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.name");
	}

}
