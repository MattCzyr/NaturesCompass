package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TemperatureCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getBaseTemperature(), biome2.getBaseTemperature());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getBaseTemperature();
	}

	@Override
	public ISortingCategory next() {
		return new RainfallCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.temperature");
	}

}
