package com.chaosthedude.naturescompass.sorting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class RainfallSorting implements ISorting<Float> {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return getValue(biome1).compareTo(getValue(biome2));
	}

	@Override
	public Float getValue(Biome biome) {
		return biome.getDownfall();
	}

	@Override
	public ISorting<?> next() {
		return new TemperatureSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.rainfall");
	}

}