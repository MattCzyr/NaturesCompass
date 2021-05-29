package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TemperatureCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getTemperature(), biome2.getTemperature());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getTemperature();
	}

	@Override
	public ISortingCategory next() {
		return new RainfallCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.temperature");
	}

}
