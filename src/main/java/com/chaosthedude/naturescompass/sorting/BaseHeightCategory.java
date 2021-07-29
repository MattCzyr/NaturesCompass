package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
		return I18n.get("string.naturescompass.baseHeight");
	}

}
