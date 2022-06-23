package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.biome.Biome;

public class TagsSorting implements ISorting<String> {
	
	private static final Minecraft mc = Minecraft.getInstance();
	
	@Override
	public int compare(Biome biome1, Biome biome2) {
		return getValue(biome1).compareTo(getValue(biome2));
	}

	@Override
	public String getValue(Biome biome) {
		if (mc.level != null) {
			return BiomeUtils.getBiomeTags(mc.level, biome);
		}
		return "";
	}

	@Override
	public ISorting<?> next() {
		return new RainfallSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.tags");
	}

}
