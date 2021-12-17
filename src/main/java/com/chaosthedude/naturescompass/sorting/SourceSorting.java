package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class SourceSorting implements ISorting<String> {
	
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return getValue(biome1).compareTo(getValue(biome2));
	}

	@Override
	public String getValue(Biome biome) {
		if (client.world != null) {
			return BiomeUtils.getBiomeSource(client.world, biome);
		}
		return null;
	}

	@Override
	public ISorting<?> next() {
		return new CategorySorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.source");
	}

}