package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RainfallSorting implements ISorting<Float> {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return getValue(biome1).compareTo(getValue(biome2));
	}

	@Override
	public Float getValue(Biome biome) {
		return biome.getModifiedClimateSettings().downfall();
	}

	@Override
	public ISorting<?> next() {
		return new TemperatureSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.rainfall");
	}

}
