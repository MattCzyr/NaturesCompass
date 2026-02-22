package com.chaosthedude.naturescompass.sorting;

import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

@Environment(EnvType.CLIENT)
public class DimensionSorting implements ISorting<String> {
	
	private static final Minecraft mc = Minecraft.getInstance();

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return getValue(biome1).compareTo(getValue(biome2));
	}

	@Override
	public String getValue(Biome biome) {
		if (mc.level != null) {
			Optional<Identifier> biomeID = BiomeUtils.getIdForBiome(mc.level, biome);
			if (biomeID.isPresent()) {
				return BiomeUtils.dimensionIdsToString(NaturesCompass.dimensionsForAllowedBiomes.get(biomeID.get()));
			}
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