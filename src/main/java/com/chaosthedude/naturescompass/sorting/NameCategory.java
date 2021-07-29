package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.biome.Biome;

public class NameCategory implements ISortingCategory {
	
	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(Biome biome1, Biome biome2) {
		if (mc.level != null) {
			return BiomeUtils.getBiomeName(mc.level, biome1).compareTo(BiomeUtils.getBiomeName(mc.level, biome2));
		}
		return 0;
	}

	@Override
	public Object getValue(Biome biome) {
		return null;
	}

	@Override
	public ISortingCategory next() {
		return new SourceCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.name");
	}

}
