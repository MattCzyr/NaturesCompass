package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface ISorting<T> extends Comparator<Identifier> {

	@Override
	public int compare(Identifier biomeID1, Identifier biomeID2);

	public T getValue(Identifier biomeID);

	public ISorting<?> next();

	public String getLocalizedName();

}
