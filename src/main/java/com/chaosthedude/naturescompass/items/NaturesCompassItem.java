package com.chaosthedude.naturescompass.items;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.utils.PlayerUtils;
import com.chaosthedude.naturescompass.workers.BiomeSearchWorker;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
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
        super(new FabricItemSettings().maxCount(1));
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
				final boolean hasInfiniteXp = player.isCreative();
				final List<Identifier> allowedBiomeIDs = BiomeUtils.getAllowedBiomeIDs(world);
				final java.util.Map<Identifier, Integer> xpLevels = BiomeUtils.getXpLevelsForAllowedBiomes(allowedBiomeIDs);
				ServerPlayNetworking.send(serverPlayer, SyncPacket.ID, new SyncPacket(canTeleport, maxNextSearches, hasInfiniteXp, allowedBiomeIDs, xpLevels, BiomeUtils.getGeneratingDimensionsForAllowedBiomes(serverWorld, allowedBiomeIDs)));
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

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		if (max > 0) {
			int damage = getCompassDamage(stack);
			return damage > 0;
		}
		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		if (max > 0) {
			int damage = getCompassDamage(stack);
			return MathHelper.clamp(Math.round(13.0f * (1.0f - (float) damage / max)), 0, 13);
		}
		return 13;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		int damage = getCompassDamage(stack);
		float f = max > 0 ? (float) damage / max : 0.0f;
		return MathHelper.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
	}

	public void searchForBiome(ServerWorld world, PlayerEntity player, Identifier biomeID, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			setSearching(stack, biomeID, player);

			if (worker != null) {
				worker.stop();
			}
			List<BlockPos> prevPos = new ArrayList<BlockPos>();
			worker = new BiomeSearchWorker(world, player, stack, biomeID, pos, prevPos);
			worker.start();

			int xpLevels = BiomeUtils.getXpLevelsForBiome(biomeID);
			if (!player.isCreative() && xpLevels > 0) {
				player.addExperienceLevels(-xpLevels);
			}
		}
	}

	public void searchForNextBiome(ServerWorld world, PlayerEntity player, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			if (ItemUtils.verifyNBT(stack) && stack.getNbt().contains("BiomeID") && stack.getNbt().contains("PrevPos")) {
				Identifier biomeID = new Identifier(stack.getNbt().getString("BiomeID"));
				List<BlockPos> prevPos = readPrevPos(stack);

				setSearching(stack, biomeID, player);

				if (worker != null) {
					worker.stop();
				}
				worker = new BiomeSearchWorker(world, player, stack, biomeID, pos, prevPos);
				worker.start();

				int xpLevels = BiomeUtils.getXpLevelsForBiome(biomeID);
				if (!player.isCreative() && xpLevels > 0) {
					player.addExperienceLevels(-xpLevels);
				}
			}
		}
	}

	public void succeed(ItemStack stack, PlayerEntity player, Identifier biomeID, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", CompassState.FOUND.getID());
			stack.getNbt().putString("BiomeID", biomeID.toString());
			stack.getNbt().putInt("FoundX", x);
			stack.getNbt().putInt("FoundZ", z);
			writePrevPos(stack, prevPos);
			stack.getNbt().putInt("Samples", samples);
			stack.getNbt().putBoolean("DisplayCoordinates", displayCoordinates);
			damageCompass(stack);
		}
		worker = null;
	}

	public void fail(ItemStack stack, PlayerEntity player, Identifier biomeID, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getNbt().putString("BiomeID", biomeID.toString());
			stack.getNbt().putInt("SearchRadius", searchRadius);
			stack.getNbt().putInt("Samples", samples);
		}
		worker = null;
	}

	public boolean isBroken(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		return max > 0 && getCompassDamage(stack) >= max;
	}

	private void damageCompass(ItemStack stack) {
		int max = NaturesCompassConfig.compassDurability;
		if (max > 0 && ItemUtils.verifyNBT(stack)) {
			int damage = getCompassDamage(stack) + 1;
			stack.getNbt().putInt("CompassDamage", damage);
		}
	}

	private int getCompassDamage(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getNbt().contains("CompassDamage")) {
			return stack.getNbt().getInt("CompassDamage");
		}
		return 0;
	}

	private void writePrevPos(ItemStack stack, List<BlockPos> prevPos) {
		if (ItemUtils.verifyNBT(stack)) {
			NbtList listTag = new NbtList();
			for (BlockPos pos : prevPos) {
				NbtCompound posTag = new NbtCompound();
				posTag.putInt("X", pos.getX());
				posTag.putInt("Y", pos.getY());
				posTag.putInt("Z", pos.getZ());
				listTag.add(posTag);
			}
			stack.getNbt().put("PrevPos", listTag);
		}
	}

	private List<BlockPos> readPrevPos(ItemStack stack) {
		List<BlockPos> prevPos = new ArrayList<BlockPos>();
		if (ItemUtils.verifyNBT(stack) && stack.getNbt().contains("PrevPos", NbtElement.LIST_TYPE)) {
			NbtList listTag = stack.getNbt().getList("PrevPos", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < listTag.size(); i++) {
				NbtCompound posTag = listTag.getCompound(i);
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

	public void setSearching(ItemStack stack, Identifier biomeID, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putString("BiomeID", biomeID.toString());
			stack.getNbt().putInt("State", CompassState.SEARCHING.getID());
			stack.getNbt().putInt("SearchRadius", 0);
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", CompassState.FOUND.getID());
			stack.getNbt().putInt("FoundX", x);
			stack.getNbt().putInt("FoundZ", z);
			stack.getNbt().putInt("Samples", samples);
		}
	}

	public void setNotFound(ItemStack stack, PlayerEntity player, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getNbt().putInt("SearchRadius", searchRadius);
			stack.getNbt().putInt("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", state.getID());
		}
	}

	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putBoolean("DisplayCoordinates", displayPosition);
		}
	}

	public void setFoundBiomeX(ItemStack stack, int x, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("FoundX", x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("FoundZ", z);
		}
	}

	public void setBiomeID(ItemStack stack, Identifier biomeID, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putString("BiomeID", biomeID.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("Samples", samples);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return CompassState.fromID(stack.getNbt().getInt("State"));
		}

		return null;
	}

	public int getFoundBiomeX(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("FoundX");
		}

		return 0;
	}

	public int getFoundBiomeZ(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("FoundZ");
		}

		return 0;
	}

	public Identifier getBiomeID(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return new Identifier(stack.getNbt().getString("BiomeID"));
		}

		return new Identifier("");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("SearchRadius");
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("Samples");
		}

		return -1;
	}

	public int getDistanceToBiome(PlayerEntity player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}

	public boolean shouldDisplayCoordinates(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getNbt().contains("DisplayCoordinates")) {
			return stack.getNbt().getBoolean("DisplayCoordinates");
		}

		return true;
	}

}
