package com.chaosthedude.naturescompass.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;
import com.chaosthedude.naturescompass.worker.BiomeSearchWorker;
import com.google.common.collect.ListMultimap;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class NaturesCompassItem extends Item {

	public static final String NAME = "naturescompass";

	public static final ResourceKey<Item> KEY = ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(NaturesCompass.MODID, NAME));

	private BiomeSearchWorker worker;

	public NaturesCompassItem() {
        super(new Properties().setId(KEY).stacksTo(1));
    }

	@Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCrouching()) {
			if (level.isClientSide()) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerLevel serverLevel = (ServerLevel) level;
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = NaturesCompassConfig.allowTeleport && PlayerUtils.canTeleport(serverPlayer.level().getServer(), player);
				final int maxNextSearches = NaturesCompassConfig.maxNextSearches;
				final boolean hasInfiniteXp = player.hasInfiniteMaterials();
				final List<Identifier> allowedBiomeIds = BiomeUtils.getAllowedBiomes(level);
				final Map<Identifier, Integer> xpLevels = BiomeUtils.getXpLevelsForAllowedBiomes(serverLevel, allowedBiomeIds);
				final ListMultimap<Identifier, Identifier> generatingDimensions = BiomeUtils.getGeneratingDimensionsForAllowedBiomes(serverLevel, allowedBiomeIds);
				ServerPlayNetworking.send(serverPlayer, new SyncPacket(canTeleport, maxNextSearches, hasInfiniteXp, allowedBiomeIds, xpLevels, generatingDimensions));
			}
		} else {
			if (worker != null) {
				worker.stop();
				worker = null;
			}
			setCompassState(player.getItemInHand(hand), null, CompassState.INACTIVE, player);
		}
		return InteractionResult.CONSUME;
	}

	public void searchForBiome(ServerLevel level, Player player, Identifier biomeId, BlockPos pos, ItemStack stack) {
		Optional<Biome> optionalBiome = BiomeUtils.getBiomeForId(level, biomeId);
		if (optionalBiome.isPresent()) {
			setSearching(stack, biomeId, player);

			if (worker != null) {
				worker.stop();
			}
			List<BlockPos> prevPos = new ArrayList<BlockPos>();
			worker = new BiomeSearchWorker(level, player, stack, optionalBiome.get(), pos, prevPos);
			worker.start();

			int xpLevels = BiomeUtils.getXpLevelsForBiome(level, biomeId);
			if (!player.hasInfiniteMaterials() && xpLevels > 0) {
				player.giveExperienceLevels(-xpLevels);
			}
		} else {
			setNotFound(stack, player, 0, 0);
		}
	}

	public void searchForNextBiome(ServerLevel level, Player player, BlockPos pos, ItemStack stack) {
		List<BlockPos> prevPos = getPrevPos(stack);
		Identifier biomeId = getBiomeId(stack);
		if (prevPos != null && biomeId != null) {
			Optional<Biome> optionalBiome = BiomeUtils.getBiomeForId(level, biomeId);
			if (optionalBiome.isPresent()) {
				setSearching(stack, biomeId, player);

				if (worker != null) {
					worker.stop();
				}
				worker = new BiomeSearchWorker(level, player, stack, optionalBiome.get(), pos, prevPos);
				worker.start();

				int xpLevels = BiomeUtils.getXpLevelsForBiome(level, biomeId);
				if (!player.hasInfiniteMaterials() && xpLevels > 0) {
					player.giveExperienceLevels(-xpLevels);
				}
			} else {
				setNotFound(stack, player, 0, 0);
			}
		}
	}

	public void succeed(ItemStack stack, Player player, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		setFound(stack, x, z, samples, player);
		setDisplayCoordinates(stack, displayCoordinates);
		setPrevPos(stack, prevPos);
		worker = null;
	}

	public void fail(ItemStack stack, Player player, int searchRadius, int samples) {
		setNotFound(stack, player, searchRadius, samples);
		worker = null;
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.isCompass(stack)) {
			return getCompassState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setPrevPos(ItemStack stack, List<BlockPos> prevPos) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.PREV_POS_COMPONENT, prevPos);
		}
	}

	public void setSearching(ItemStack stack, Identifier biomeId, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.BIOME_ID_COMPONENT, biomeId.toString());
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.SEARCHING.getID());
			stack.set(NaturesCompass.SEARCH_RADIUS_COMPONENT, 0);
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.FOUND.getID());
			stack.set(NaturesCompass.FOUND_X_COMPONENT, x);
			stack.set(NaturesCompass.FOUND_Z_COMPONENT, z);
			stack.set(NaturesCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setNotFound(ItemStack stack, Player player, int searchRadius, int samples) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.NOT_FOUND.getID());
			stack.set(NaturesCompass.SEARCH_RADIUS_COMPONENT, searchRadius);
			stack.set(NaturesCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setInactive(ItemStack stack, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, CompassState.INACTIVE.getID());
		}
	}

	public void setCompassState(ItemStack stack, BlockPos pos, CompassState state, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.COMPASS_STATE_COMPONENT, state.getID());
		}
	}

	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
  		if (ItemUtils.isCompass(stack)) {
  			stack.set(NaturesCompass.DISPLAY_COORDS_COMPONENT, displayPosition);
  		}
  	}

	public void setFoundBiomeX(ItemStack stack, int x, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.FOUND_X_COMPONENT, x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.FOUND_Z_COMPONENT, z);
		}
	}

	public void setBiomeId(ItemStack stack, Identifier biomeId, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.BIOME_ID_COMPONENT, biomeId.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.SEARCH_RADIUS_COMPONENT, searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(NaturesCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public CompassState getCompassState(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.COMPASS_STATE_COMPONENT)) {
			return CompassState.fromID(stack.get(NaturesCompass.COMPASS_STATE_COMPONENT));
		}

		return null;
	}

	public int getFoundBiomeX(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.FOUND_X_COMPONENT)) {
			return stack.get(NaturesCompass.FOUND_X_COMPONENT);
		}

		return 0;
	}

	public int getFoundBiomeZ(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.FOUND_Z_COMPONENT)) {
			return stack.get(NaturesCompass.FOUND_Z_COMPONENT);
		}

		return 0;
	}

	public Identifier getBiomeId(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.BIOME_ID_COMPONENT)) {
			return Identifier.parse(stack.get(NaturesCompass.BIOME_ID_COMPONENT));
		}

		return Identifier.fromNamespaceAndPath("", "");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.SEARCH_RADIUS_COMPONENT)) {
			return stack.get(NaturesCompass.SEARCH_RADIUS_COMPONENT);
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.SAMPLES_COMPONENT)) {
			return stack.get(NaturesCompass.SAMPLES_COMPONENT);
		}

		return -1;
	}

	public List<BlockPos> getPrevPos(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.PREV_POS_COMPONENT)) {
			return stack.get(NaturesCompass.PREV_POS_COMPONENT);
		}

		return null;
	}

	public boolean shouldDisplayCoordinates(ItemStack stack) {
  		if (ItemUtils.isCompass(stack) && stack.has(NaturesCompass.DISPLAY_COORDS_COMPONENT)) {
  			return stack.get(NaturesCompass.DISPLAY_COORDS_COMPONENT);
  		}

  		return true;
  	}

	public int getDistanceToBiome(Player player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}

}
