package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public class TemperatureSorting implements ISorting<Float> {
	
	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(Identifier biomeId1, Identifier biomeId2) {
		return getValue(biomeId1).compareTo(getValue(biomeId2));
	}

	@Override
	public Float getValue(Identifier biomeId) {
		if (mc.level != null && BiomeUtils.getBiomeForId(mc.level, biomeId).isPresent()) {
			Biome biome = BiomeUtils.getBiomeForId(mc.level, biomeId).get();
			return biome.getBaseTemperature();
		}
		return 0F;
	}

	@Override
	public ISorting<?> next() {
		return new NameSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.temperature");
	}

}
