package com.chaosthedude.naturescompass.items;

import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.utils.PlayerUtils;
import com.chaosthedude.naturescompass.workers.BiomeSearchWorker;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class NaturesCompassItem extends Item {
	
	private BiomeSearchWorker worker;
	
	public NaturesCompassItem() {
        super(new Settings().maxCount(1));
    }
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			if (world.isClient) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				GuiWrapper.openGUI(world, player, stack);
			} else {
				final ServerWorld serverWorld = (ServerWorld) world;
				final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				final boolean canTeleport = NaturesCompassConfig.allowTeleport && PlayerUtils.canTeleport(player);
				final List<Identifier> allowedBiomeIDs = BiomeUtils.getAllowedBiomeIDs(world);
				ServerPlayNetworking.send(serverPlayer, new SyncPacket(canTeleport, allowedBiomeIDs, BiomeUtils.getGeneratingDimensionsForAllowedBiomes(serverWorld)));
			}
		} else {
			if (worker != null) {
				worker.stop();
				worker = null;
			}
			setState(player.getStackInHand(hand), null, CompassState.INACTIVE, player);
		}
		return TypedActionResult.pass(player.getStackInHand(hand));
	}

	public void searchForBiome(ServerWorld world, PlayerEntity player, Identifier biomeID, BlockPos pos, ItemStack stack) {
		setSearching(stack, biomeID, player);
		Optional<Biome> optionalBiome = BiomeUtils.getBiomeForIdentifier(world, biomeID);
 		if (optionalBiome.isPresent()) {
 			if (worker != null) {
 				worker.stop();
 			}
 			worker = new BiomeSearchWorker(world, player, stack, optionalBiome.get(), pos);
 			worker.start();
 		}
	}
	
	public void succeed(ItemStack stack, PlayerEntity player, int x, int z, int samples, boolean displayCoordinates) {
		setFound(stack, x, z, samples, player);
		setDisplayCoordinates(stack, displayCoordinates);
		worker = null;
	}
	
	public void fail(ItemStack stack, PlayerEntity player, int searchRadius, int samples) {
		setNotFound(stack, player, searchRadius, samples);
		worker = null;
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.isCompass(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, Identifier biomeID, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.BIOME_ID_COMPONENT, biomeID.toString());
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.SEARCHING.getID());
			stack.set(NaturesCompass.SEARCH_RADIUS_COMPONENT, 0);
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.FOUND.getID());
			stack.set(NaturesCompass.FOUND_X_COMPONENT, x);
			stack.set(NaturesCompass.FOUND_Z_COMPONENT, z);
			stack.set(NaturesCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setNotFound(ItemStack stack, PlayerEntity player, int searchRadius, int samples) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.NOT_FOUND.getID());
			stack.set(NaturesCompass.SEARCH_RADIUS_COMPONENT, searchRadius);
			stack.set(NaturesCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setInactive(ItemStack stack, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, state.getID());
		}
	}
	
	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
  		if (ItemUtils.isCompass(stack)) {
  			stack.set(NaturesCompass.DISPLAY_COORDS_COMPONENT, displayPosition);
  		}
  	}

	public void setFoundBiomeX(ItemStack stack, int x, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.FOUND_X_COMPONENT, x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.FOUND_Z_COMPONENT, z);
		}
	}

	public void setBiomeID(ItemStack stack, Identifier biomeID, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.BIOME_ID_COMPONENT, biomeID.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.SEARCH_RADIUS_COMPONENT, searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, PlayerEntity player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(NaturesCompass.COMPASS_STATE_COMPONENT)) {
			return CompassState.fromID(stack.get(NaturesCompass.COMPASS_STATE_COMPONENT));
		}

		return null;
	}

	public int getFoundBiomeX(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(NaturesCompass.FOUND_X_COMPONENT)) {
			return stack.get(NaturesCompass.FOUND_X_COMPONENT);
		}

		return 0;
	}

	public int getFoundBiomeZ(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(NaturesCompass.FOUND_Z_COMPONENT)) {
			return stack.get(NaturesCompass.FOUND_Z_COMPONENT);
		}

		return 0;
	}

	public Identifier getBiomeID(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(NaturesCompass.BIOME_ID_COMPONENT)) {
			return Identifier.of(stack.get(NaturesCompass.BIOME_ID_COMPONENT));
		}

		return Identifier.of("", "");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(NaturesCompass.SEARCH_RADIUS_COMPONENT)) {
			return stack.get(NaturesCompass.SEARCH_RADIUS_COMPONENT);
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(NaturesCompass.SAMPLES_COMPONENT)) {
			return stack.get(NaturesCompass.SAMPLES_COMPONENT);
		}

		return -1;
	}
	
	public boolean shouldDisplayCoordinates(ItemStack stack) {
  		if (ItemUtils.isCompass(stack) && stack.contains(NaturesCompass.DISPLAY_COORDS_COMPONENT)) {
  			return stack.get(NaturesCompass.DISPLAY_COORDS_COMPONENT);
  		}

  		return true;
  	}

	public int getDistanceToBiome(PlayerEntity player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}

}
