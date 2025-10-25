package com.chaosthedude.naturescompass.utils;

import com.mojang.serialization.MapCodec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;

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
	public float getValue(ItemStack stack, ClientWorld world, HeldItemContext context, int seed) {
		return state.getValue(stack, world, context, seed);
	}

	@Override
	public MapCodec<NaturesCompassAngle> getCodec() {
		return MAP_CODEC;
	}

}