package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FillerBlockCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return I18n.format(biome1.getGenerationSettings().getSurfaceBuilderConfig().getUnder().getBlock().getTranslationKey()).compareTo(I18n.format(biome2.getGenerationSettings().getSurfaceBuilderConfig().getUnder().getBlock().getTranslationKey()));
	}

	@Override
	public Object getValue(Biome biome) {
		return I18n.format(biome.getGenerationSettings().getSurfaceBuilderConfig().getUnder().getBlock().getTranslationKey());
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
