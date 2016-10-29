package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.minecraft.world.biome.BiomeGenBase;

public interface ISortingCategory extends Comparator {

	@Override
	public int compare(Object biome1, Object biome2);

	public Object getValue(BiomeGenBase biome);

	public ISortingCategory next();

	public String getLocalizedName();

}
