package com.chaosthedude.naturescompass.items;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.network.PacketRequestSync;
import com.chaosthedude.naturescompass.util.BiomeSearchWorker;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.EnumCompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemNaturesCompass extends Item {

	public static final String NAME = "NaturesCompass";

	public ItemNaturesCompass() {
		super();

		setCreativeTab(CreativeTabs.TOOLS);
		setUnlocalizedName(NaturesCompass.MODID + "." + NAME);

		addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			double rotation;
			@SideOnly(Side.CLIENT)
			double rota;
			@SideOnly(Side.CLIENT)
			long lastUpdateTick;

			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, World world, EntityLivingBase entityLiving) {
				if (entityLiving == null && !stack.isOnItemFrame()) {
					return 0.0F;
				} else {
					final boolean entityExists = entityLiving != null;
					final Entity entity = (Entity) (entityExists ? entityLiving : stack.getItemFrame());
					if (world == null) {
						world = entity.world;
					}

					double rotation = entityExists ? (double) entity.rotationYaw : getFrameRotation((EntityItemFrame) entity);
					rotation = rotation % 360.0D;
					double adjusted = Math.PI - ((rotation - 90.0D) * 0.01745329238474369D - getAngle(world, entity, stack));

					if (entityExists) {
						adjusted = wobble(world, adjusted);
					}

					final float f = (float) (adjusted / (Math.PI * 2D));
					return MathHelper.positiveModulo(f, 1.0F);
				}
			}

			@SideOnly(Side.CLIENT)
			private double wobble(World world, double amount) {
				if (world.getTotalWorldTime() != lastUpdateTick) {
					lastUpdateTick = world.getTotalWorldTime();
					double d0 = amount - rotation;
					d0 = d0 % (Math.PI * 2D);
					d0 = MathHelper.clamp(d0, -1.0D, 1.0D);
					rota += d0 * 0.1D;
					rota *= 0.8D;
					rotation += rota;
				}

				return rotation;
			}

			@SideOnly(Side.CLIENT)
			private double getFrameRotation(EntityItemFrame itemFrame) {
				return (double) MathHelper.wrapDegrees(180 + itemFrame.facingDirection.getHorizontalIndex() * 90);
			}

			@SideOnly(Side.CLIENT)
			private double getAngle(World world, Entity entity, ItemStack stack) {
			        double angle = entity.rotationYaw < 360
			        	? 270.0D
			        	: -90.0D;
			        	
			        return Math.toRadians(angle);
			}
		});
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!player.isSneaking()) {
			if (world.isRemote) {
				NaturesCompass.network.sendToServer(new PacketRequestSync());
			}
			player.openGui(NaturesCompass.instance, 0, world, 0, 0, 0);
		} else {
			setState(player.getHeldItem(hand), null, EnumCompassState.INACTIVE, player);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	public void searchForBiome(World world, EntityPlayer player, int biomeID, int radius, BlockPos pos, ItemStack stack) {
		setSearching(stack, biomeID, player);
		BiomeSearchWorker worker = new BiomeSearchWorker(world, player, stack, Biome.getBiome(biomeID), radius, pos);
		worker.start();
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != EnumCompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, int biomeID, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("BiomeID", biomeID);
			stack.getTagCompound().setInteger("State", EnumCompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, int searchRadius, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", EnumCompassState.FOUND.getID());
			// here add a "Hint" to the tag compound
			stack.getTagCompound().setInteger("FoundX", x);
			stack.getTagCompound().setInteger("FoundZ", z);
			stack.getTagCompound().setInteger("SearchRadius", searchRadius);
			stack.getTagCompound().setInteger("Samples", samples);
		} 
	}

	public void setNotFound(ItemStack stack, EntityPlayer player, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", EnumCompassState.NOT_FOUND.getID());
			stack.getTagCompound().setInteger("SearchRadius", searchRadius);
			stack.getTagCompound().setInteger("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", EnumCompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, EnumCompassState state, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", state.getID());
		}
	}

	public void setFoundBiomeX(ItemStack stack, int x, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("FoundX", x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("FoundZ", z);
		}
	}

	public void setBiomeID(ItemStack stack, int biomeID, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("BiomeID", biomeID);
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("Samples", samples);
		}
	}

	public EnumCompassState getState(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return EnumCompassState.fromID(stack.getTagCompound().getInteger("State"));
		}

		return null;
	}

	public int getFoundBiomeX(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTagCompound().getInteger("FoundX");
		}

		return 0;
	}

	public int getFoundBiomeZ(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTagCompound().getInteger("FoundZ");
		}

		return 0;
	}

	public int getBiomeID(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTagCompound().getInteger("BiomeID");
		}

		return -1;
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTagCompound().getInteger("SearchRadius");
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTagCompound().getInteger("Samples");
		}

		return -1;
	}

	public String getBiomeName(ItemStack stack) {
		return BiomeUtils.getBiomeName(getBiomeID(stack));
	}

	public int getDistanceToBiome(EntityPlayer player, ItemStack stack) {
		return (int) player.getDistance(getFoundBiomeX(stack), player.posY, getFoundBiomeZ(stack));
	}

}
