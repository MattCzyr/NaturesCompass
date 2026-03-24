package com.chaosthedude.naturescompass.item;

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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            if (isBroken(player.getItemInHand(hand))) {
                return InteractionResult.PASS;
            }
            
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
			ItemStack stack = player.getItemInHand(hand);
			clearSearchData(stack);
			setCompassState(stack, CompassState.INACTIVE);
		}
		return InteractionResult.CONSUME;
	}

    @Override
    public boolean isBarVisible(ItemStack stack) {
        int max = NaturesCompassConfig.compassDurability;
        if (max > 0) {
            int damage = stack.getOrDefault(NaturesCompass.DAMAGE, 0);
            return damage > 0;
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = NaturesCompassConfig.compassDurability;
        if (max > 0) {
            int damage = stack.getOrDefault(NaturesCompass.DAMAGE, 0);
            return Math.clamp(Math.round(13.0f * (1.0f - (float) damage / max)), 0, 13);
        }
        return 13;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int max = NaturesCompassConfig.compassDurability;
        int damage = stack.getOrDefault(NaturesCompass.DAMAGE, 0);
        float f = max > 0 ? (float) damage / max : 0.0f;
        return Mth.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
    }

	public void searchForBiome(ServerLevel level, Player player, Identifier biomeId, BlockPos pos, ItemStack stack) {
        if (!isBroken(stack)) {
            search(stack, biomeId);

            if (worker != null) {
                worker.stop();
            }
            List<BlockPos> prevPos = new ArrayList<BlockPos>();
            worker = new BiomeSearchWorker(level, player, stack, biomeId, pos, prevPos);
            worker.start();

            int xpLevels = BiomeUtils.getXpLevelsForBiome(level, biomeId);
            if (!player.hasInfiniteMaterials() && xpLevels > 0) {
                player.giveExperienceLevels(-xpLevels);
            }
        }
	}

	public void searchForNextBiome(ServerLevel level, Player player, BlockPos pos, ItemStack stack) {
        if (!isBroken(stack)) {
			List<BlockPos> prevPos = stack.getOrDefault(NaturesCompass.PREV_POS, null);
			String biomeIdStr = stack.getOrDefault(NaturesCompass.BIOME_ID, null);
			if (prevPos != null && biomeIdStr != null) {
				Identifier biomeId = Identifier.parse(biomeIdStr);
                search(stack, biomeId);

                if (worker != null) {
                    worker.stop();
                }
                worker = new BiomeSearchWorker(level, player, stack, biomeId, pos, prevPos);
                worker.start();

                int xpLevels = BiomeUtils.getXpLevelsForBiome(level, biomeId);
                if (!player.hasInfiniteMaterials() && xpLevels > 0) {
                    player.giveExperienceLevels(-xpLevels);
                }
			}
		}
	}

	public void setCompassState(ItemStack stack, CompassState state) {
		stack.set(NaturesCompass.COMPASS_STATE, state.getID());
	}

	public CompassState getCompassState(ItemStack stack) {
        if (stack.has(NaturesCompass.COMPASS_STATE)) {
            return CompassState.fromID(stack.get(NaturesCompass.COMPASS_STATE));
        }
        return null;
	}

	public void succeed(ItemStack stack, Identifier biomeId, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		clearSearchData(stack);
		setCompassState(stack, CompassState.FOUND);
		stack.set(NaturesCompass.BIOME_ID, biomeId.toString());
		stack.set(NaturesCompass.FOUND_X, x);
		stack.set(NaturesCompass.FOUND_Z, z);
		stack.set(NaturesCompass.PREV_POS, prevPos);
		stack.set(NaturesCompass.SAMPLES, samples);
		stack.set(NaturesCompass.DISPLAY_COORDS, displayCoordinates);
		damageCompass(stack);
		worker = null;
	}

	private void damageCompass(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		if (max > 0) {
            int damage = stack.getOrDefault(NaturesCompass.DAMAGE, 0) + 1;
            stack.set(NaturesCompass.DAMAGE, damage);
        }
	}

	public boolean isBroken(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		return max > 0 && stack.getOrDefault(NaturesCompass.DAMAGE, 0) >= max;
	}

	public void fail(ItemStack stack, Identifier biomeId, int radius, int samples) {
		clearSearchData(stack);
		setCompassState(stack, CompassState.NOT_FOUND);
        stack.set(NaturesCompass.BIOME_ID, biomeId.toString());
		stack.set(NaturesCompass.SEARCH_RADIUS, radius);
		stack.set(NaturesCompass.SAMPLES, samples);
		worker = null;
	}

	private void search(ItemStack stack, Identifier biomeId) {
		clearSearchData(stack);
		setCompassState(stack, CompassState.SEARCHING);
		stack.set(NaturesCompass.BIOME_ID, biomeId.toString());
		stack.set(NaturesCompass.SAMPLES, 0);
		stack.set(NaturesCompass.SEARCH_RADIUS, 0);
	}

	private void clearSearchData(ItemStack stack) {
		stack.remove(NaturesCompass.COMPASS_STATE);
		stack.remove(NaturesCompass.BIOME_ID);
		stack.remove(NaturesCompass.FOUND_X);
		stack.remove(NaturesCompass.FOUND_Z);
		stack.remove(NaturesCompass.PREV_POS);
		stack.remove(NaturesCompass.SAMPLES);
		stack.remove(NaturesCompass.SEARCH_RADIUS);
		stack.remove(NaturesCompass.DISPLAY_COORDS);
	}

}
