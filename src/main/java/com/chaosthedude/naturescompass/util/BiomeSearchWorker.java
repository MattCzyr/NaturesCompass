package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.WorldWorkerManager;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {
	
	public final int sampleSpace;
	public final int maxDistance;
	public World world;
	public Biome biome;
	public BlockPos startPos;
	public int samples;
	public int nextLength;
	public EnumFacing direction;
	public ItemStack stack;
	public EntityPlayer player;
	public int x;
	public int z;
	public int length;
	public boolean finished;
	
	public BiomeSearchWorker(World world, EntityPlayer player, ItemStack stack, Biome biome, int radius, BlockPos startPos) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.biome = biome;
		this.startPos = startPos;
		x = startPos.getX();
		z = startPos.getZ();
		sampleSpace = ConfigHandler.sampleSpaceModifier * BiomeUtils.getBiomeSize(world);
		maxDistance = radius;
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = EnumFacing.UP;
		finished = false;
	}
	
	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (maxDistance > 0 && sampleSpace > 0) {
				NaturesCompass.logger.info("Starting search: " + sampleSpace + " sample space, " + maxDistance + " max distance");
				WorldWorkerManager.addWorker(this);
			} else {
				finish(false);
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() < maxDistance && samples < ConfigHandler.maxSamples;
	}

	@Override
	public boolean doWork() {
		if (hasWork()) {
			if (direction == EnumFacing.NORTH) {
				z -= sampleSpace;
			} else if (direction == EnumFacing.EAST) {
				x += sampleSpace;
			} else if (direction == EnumFacing.SOUTH) {
				z += sampleSpace;
			} else if (direction == EnumFacing.WEST) {
				x -= sampleSpace;
			}

			final BlockPos pos = new BlockPos(x, world.getHeight(), z);
			final Biome biomeAtPos = world.getBiomeForCoordsBody(pos);
			if (biomeAtPos == biome) {
				finish(true);
				return false;
			}

			samples++;
			length += sampleSpace;
			if (length >= nextLength) {
				if (direction != EnumFacing.UP) {
					nextLength += sampleSpace;
					direction = direction.rotateY();
				} else {
					direction = EnumFacing.NORTH;
				}
				length = 0;
			}
		}
		if (hasWork()) {
			return true;
		}
		finish(false);
		return false;
	}
	
	private void finish(boolean found) {
		if (found) {
		    	// if we found the biome, but it is outside of 4k blocks
		    	// then we want to display a hint instead of Found
		    	// 1. display the direction "You need to go East"
		    	// 2. get the name of the biome 4k away in that direction
		    	//    "You need to go East, past the Taiga"
		        // x and y are world position, not relative to the player

		    	double distanceToTarget = player.getDistance(x, player.posY, z);
			String hint = "The biome you seek ";
			
		    	if (distanceToTarget > 4096) {
		    	    // based on the angle, set the "hint"
		    	    // this will be passed out to the setFound method
		    	    double angle = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(x - player.posX, z - player.posZ))) + 180.0D;
			    
		    	    if (-45 <= angle && angle < 45) {
		    		hint += "lies to the north...";
		    	    } else if (45 <= angle && angle < 135) {
		    		System.out.println("West");
		    		hint += "lies to the west...";
		    	    } else if (135 <= angle && angle < 225) {
		    		System.out.println("South");
		    		hint += "lies to the south...";
		    	    } else if (225 <= angle && angle < 315) {
		    		System.out.println("East");
		    		hint += "lies to the east...";
		    	    } else if (315 <= angle && angle < 405) {
		    		System.out.println("North");
		    		hint += "lies to the north...";
		    	    }
		    	    
		    	    // get the half-way point between player and x, z
		    	    double halfwayX = (x - player.posX) / 2 + player.posX;
		    	    double halfwayZ = (z - player.posZ) / 2 + player.posZ;
		    	    
		    	    final BlockPos pos = new BlockPos(halfwayX, world.getHeight(), halfwayZ);
		    	    final Biome biomeAtPos = world.getBiomeForCoordsBody(pos);
		    	    
		    	    hint += " beyond the " + biomeAtPos.getBiomeName();
		    	 
		    	} else {
		    	    hint += "is within " + maxDistance + " blocks.";
		    	}
		    
			NaturesCompass.logger.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
			((ItemNaturesCompass) stack.getItem()).setFound(stack, x, z, samples, maxDistance, player, hint);
		} else {
		    	String hint = "";
		    	
		    	if (maxDistance > 4096) {
			    hint = "To search for such a place is a fool's errand... it may not even exist!";
		    	} else {
			    hint = "The biome you seek is further than " + maxDistance + " blocks away...";
		    	}
		    
			NaturesCompass.logger.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
			((ItemNaturesCompass) stack.getItem()).setNotFound(stack, player, maxDistance, samples, hint);
		}
		finished = true;
	}
	
	private int getRadius() {
		return (int) startPos.getDistance(x, startPos.getY(), z);
	}

}
