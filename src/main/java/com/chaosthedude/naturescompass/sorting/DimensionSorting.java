package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class DimensionSorting implements ISorting<String> {

	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(ResourceLocation biomeKey1, ResourceLocation biomeKey2) {
		return getValue(biomeKey1).compareTo(getValue(biomeKey2));
	}

	@Override
	public String getValue(ResourceLocation biomeKey) {
		if (mc.level != null) {
			return BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionKeysForAllowedBiomeKeys.get(biomeKey));
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
