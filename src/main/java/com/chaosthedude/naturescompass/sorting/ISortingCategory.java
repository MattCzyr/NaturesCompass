package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISortingCategory extends Comparator<Biome> {

	@Override
	public int compare(Biome biome1, Biome biome2);

	public Object getValue(Biome biome);

	public ISortingCategory next();

	public String getLocalizedName();

}
