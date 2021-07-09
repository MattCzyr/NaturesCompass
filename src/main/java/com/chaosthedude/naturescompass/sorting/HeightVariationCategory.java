package com.chaosthedude.naturescompass.sorting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class HeightVariationCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getScale(), biome2.getScale());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getScale();
	}

	@Override
	public ISortingCategory next() {
		return new TemperatureCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.heightVariation");
	}

}
