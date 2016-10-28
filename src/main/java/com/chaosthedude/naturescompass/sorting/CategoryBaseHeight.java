package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryBaseHeight implements ISortingCategory {

	@Override
	public int compare(Object biome1, Object biome2) {
		return Float.compare(((Biome) biome1).getBaseHeight(), ((Biome) biome2).getBaseHeight());
	}
	
	@Override
	public Object getValue(Biome biome) {
		return biome.getBaseHeight();
	}

	@Override
	public ISortingCategory next() {
		return new CategoryHeightVariation();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.baseHeight");
	}

}
