package com.chaosthedude.naturescompass.items;

import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.util.BiomeSearchWorker;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.network.PacketDistributor;

public class NaturesCompassItem extends Item {

	public static final String NAME = "naturescompass";
	
	private BiomeSearchWorker worker;

	public NaturesCompassItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!player.isCrouching()) {
			if (level.isClientSide()) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerLevel serverLevel = (ServerLevel) level;
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(serverPlayer.getServer(), player);
				final List<ResourceLocation> allowedBiomeKeys = BiomeUtils.getAllowedBiomeKeys(level);
				PacketDistributor.sendToPlayer(serverPlayer, new SyncPacket(canTeleport, allowedBiomeKeys, BiomeUtils.getGeneratingDimensionsForAllowedBiomes(serverLevel)));
			}
		} else {
			if (worker != null) {
				worker.stop();
				worker = null;
			}
			setState(player.getItemInHand(hand), null, CompassState.INACTIVE, player);
		}

		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, player.getItemInHand(hand));
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (getState(oldStack) == getState(newStack)) {
			return false;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	public void searchForBiome(ServerLevel level, Player player, ResourceLocation biomeKey, BlockPos pos, ItemStack stack) {
		setSearching(stack, biomeKey, player);
		Optional<Biome> optionalBiome = BiomeUtils.getBiomeForKey(level, biomeKey);
		if (optionalBiome.isPresent()) {
			if (worker != null) {
				worker.stop();
			}
			worker = new BiomeSearchWorker(level, player, stack, optionalBiome.get(), pos);
			worker.start();
		}
	}
	
	public void succeed(ItemStack stack, Player player, int x, int z, int samples, boolean displayCoordinates) {
		setFound(stack, x, z, samples, player);
		setDisplayCoordinates(stack, displayCoordinates);
		worker = null;
	}
	
	public void fail(ItemStack stack, Player player, int radius, int samples) {
		setNotFound(stack, player, radius, samples);
		worker = null;
	}

	public boolean isActive(ItemStack stack) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, ResourceLocation biomeKey, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.BIOME_ID, biomeKey.toString());
			stack.set(NaturesCompass.COMPASS_STATE, CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.COMPASS_STATE, CompassState.FOUND.getID());
			stack.set(NaturesCompass.FOUND_X, x);
			stack.set(NaturesCompass.FOUND_Z, z);
			stack.set(NaturesCompass.SAMPLES, samples);
		}
	}

	public void setNotFound(ItemStack stack, Player player, int searchRadius, int samples) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.COMPASS_STATE, CompassState.NOT_FOUND.getID());
			stack.set(NaturesCompass.SEARCH_RADIUS, searchRadius);
			stack.set(NaturesCompass.SAMPLES, samples);
		}
	}

	public void setInactive(ItemStack stack, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.COMPASS_STATE, CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.COMPASS_STATE, state.getID());
		}
	}

	public void setFoundBiomeX(ItemStack stack, int x, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.FOUND_X, x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.FOUND_Z, z);
		}
	}

	public void setBiomeKey(ItemStack stack, ResourceLocation biomeKey, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.BIOME_ID, biomeKey.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.SEARCH_RADIUS, searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, Player player) {
		if (stack.getItem() == NaturesCompass.naturesCompass) {
			stack.set(NaturesCompass.SAMPLES, samples);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (stack.getItem() == NaturesCompass.naturesCompass && stack.has(NaturesCompass.COMPASS_STATE)) {
			return CompassState.fromID(stack.get(NaturesCompass.COMPASS_STATE));
		}

		return null;
	}
	
	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
 		if (stack.getItem() == NaturesCompass.naturesCompass) {
 			stack.set(NaturesCompass.DISPLAY_COORDS, displayPosition);
 		}
 	}

	public int getFoundBiomeX(ItemStack stack) {
		if (stack.getItem() == NaturesCompass.naturesCompass && stack.has(NaturesCompass.FOUND_X)) {
			return stack.get(NaturesCompass.FOUND_X);
		}

		return 0;
	}

	public int getFoundBiomeZ(ItemStack stack) {
		if (stack.getItem() == NaturesCompass.naturesCompass && stack.has(NaturesCompass.FOUND_Z)) {
			return stack.get(NaturesCompass.FOUND_Z);
		}

		return 0;
	}

	public ResourceLocation getBiomeKey(ItemStack stack) {
		if (stack.getItem() == NaturesCompass.naturesCompass && stack.has(NaturesCompass.BIOME_ID)) {
			return ResourceLocation.parse(stack.get(NaturesCompass.BIOME_ID));
		}

		return ResourceLocation.fromNamespaceAndPath("", "");
	}

	public int getSearchRadius(ItemStack stack) {
		if (stack.getItem() == NaturesCompass.naturesCompass && stack.has(NaturesCompass.SEARCH_RADIUS)) {
			return stack.get(NaturesCompass.SEARCH_RADIUS);
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (stack.getItem() == NaturesCompass.naturesCompass && stack.has(NaturesCompass.SAMPLES)) {
			return stack.get(NaturesCompass.SAMPLES);
		}

		return -1;
	}

	public int getDistanceToBiome(Player player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}
	
	public boolean shouldDisplayCoordinates(ItemStack stack) {
 		if (stack.getItem() == NaturesCompass.naturesCompass && stack.has(NaturesCompass.DISPLAY_COORDS)) {
 			return stack.get(NaturesCompass.DISPLAY_COORDS);
 		}

 		return true;
 	}

}
