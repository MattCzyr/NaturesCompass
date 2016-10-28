package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryFillerBlock implements ISortingCategory {

	@Override
	public int compare(Object biome1, Object biome2) {
		return ((Biome) biome1).fillerBlock.getBlock().getLocalizedName().compareTo(((Biome) biome2).fillerBlock.getBlock().getLocalizedName());
	}
	
	@Override
	public Object getValue(Biome biome) {
		return biome.fillerBlock.getBlock().getLocalizedName();
	}

	@Override
	public ISortingCategory next() {
		return new CategoryName();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.fillerBlock");
	}

}
