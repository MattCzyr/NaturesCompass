package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class RainfallSorting implements ISorting<Float> {

	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public int compare(Identifier biomeId1, Identifier biomeId2) {
		return getValue(biomeId1).compareTo(getValue(biomeId2));
	}

	@Override
	public Float getValue(Identifier biomeId) {
		if (client.world != null && BiomeUtils.getBiomeForIdentifier(client.world, biomeId).isPresent()) {
			Biome biome = BiomeUtils.getBiomeForIdentifier(client.world, biomeId).get();
			return biome.weather.downfall();
		}
		return 0F;
	}

	@Override
	public ISorting<?> next() {
		return new TemperatureSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.rainfall");
	}

}
