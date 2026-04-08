package com.chaosthedude.naturescompass.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.utils.PlayerUtils;
import com.chaosthedude.naturescompass.workers.BiomeSearchWorker;
import com.google.common.collect.ListMultimap;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class NaturesCompassItem extends Item {

	private BiomeSearchWorker worker;

	public NaturesCompassItem() {
        super(new Settings().maxCount(1));
    }

	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			if (isBroken(player.getStackInHand(hand))) {
				return TypedActionResult.pass(player.getStackInHand(hand));
			}
			if (world.isClient) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				GuiWrapper.openGUI(world, player, stack);
			} else {
				final ServerWorld serverWorld = (ServerWorld) world;
				final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				final boolean canTeleport = NaturesCompassConfig.allowTeleport && PlayerUtils.canTeleport(player);
				final int maxNextSearches = NaturesCompassConfig.maxNextSearches;
				final boolean hasInfiniteXp = player.getAbilities().creativeMode;
				final List<Identifier> allowedBiomeIDs = BiomeUtils.getAllowedBiomeIDs(world);
				final Map<Identifier, Integer> xpLevels = BiomeUtils.getXpLevelsForAllowedBiomes(allowedBiomeIDs);
				final ListMultimap<Identifier, Identifier> generatingDimensions = BiomeUtils.getGeneratingDimensionsForAllowedBiomes(serverWorld, allowedBiomeIDs);
				ServerPlayNetworking.send(serverPlayer, new SyncPacket(canTeleport, maxNextSearches, hasInfiniteXp, allowedBiomeIDs, xpLevels, generatingDimensions));
			}
		} else {
			if (worker != null) {
				worker.stop();
				worker = null;
			}
			clearSearchData(player.getStackInHand(hand));
			setState(player.getStackInHand(hand), null, CompassState.INACTIVE, player);
		}
		return TypedActionResult.pass(player.getStackInHand(hand));
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		if (max > 0) {
			int damage = stack.getOrDefault(NaturesCompass.DAMAGE_COMPONENT, 0);
			return damage > 0;
		}
		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		if (max > 0) {
			int damage = stack.getOrDefault(NaturesCompass.DAMAGE_COMPONENT, 0);
			return Math.round(13.0f * (1.0f - (float) damage / max));
		}
		return 13;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		int damage = stack.getOrDefault(NaturesCompass.DAMAGE_COMPONENT, 0);
		float f = max > 0 ? (float) damage / max : 0.0f;
		return MathHelper.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
	}

	public void searchForBiome(ServerWorld world, PlayerEntity player, Identifier biomeID, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			search(stack, biomeID, player);

			if (worker != null) {
				worker.stop();
			}
			List<BlockPos> prevPos = new ArrayList<BlockPos>();
			worker = new BiomeSearchWorker(world, player, stack, biomeID, pos, prevPos);
			worker.start();

			int xpLevels = BiomeUtils.getXpLevelsForBiome(biomeID);
			if (!player.getAbilities().creativeMode && xpLevels > 0) {
				player.addExperienceLevels(-xpLevels);
			}
		}
	}

	public void searchForNextBiome(ServerWorld world, PlayerEntity player, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			List<BlockPos> prevPos = stack.getOrDefault(NaturesCompass.PREV_POS_COMPONENT, null);
			Identifier biomeID = getBiomeID(stack);
			if (prevPos != null && biomeID != null && !biomeID.toString().isEmpty()) {
				search(stack, biomeID, player);

				if (worker != null) {
					worker.stop();
				}
				worker = new BiomeSearchWorker(world, player, stack, biomeID, pos, prevPos);
				worker.start();

				int xpLevels = BiomeUtils.getXpLevelsForBiome(biomeID);
				if (!player.getAbilities().creativeMode && xpLevels > 0) {
					player.addExperienceLevels(-xpLevels);
				}
			}
		}
	}

	public void succeed(ItemStack stack, PlayerEntity player, Identifier biomeID, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		clearSearchData(stack);
		setFound(stack, x, z, samples, player);
		setBiomeID(stack, biomeID, player);
		stack.set(NaturesCompass.PREV_POS_COMPONENT, prevPos);
		setDisplayCoordinates(stack, displayCoordinates);
		damageCompass(stack);
		worker = null;
	}

	public void fail(ItemStack stack, PlayerEntity player, Identifier biomeID, int searchRadius, int samples) {
		clearSearchData(stack);
		setNotFound(stack, player, searchRadius, samples);
		setBiomeID(stack, biomeID, player);
		worker = null;
	}

	private void damageCompass(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		if (max > 0) {
			int damage = stack.getOrDefault(NaturesCompass.DAMAGE_COMPONENT, 0) + 1;
			stack.set(NaturesCompass.DAMAGE_COMPONENT, damage);
		}
	}

	public boolean isBroken(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		return max > 0 && stack.getOrDefault(NaturesCompass.DAMAGE_COMPONENT, 0) >= max;
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.isCompass(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	private void search(ItemStack stack, Identifier biomeID, PlayerEntity player) {
		clearSearchData(stack);
		setState(stack, null, CompassState.SEARCHING, player);
		setBiomeID(stack, biomeID, player);
		setSearchRadius(stack, 0, player);
		setSamples(stack, 0, player);
	}

	private void clearSearchData(ItemStack stack) {
		stack.remove(NaturesCompass.COMPASS_STATE_COMPONENT);
		stack.remove(NaturesCompass.BIOME_ID_COMPONENT);
		stack.remove(NaturesCompass.FOUND_X_COMPONENT);
		stack.remove(NaturesCompass.FOUND_Z_COMPONENT);
		stack.remove(NaturesCompass.PREV_POS_COMPONENT);
		stack.remove(NaturesCompass.SAMPLES_COMPONENT);
		stack.remove(NaturesCompass.SEARCH_RADIUS_COMPONENT);
		stack.remove(NaturesCompass.DISPLAY_COORDS_COMPONENT);
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

		return null;
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
