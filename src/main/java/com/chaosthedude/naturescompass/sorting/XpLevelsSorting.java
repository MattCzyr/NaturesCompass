package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

public class XpLevelsSorting implements ISorting<String> {

	@Override
	public int compare(Identifier biomeId1, Identifier biomeId2) {
		return getValue(biomeId1).compareTo(getValue(biomeId2));
	}

	@Override
	public String getValue(Identifier biomeId) {
		if (NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeId) ) {
			return String.valueOf(NaturesCompass.xpLevelsForAllowedBiomes.get(biomeId));
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
