package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;

public class TopBlockCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return (I18n.format(biome1.func_242440_e().func_242502_e().getTop().getBlock().getTranslationKey()).compareTo(I18n.format(biome2.func_242440_e().func_242502_e().getTop().getBlock().getTranslationKey())));
	}

	@Override
	public Object getValue(Biome biome) {
		return I18n.format(biome.func_242440_e().func_242502_e().getTop().getBlock().getTranslationKey());
	}

	@Override
	public ISortingCategory next() {
		return new FillerBlockCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.topBlock");
	}

}
