package com.chaosthedude.naturescompass.items;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.gui.NaturesCompassScreen;
import com.chaosthedude.naturescompass.network.RequestSyncPacket;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NaturesCompassItem extends Item {

	public static final String NAME = "naturescompass";

	public NaturesCompassItem() {
		super(new Properties().maxStackSize(1).group(ItemGroup.TOOLS));
		setRegistryName(NAME);

		addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
			@OnlyIn(Dist.CLIENT)
			private double rotation;
			@OnlyIn(Dist.CLIENT)
			private double rota;
			@OnlyIn(Dist.CLIENT)
			private long lastUpdateTick;

			@OnlyIn(Dist.CLIENT)
			@Override
			public float call(ItemStack stack, World world, LivingEntity entityLiving) {
				if (entityLiving == null && !stack.isOnItemFrame()) {
					return 0.0F;
				} else {
					final boolean entityExists = entityLiving != null;
					final Entity entity = (Entity) (entityExists ? entityLiving : stack.getItemFrame());
					if (world == null) {
						world = entity.world;
					}

					double rotation = entityExists ? (double) entity.rotationYaw : getFrameRotation((ItemFrameEntity) entity);
					rotation = rotation % 360.0D;
					double adjusted = Math.PI - ((rotation - 90.0D) * 0.01745329238474369D - getAngle(world, entity, stack));

					if (entityExists) {
						adjusted = wobble(world, adjusted);
					}

					final float f = (float) (adjusted / (Math.PI * 2D));
					return MathHelper.positiveModulo(f, 1.0F);
				}
			}

			@OnlyIn(Dist.CLIENT)
			private double wobble(World world, double amount) {
				if (world.getGameTime() != lastUpdateTick) {
					lastUpdateTick = world.getGameTime();
					double d0 = amount - rotation;
					d0 = d0 % (Math.PI * 2D);
					d0 = MathHelper.clamp(d0, -1.0D, 1.0D);
					rota += d0 * 0.1D;
					rota *= 0.8D;
					rotation += rota;
				}

				return rotation;
			}

			@OnlyIn(Dist.CLIENT)
			private double getFrameRotation(ItemFrameEntity itemFrame) {
				return (double) MathHelper.wrapDegrees(180 + itemFrame.getHorizontalFacing().getHorizontalIndex() * 90);
			}

			@OnlyIn(Dist.CLIENT)
			private double getAngle(World world, Entity entity, ItemStack stack) {
				BlockPos pos;
				if (getState(stack) == CompassState.FOUND) {
					pos = new BlockPos(getFoundBiomeX(stack), 0, getFoundBiomeZ(stack));
				} else {
					pos = world.getSpawnPoint();
				}

				return Math.atan2((double) pos.getZ() - entity.posZ, (double) pos.getX() - entity.posX);
			}
		});
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			if (world.isRemote) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				NaturesCompass.network.sendToServer(new RequestSyncPacket());
				Minecraft.getInstance().displayGuiScreen(new NaturesCompassScreen(world, player, stack, (NaturesCompassItem) stack.getItem(), NaturesCompass.allowedBiomes));
			}
		} else {
			setState(player.getHeldItem(hand), null, CompassState.INACTIVE, player);
		}

		return new ActionResult<ItemStack>(ActionResultType.PASS, player.getHeldItem(hand));
	}

	public void searchForBiome(World world, PlayerEntity player, int biomeID, BlockPos pos, ItemStack stack) {
		setSearching(stack, biomeID, player);
		BiomeUtils.searchForBiome(world, player, stack, BiomeUtils.getBiomeForID(biomeID), pos);
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, int biomeID, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("BiomeID", biomeID);
			stack.getTag().putInt("State", CompassState.SEARCHING.getID());
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

	public void setBiomeID(ItemStack stack, int biomeID, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("BiomeID", biomeID);
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

	public int getBiomeID(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("BiomeID");
		}

		return -1;
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

}
