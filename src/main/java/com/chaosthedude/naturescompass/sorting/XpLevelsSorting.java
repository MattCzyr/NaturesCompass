package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class XpLevelsSorting implements ISorting<String> {

	@Override
	public int compare(ResourceLocation biomeKey1, ResourceLocation biomeKey2) {
		return getValue(biomeKey1).compareTo(getValue(biomeKey2));
	}

	@Override
	public String getValue(ResourceLocation biomeKey) {
		if (NaturesCompass.xpLevelsForAllowedBiomes != null && NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeKey)) {
			return String.valueOf(NaturesCompass.xpLevelsForAllowedBiomes.get(biomeKey));
		}
		return "0";
	}

	@Override
	public ISorting<?> next() {
		return new RainfallSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.levels");
	}

}
