package com.chaosthedude.naturescompass;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.utils.CompassState;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class NaturesCompassClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncPacket.ID, SyncPacket::apply);
		
		ModelPredicateProviderRegistry.register(NaturesCompass.NATURES_COMPASS_ITEM, new Identifier("angle"), new ClampedModelPredicateProvider() {
			private double rotation;
			private double rota;
			private long lastUpdateTick;

			@Override
			public float unclampedCall(ItemStack stack, ClientWorld world, LivingEntity entityLiving, int seed) {
				if (entityLiving == null && !stack.isInFrame()) {
					return 0.0F;
				} else {
					final boolean entityExists = entityLiving != null;
					final Entity entity = (Entity) (entityExists ? entityLiving : stack.getFrame());
					if (world == null && entity.getWorld() instanceof ClientWorld) {
						world = (ClientWorld) entity.getWorld();
					}

					double rotation = entityExists ? (double) entity.getYaw() : getFrameRotation((ItemFrameEntity) entity);
					rotation = rotation % 360.0D;
					double adjusted = Math.PI - ((rotation - 90.0D) * 0.01745329238474369D - getAngle(world, entity, stack));

					if (entityExists) {
						adjusted = wobble(world, adjusted);
					}

					final float f = (float) (adjusted / (Math.PI * 2D));
					return MathHelper.floorMod(f, 1.0F);
				}
			}

			private double wobble(ClientWorld world, double amount) {
				if (world.getTime() != lastUpdateTick) {
					lastUpdateTick = world.getTime();
					double d0 = amount - rotation;
					d0 = d0 % (Math.PI * 2D);
					d0 = MathHelper.floorMod(d0 + Math.PI, Math.PI * 2D) - Math.PI;
					rota += d0 * 0.1D;
					rota *= 0.8D;
					rotation += rota;
				}

				return rotation;
			}

			private double getFrameRotation(ItemFrameEntity itemFrame) {
				return (double) MathHelper.wrapDegrees(180 + itemFrame.getHorizontalFacing().getHorizontal() * 90);
			}

			private double getAngle(ClientWorld world, Entity entity, ItemStack stack) {
				if (stack.getItem() == NaturesCompass.NATURES_COMPASS_ITEM) {
					NaturesCompassItem compassItem = (NaturesCompassItem) stack.getItem();
					BlockPos pos;
					if (compassItem.getState(stack) == CompassState.FOUND) {
						pos = new BlockPos(compassItem.getFoundBiomeX(stack), 0, compassItem.getFoundBiomeZ(stack));
					} else {
						pos = world.getSpawnPos();
					}
					return Math.atan2((double) pos.getZ() - entity.getPos().z, (double) pos.getX() - entity.getPos().x);
				}
				return 0.0D;
			}
		});
	}

}
