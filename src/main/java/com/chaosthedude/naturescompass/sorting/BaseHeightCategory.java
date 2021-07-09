package com.chaosthedude.naturescompass.sorting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class BaseHeightCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getDepth(), biome2.getDepth());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getDepth();
	}

	@Override
	public ISortingCategory next() {
		return new HeightVariationCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.baseHeight");
	}

}
