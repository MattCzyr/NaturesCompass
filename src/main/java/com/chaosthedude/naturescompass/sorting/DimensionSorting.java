package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

public class DimensionSorting implements ISorting<String> {
	
	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(Identifier biomeId1, Identifier biomeId2) {
		return getValue(biomeId1).compareTo(getValue(biomeId2));
	}

	@Override
	public String getValue(Identifier biomeId) {
		if (mc.level != null) {
			return BiomeUtils.dimensionIdsToString(NaturesCompass.dimensionsForAllowedBiomes.get(biomeId));
		}
		return "";
	}

	@Override
	public ISorting<?> next() {
		return new XpLevelsSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.dimension");
	}

}
