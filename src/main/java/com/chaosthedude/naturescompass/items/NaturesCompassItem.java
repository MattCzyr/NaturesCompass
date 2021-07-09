package com.chaosthedude.naturescompass.items;

import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.utils.PlayerUtils;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class NaturesCompassItem extends Item {
	
	public NaturesCompassItem() {
        super(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1));
    }
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			if (world.isClient) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				GuiWrapper.openGUI(world, player, stack);
			} else {
				final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				final boolean canTeleport = NaturesCompassConfig.allowTeleport && PlayerUtils.canTeleport(player);
				final List<Identifier> allowedBiomeIDs = BiomeUtils.getAllowedBiomeIDs(world);
				ServerPlayNetworking.send(serverPlayer, SyncPacket.ID, new SyncPacket(canTeleport, allowedBiomeIDs));
			}
		} else {
			setState(player.getStackInHand(hand), null, CompassState.INACTIVE, player);
		}
		return TypedActionResult.pass(player.getStackInHand(hand));
	}

	public void searchForBiome(World world, PlayerEntity player, Identifier biomeID, BlockPos pos, ItemStack stack) {
		setSearching(stack, biomeID, player);
		Optional<Biome> optionalBiome = BiomeUtils.getBiomeForIdentifier(world, biomeID);
 		if (optionalBiome.isPresent()) {
 			BiomeUtils.searchForBiome(world, player, stack, optionalBiome.get(), pos);
 		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, Identifier biomeID, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeID", biomeID.toString());
			stack.getTag().putInt("State", CompassState.SEARCHING.getID());
			stack.getTag().putInt("SearchRadius", 0);
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.FOUND.getID());
			stack.getTag().putInt("FoundX", x);
			stack.getTag().putInt("FoundZ", z);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setNotFound(ItemStack stack, PlayerEntity player, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getTag().putInt("SearchRadius", searchRadius);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", state.getID());
		}
	}
	
	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
  		if (ItemUtils.verifyNBT(stack)) {
  			stack.getTag().putBoolean("DisplayCoordinates", displayPosition);
  		}
  	}

	public void setFoundBiomeX(ItemStack stack, int x, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundX", x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundZ", z);
		}
	}

	public void setBiomeID(ItemStack stack, Identifier biomeID, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeID", biomeID.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, PlayerEntity player) {
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

	public Identifier getBiomeID(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return new Identifier(stack.getTag().getString("BiomeID"));
		}

		return new Identifier("");
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

	public int getDistanceToBiome(PlayerEntity player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}
	
	public boolean shouldDisplayCoordinates(ItemStack stack) {
  		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("DisplayCoordinates")) {
  			return stack.getTag().getBoolean("DisplayCoordinates");
  		}

  		return true;
  	}

}
