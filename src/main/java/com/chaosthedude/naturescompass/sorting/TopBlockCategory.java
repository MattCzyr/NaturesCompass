package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TopBlockCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return (I18n.get(biome1.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().getBlock().getDescriptionId()).compareTo(I18n.get(biome2.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().getBlock().getDescriptionId())));
	}

	@Override
	public Object getValue(Biome biome) {
		return I18n.get(biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().getBlock().getDescriptionId());
	}

	@Override
	public ISortingCategory next() {
		return new FillerBlockCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.topBlock");
	}

}
