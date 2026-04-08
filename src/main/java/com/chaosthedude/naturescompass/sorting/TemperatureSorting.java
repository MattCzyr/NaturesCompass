package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class TemperatureSorting implements ISorting<Float> {

	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public int compare(Identifier biomeID1, Identifier biomeID2) {
		return getValue(biomeID1).compareTo(getValue(biomeID2));
	}

	@Override
	public Float getValue(Identifier biomeID) {
		if (client.world != null && BiomeUtils.getBiomeForIdentifier(client.world, biomeID).isPresent()) {
			Biome biome = BiomeUtils.getBiomeForIdentifier(client.world, biomeID).get();
			return biome.getTemperature();
		}
		return 0F;
	}

	@Override
	public ISorting<?> next() {
		return new NameSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.temperature");
	}

}
