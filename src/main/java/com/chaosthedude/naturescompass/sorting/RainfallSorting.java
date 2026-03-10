package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

@Environment(EnvType.CLIENT)
public class RainfallSorting implements ISorting<Float> {

	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(Identifier biomeId1, Identifier biomeId2) {
		return getValue(biomeId1).compareTo(getValue(biomeId2));
	}

	@Override
	public Float getValue(Identifier biomeId) {
		if (mc.level != null && BiomeUtils.getBiomeForId(mc.level, biomeId).isPresent()) {
			Biome biome = BiomeUtils.getBiomeForId(mc.level, biomeId).get();
			return biome.climateSettings.downfall();
		}
		return 0F;
	}

	@Override
	public ISorting<?> next() {
		return new TemperatureSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.rainfall");
	}

}
