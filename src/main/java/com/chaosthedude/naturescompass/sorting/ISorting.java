package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public interface ISorting<T> extends Comparator<Biome> {

	@Override
	public int compare(Biome biome1, Biome biome2);

	public T getValue(Biome biome);

	public ISorting<?> next();

	public String getLocalizedName();

}