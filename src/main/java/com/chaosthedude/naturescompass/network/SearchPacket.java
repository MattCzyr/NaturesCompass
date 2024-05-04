package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SearchPacket(ResourceLocation biomeKey, BlockPos pos) implements CustomPacketPayload {
	
	public static final ResourceLocation ID = new ResourceLocation(NaturesCompass.MODID, "search");

	public static SearchPacket read(FriendlyByteBuf buf) {
		return new SearchPacket(buf.readResourceLocation(), buf.readBlockPos());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(biomeKey);
		buf.writeBlockPos(pos);
	}

	public static void handle(SearchPacket packet, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			if (context.player().isPresent() && context.level().isPresent()) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player().get());
				if (!stack.isEmpty()) {
					final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
					natureCompass.searchForBiome((ServerLevel) context.level().get(), context.player().get(), packet.biomeKey, packet.pos, stack);
				}
			}
		});
	}
	
	@Override
	public ResourceLocation id() {
		return ID;
	}

}
