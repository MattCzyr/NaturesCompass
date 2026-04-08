package com.chaosthedude.naturescompass.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;

public class NaturesCompassItem extends Item {

	public static final String NAME = "naturescompass";

	private BiomeSearchWorker worker;

	public NaturesCompassItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!player.isCrouching()) {
			if (isBroken(player.getItemInHand(hand))) {
				return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, player.getItemInHand(hand));
			}

			if (level.isClientSide()) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerLevel serverLevel = (ServerLevel) level;
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(serverPlayer.getServer(), player);
				final int maxNextSearches = ConfigHandler.GENERAL.maxNextSearches.get();
				final boolean hasInfiniteXp = player.getAbilities().instabuild;
				final List<ResourceLocation> allowedBiomeKeys = BiomeUtils.getAllowedBiomeKeys(level);
				final Map<ResourceLocation, Integer> xpLevels = BiomeUtils.getXpLevelsForAllowedBiomes(allowedBiomeKeys);
				final com.google.common.collect.ListMultimap<ResourceLocation, ResourceLocation> generatingDimensions = BiomeUtils.getGeneratingDimensionsForAllowedBiomes(serverLevel);
				NaturesCompass.network.sendTo(new SyncPacket(canTeleport, maxNextSearches, hasInfiniteXp, allowedBiomeKeys, xpLevels, generatingDimensions), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
	public boolean isBarVisible(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = getCompassDamage(stack);
			return damage > 0;
		}
		return false;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = getCompassDamage(stack);
			return Mth.clamp(Math.round(13.0f * (1.0f - (float) damage / max)), 0, 13);
		}
		return 13;
	}

	@Override
	public int getBarColor(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		int damage = getCompassDamage(stack);
		float f = max > 0 ? (float) damage / max : 0.0f;
		return Mth.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (getState(oldStack) == getState(newStack)) {
			return false;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	public void searchForBiome(ServerLevel level, Player player, ResourceLocation biomeKey, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			setSearching(stack, biomeKey, player);

			if (worker != null) {
				worker.stop();
			}
			List<BlockPos> prevPos = new ArrayList<BlockPos>();
			worker = new BiomeSearchWorker(level, player, stack, biomeKey, pos, prevPos);
			worker.start();

			int xpLevels = BiomeUtils.getXpLevelsForBiome(biomeKey);
			if (!player.getAbilities().instabuild && xpLevels > 0) {
				player.giveExperienceLevels(-xpLevels);
			}
		}
	}

	public void searchForNextBiome(ServerLevel level, Player player, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("BiomeKey") && stack.getTag().contains("PrevPos")) {
				ResourceLocation biomeKey = new ResourceLocation(stack.getTag().getString("BiomeKey"));
				List<BlockPos> prevPos = readPrevPos(stack);

				setSearching(stack, biomeKey, player);

				if (worker != null) {
					worker.stop();
				}
				worker = new BiomeSearchWorker(level, player, stack, biomeKey, pos, prevPos);
				worker.start();

				int xpLevels = BiomeUtils.getXpLevelsForBiome(biomeKey);
				if (!player.getAbilities().instabuild && xpLevels > 0) {
					player.giveExperienceLevels(-xpLevels);
				}
			}
		}
	}

	public void succeed(ItemStack stack, Player player, ResourceLocation biomeKey, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.FOUND.getID());
			stack.getTag().putString("BiomeKey", biomeKey.toString());
			stack.getTag().putInt("FoundX", x);
			stack.getTag().putInt("FoundZ", z);
			writePrevPos(stack, prevPos);
			stack.getTag().putInt("Samples", samples);
			stack.getTag().putBoolean("DisplayCoordinates", displayCoordinates);
			damageCompass(stack);
		}
		worker = null;
	}

	public void fail(ItemStack stack, Player player, ResourceLocation biomeKey, int radius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getTag().putString("BiomeKey", biomeKey.toString());
			stack.getTag().putInt("SearchRadius", radius);
			stack.getTag().putInt("Samples", samples);
		}
		worker = null;
	}

	public boolean isBroken(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		return max > 0 && getCompassDamage(stack) >= max;
	}

	private void damageCompass(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0 && ItemUtils.verifyNBT(stack)) {
			int damage = getCompassDamage(stack) + 1;
			stack.getTag().putInt("CompassDamage", damage);
		}
	}

	private int getCompassDamage(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("CompassDamage")) {
			return stack.getTag().getInt("CompassDamage");
		}
		return 0;
	}

	private void writePrevPos(ItemStack stack, List<BlockPos> prevPos) {
		if (ItemUtils.verifyNBT(stack)) {
			ListTag listTag = new ListTag();
			for (BlockPos pos : prevPos) {
				CompoundTag posTag = new CompoundTag();
				posTag.putInt("X", pos.getX());
				posTag.putInt("Y", pos.getY());
				posTag.putInt("Z", pos.getZ());
				listTag.add(posTag);
			}
			stack.getTag().put("PrevPos", listTag);
		}
	}

	private List<BlockPos> readPrevPos(ItemStack stack) {
		List<BlockPos> prevPos = new ArrayList<BlockPos>();
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("PrevPos", Tag.TAG_LIST)) {
			ListTag listTag = stack.getTag().getList("PrevPos", Tag.TAG_COMPOUND);
			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag posTag = listTag.getCompound(i);
				prevPos.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
			}
		}
		return prevPos;
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}
		return false;
	}

	public void setSearching(ItemStack stack, ResourceLocation biomeKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeKey", biomeKey.toString());
			stack.getTag().putInt("State", CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.FOUND.getID());
			stack.getTag().putInt("FoundX", x);
			stack.getTag().putInt("FoundZ", z);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setNotFound(ItemStack stack, Player player, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getTag().putInt("SearchRadius", searchRadius);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", state.getID());
		}
	}

	public void setFoundBiomeX(ItemStack stack, int x, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundX", x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundZ", z);
		}
	}

	public void setBiomeKey(ItemStack stack, ResourceLocation biomeKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeKey", biomeKey.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("Samples", samples);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return CompassState.fromID(stack.getTag().getInt("State"));
		}
		return null;
	}

	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putBoolean("DisplayCoordinates", displayPosition);
		}
	}

	public int getFoundBiomeX(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundX");
		}
		return 0;
	}

	public int getFoundBiomeZ(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundZ");
		}
		return 0;
	}

	public ResourceLocation getBiomeKey(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return new ResourceLocation(stack.getTag().getString("BiomeKey"));
		}
		return new ResourceLocation("");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("SearchRadius");
		}
		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("Samples");
		}
		return -1;
	}

	public int getDistanceToBiome(Player player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}

	public boolean shouldDisplayCoordinates(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("DisplayCoordinates")) {
			return stack.getTag().getBoolean("DisplayCoordinates");
		}
		return true;
	}

}
