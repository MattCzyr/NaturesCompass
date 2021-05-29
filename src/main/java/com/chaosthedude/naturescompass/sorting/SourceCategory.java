package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SourceCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.world != null) {
			return BiomeUtils.getBiomeSource(mc.world, biome1).compareTo(BiomeUtils.getBiomeSource(mc.world, biome2));
		}
		return 0;
	}

	@Override
	public Object getValue(Biome biome) {
		return null;
	}

	@Override
	public ISortingCategory next() {
		return new BaseHeightCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.source");
	}

}