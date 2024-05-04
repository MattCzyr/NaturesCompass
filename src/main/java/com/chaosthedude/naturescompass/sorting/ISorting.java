package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISorting<T> extends Comparator<Biome> {

	@Override
	public int compare(Biome biome1, Biome biome2);

	public T getValue(Biome biome);

	public ISorting<?> next();

	public String getLocalizedName();

}
