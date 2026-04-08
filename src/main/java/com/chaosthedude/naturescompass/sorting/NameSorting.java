package com.chaosthedude.naturescompass.sorting;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class NameSorting implements ISorting<String> {

	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public int compare(Identifier biomeID1, Identifier biomeID2) {
		return getValue(biomeID1).compareTo(getValue(biomeID2));
	}

	@Override
	public String getValue(Identifier biomeID) {
		if (client.world != null) {
			return BiomeUtils.getBiomeName(client.world, biomeID);
		}
		return "";
	}

	@Override
	public ISorting<?> next() {
		return new SourceSorting();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translate("string.naturescompass.name");
	}

}
