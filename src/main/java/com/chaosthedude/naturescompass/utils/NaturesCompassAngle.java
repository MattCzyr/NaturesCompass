package com.chaosthedude.naturescompass.utils;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class NaturesCompassAngle implements NumericProperty {

	public static final MapCodec<NaturesCompassAngle> MAP_CODEC = MapCodec.unit(new NaturesCompassAngle());
	private final NaturesCompassAngleState state;

	public NaturesCompassAngle() {
		this(new NaturesCompassAngleState());
	}

	private NaturesCompassAngle(NaturesCompassAngleState state) {
		this.state = state;
	}

	@Override
	public float getValue(ItemStack stack, @Nullable ClientWorld level, @Nullable LivingEntity entity, int seed) {
		return state.getValue(stack, level, entity, seed);
	}

	@Override
	public MapCodec<NaturesCompassAngle> getCodec() {
		return MAP_CODEC;
	}

}