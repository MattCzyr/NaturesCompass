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
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

@Environment(EnvType.CLIENT)
@Mixin(NumericProperties.class)
public class NumericPropertiesMixin {
	
	@Shadow
	@Final
	public static Codecs.IdMapper<Identifier, MapCodec<? extends NumericProperty>> ID_MAPPER;

	@Inject(method = "bootstrap()V", at = @At(value = "TAIL"))
	private static void registerCompassProperty(CallbackInfo info) {
		ID_MAPPER.put(Identifier.of(NaturesCompass.MODID, "angle"), NaturesCompassAngle.MAP_CODEC);
	}
	
}