package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SourceSorting implements ISorting<String> {

	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(Identifier biomeId1, Identifier biomeId2) {
		return getValue(biomeId1).compareTo(getValue(biomeId2));
	}

	@Override
	public String getValue(Identifier biomeId) {
		if (mc.level != null) {
			return BiomeUtils.getBiomeSource(mc.level, biomeId);
		}
		return null;
	}

	@Override
	public ISorting<?> next() {
		return new TagsSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.source");
	}

}
