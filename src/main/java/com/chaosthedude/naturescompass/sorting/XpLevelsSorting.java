package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

public class XpLevelsSorting implements ISorting<String> {

	@Override
	public int compare(Identifier biomeID1, Identifier biomeID2) {
		return getValue(biomeID1).compareTo(getValue(biomeID2));
	}

	@Override
	public String getValue(Identifier biomeID) {
		if (NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeID)) {
			return String.valueOf(NaturesCompass.xpLevelsForAllowedBiomes.get(biomeID));
		}
		return "0";
	}

	@Override
	public ISorting<?> next() {
		return new RainfallSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.levels");
	}

}
