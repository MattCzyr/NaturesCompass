package com.chaosthedude.naturescompass.items;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.network.PacketRequestSync;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.EnumCompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.SearchResult;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class ItemNaturesCompass extends Item {

	public static final String NAME = "NaturesCompass";

	private IIcon[] icons = new IIcon[32];
	@SideOnly(Side.CLIENT)
	private double rotation;
	@SideOnly(Side.CLIENT)
	private double rota;
	@SideOnly(Side.CLIENT)
	private long lastUpdateTick;

	public ItemNaturesCompass() {
		super();

		setCreativeTab(CreativeTabs.tabTools);
		setUnlocalizedName(NaturesCompass.MODID + "." + NAME);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!player.isSneaking()) {
			if (world.isRemote) {
				NaturesCompass.network.sendToServer(new PacketRequestSync());
			}
			player.openGui(NaturesCompass.instance, 0, world, 0, 0, 0);
		} else {
			setState(stack, EnumCompassState.INACTIVE, player);
		}

		return stack;
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		for (int i = 0; i < 32; i++) {
			String index = String.valueOf(i);
			if (index.length() < 2) {
				index = "0" + index;
			}
			icons[i] = iconRegister.registerIcon("naturescompass:natures_compass_" + index);
		}

		itemIcon = iconRegister.registerIcon("naturescompass:natures_compass_00");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass) {
		final Minecraft mc = Minecraft.getMinecraft();
		final World world = mc.theWorld;
		final EntityPlayer player = mc.thePlayer;

		if (world != null && player != null && stack != null) {
			return icons[(int) apply(stack, world, player)];
		}

		return itemIcon;
	}

	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	public void searchForBiome(World world, EntityPlayer player, int biomeID, int x, int z, ItemStack stack) {
		setState(stack, EnumCompassState.SEARCHING, player);
		setBiomeID(stack, biomeID, player);
		final SearchResult result = BiomeUtils.searchForBiome(world, stack, BiomeGenBase.getBiome(biomeID), x, z);
		if (result.found()) {
			setFound(stack, result.getX(), result.getZ(), player);
		} else {
			setNotFound(stack, player, result.getRadius());
		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != EnumCompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, int biomeID, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", EnumCompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, int x, int z, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", EnumCompassState.FOUND.getID());
			stack.getTagCompound().setInteger("FoundX", x);
			stack.getTagCompound().setInteger("FoundZ", z);
		}
	}

	public void setNotFound(ItemStack stack, EntityPlayer player, int searchRadius) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", EnumCompassState.NOT_FOUND.getID());
			stack.getTagCompound().setInteger("SearchRadius", searchRadius);
		}
	}

	public void setInactive(ItemStack stack, EntityPlayer player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTagCompound().setInteger("State", EnumCompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, EnumCompassState state, EntityPlayer player) {
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

	public String getBiomeName(ItemStack stack) {
		return BiomeUtils.getBiomeName(getBiomeID(stack));
	}

	public int getDistanceToBiome(EntityPlayer player, ItemStack stack) {
		return (int) player.getDistance(getFoundBiomeX(stack), player.posY, getFoundBiomeZ(stack));
	}

	@SideOnly(Side.CLIENT)
	public int apply(ItemStack stack, World world, EntityLivingBase entityLiving) {
		if (entityLiving == null && !stack.isOnItemFrame()) {
			return 0;
		} else {
			final boolean entityExists = entityLiving != null;
			final Entity entity = (Entity) (entityExists ? entityLiving : stack.getItemFrame());
			if (world == null) {
				world = entity.worldObj;
			}

			double rotation = entityExists ? (double) entity.rotationYaw : getFrameRotation((EntityItemFrame) entity);
			rotation = rotation % 360.0D;
			double adjusted = Math.PI - ((rotation - 90.0D) * 0.01745329238474369D - getAngle(world, entity, stack));

			if (entityExists) {
				adjusted = wobble(world, adjusted);
			}

			final float f = ((float) (adjusted / (Math.PI * 2D)) % 1.0F + 1.0F % 1.0F);
			return MathHelper.clamp_int(Math.round(((f * 32) + 16) % 32), 0, 31);
		}
	}

	@SideOnly(Side.CLIENT)
	private double wobble(World world, double amount) {
		if (world.getTotalWorldTime() != lastUpdateTick) {
			lastUpdateTick = world.getTotalWorldTime();
			double d0 = amount - rotation;
			d0 = d0 % (Math.PI * 2D);
			d0 = MathHelper.clamp_double(d0, -1.0D, 1.0D);
			rota += d0 * 0.1D;
			rota *= 0.8D;
			rotation += rota;
		}

		return rotation;
	}

	@SideOnly(Side.CLIENT)
	private int clampAngle(int angle) {
		angle = angle % 360;

		if (angle >= 180) {
			angle -= 360;
		}

		if (angle < -180) {
			angle += 360;
		}

		return angle;
	}

	@SideOnly(Side.CLIENT)
	private double getFrameRotation(EntityItemFrame itemFrame) {
		return (double) clampAngle(180 + itemFrame.hangingDirection * 90); // itemFrame.facingDirection.getHorizontalIndex()
																			// *
																			// 90);
	}

	@SideOnly(Side.CLIENT)
	private double getAngle(World world, Entity entity, ItemStack stack) {
		ChunkCoordinates pos;
		if (getState(stack) == EnumCompassState.FOUND) {
			pos = new ChunkCoordinates(getFoundBiomeX(stack), 0, getFoundBiomeZ(stack));
		} else {
			pos = world.getSpawnPoint();
		}

		return Math.atan2((double) pos.posZ - entity.posZ, (double) pos.posX - entity.posX);
	}

}
