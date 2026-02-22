package com.chaosthedude.naturescompass.mixin;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.naturescompass.worker.WorldWorkerManager;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(method = "tickServer(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "HEAD"))
	private void startTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		WorldWorkerManager.tick(true);
	}
	
	@Inject(method = "tickServer(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "TAIL"))
	private void endTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		WorldWorkerManager.tick(false);
	}
	
	@Inject(method = "stopServer()V", at = @At(value = "TAIL"))
	private void onShutdown(CallbackInfo ci) {
		WorldWorkerManager.clear();
	}
	
}
