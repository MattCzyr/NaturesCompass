package com.chaosthedude.naturescompass.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.utils.NaturesCompassAngle;
import com.mojang.serialization.MapCodec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

@Environment(EnvType.CLIENT)
@Mixin(RangeSelectItemModelProperties.class)
public class RangeSelectItemModelPropertiesMixin {
	
	@Shadow
	@Final
	public static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends RangeSelectItemModelProperty>> ID_MAPPER;

	@Inject(method = "bootstrap()V", at = @At(value = "TAIL"))
	private static void registerCompassProperty(CallbackInfo info) {
		ID_MAPPER.put(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "angle"), NaturesCompassAngle.MAP_CODEC);
	}
	
}