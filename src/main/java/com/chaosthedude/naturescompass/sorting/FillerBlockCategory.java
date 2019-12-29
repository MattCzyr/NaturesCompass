package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class FillerBlockCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return I18n.format(biome1.getSurfaceBuilderConfig().getUnder().getBlock().getTranslationKey()).compareTo(I18n.format(((Biome) biome2).getSurfaceBuilderConfig().getUnder().getBlock().getTranslationKey()));
	}

	@Override
	public Object getValue(Biome biome) {
		return I18n.format(biome.getSurfaceBuilderConfig().getUnder().getBlock().getTranslationKey());
	}

	@Override
	public ISortingCategory next() {
		return new NameCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.fillerBlock");
	}

}
