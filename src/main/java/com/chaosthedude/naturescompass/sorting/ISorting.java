package com.chaosthedude.naturescompass.sorting;

import java.util.Comparator;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISorting<T> extends Comparator<ResourceLocation> {

	@Override
	public int compare(ResourceLocation biomeKey1, ResourceLocation biomeKey2);

	public T getValue(ResourceLocation biomeKey);

	public ISorting<?> next();

	public String getLocalizedName();

}
