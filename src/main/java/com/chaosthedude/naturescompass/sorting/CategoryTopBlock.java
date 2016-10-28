package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class CategoryTopBlock implements ISortingCategory {

	@Override
	public int compare(Object biome1, Object biome2) {
		return ((Biome) biome1).topBlock.getBlock().getLocalizedName().compareTo(((Biome) biome2).topBlock.getBlock().getLocalizedName());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.topBlock.getBlock().getLocalizedName();
	}

	@Override
	public ISortingCategory next() {
		return new CategoryFillerBlock();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.topBlock");
	}

}
