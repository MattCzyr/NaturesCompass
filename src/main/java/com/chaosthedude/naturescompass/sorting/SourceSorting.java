package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SourceSorting implements ISorting<String> {

	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public int compare(Identifier biomeId1, Identifier biomeId2) {
		return getValue(biomeId1).compareTo(getValue(biomeId2));
	}

	@Override
	public String getValue(Identifier biomeId) {
		if (client.world != null) {
			return BiomeUtils.getBiomeSource(client.world, biomeId);
		}
		return null;
	}

	@Override
	public ISorting<?> next() {
		return new TagsSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.source");
	}

}
