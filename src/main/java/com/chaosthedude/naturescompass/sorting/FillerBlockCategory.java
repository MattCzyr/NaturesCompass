package com.chaosthedude.naturescompass.sorting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class FillerBlockCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return I18n.translate(biome1.getGenerationSettings().getSurfaceConfig().getUnderMaterial().getBlock().getTranslationKey()).compareTo(I18n.translate(biome2.getGenerationSettings().getSurfaceConfig().getUnderMaterial().getBlock().getTranslationKey()));
	}

	@Override
	public Object getValue(Biome biome) {
		return I18n.translate(biome.getGenerationSettings().getSurfaceConfig().getUnderMaterial().getBlock().getTranslationKey());
	}

	@Override
	public ISortingCategory next() {
		return new NameCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.fillerBlock");
	}

}
