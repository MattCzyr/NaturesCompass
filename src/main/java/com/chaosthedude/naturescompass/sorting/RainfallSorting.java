package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RainfallSorting implements ISorting<Float> {

	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(ResourceLocation biomeKey1, ResourceLocation biomeKey2) {
		return getValue(biomeKey1).compareTo(getValue(biomeKey2));
	}

	@Override
	public Float getValue(ResourceLocation biomeKey) {
		if (mc.level != null && BiomeUtils.getBiomeForKey(mc.level, biomeKey).isPresent()) {
			Biome biome = BiomeUtils.getBiomeForKey(mc.level, biomeKey).get();
			return biome.getModifiedClimateSettings().downfall();
		}
		return 0F;
	}

	@Override
	public ISorting<?> next() {
		return new TemperatureSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.get("string.naturescompass.rainfall");
	}

}
