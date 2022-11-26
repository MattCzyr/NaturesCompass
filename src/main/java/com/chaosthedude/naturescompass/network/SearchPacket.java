package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.ItemUtils;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SearchPacket extends PacketByteBuf {
	
	public static final Identifier ID = new Identifier(NaturesCompass.MODID, "search");
	
	public SearchPacket(Identifier biomeID, BlockPos pos) {
		super(Unpooled.buffer());
		writeIdentifier(biomeID);
		writeBlockPos(pos);
	}

    public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final Identifier biomeID = buf.readIdentifier();
		final BlockPos pos = buf.readBlockPos();
		
		server.execute(() -> {
	    	final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final ServerWorld world = player.getWorld();
				natureCompass.searchForBiome(world, player, biomeID, pos, stack);
			}
		});
	}

}