package com.chaosthedude.naturescompass.client;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

public class NaturesCompassAngle implements RangeSelectItemModelProperty {

	public static final MapCodec<NaturesCompassAngle> MAP_CODEC = MapCodec.unit(new NaturesCompassAngle());
	private final NaturesCompassAngleState state;

	public NaturesCompassAngle() {
		this(new NaturesCompassAngleState());
	}

	private NaturesCompassAngle(NaturesCompassAngleState state) {
		this.state = state;
	}

	@Override
	public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		return state.get(stack, level, owner, seed);
	}

	@Override
	public MapCodec<NaturesCompassAngle> type() {
		return MAP_CODEC;
	}

}