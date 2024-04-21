package com.chaosthedude.naturescompass;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.naturescompass.client.ClientEventHandler;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.CompassSearchPacket;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.util.CompassState;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

@Mod(NaturesCompass.MODID)
public class NaturesCompass {

	public static final String MODID = "naturescompass";

	public static final PermissionNode<Boolean> TELEPORT_PERMISSION = new PermissionNode<>(MODID, "naturescompass.teleport", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> false);

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static SimpleChannel network;
	public static NaturesCompassItem naturesCompass;

	public static boolean canTeleport;
	public static List<ResourceLocation> allowedBiomes;
	public static ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys;

	public static NaturesCompass instance;

	public NaturesCompass() {
		instance = this;

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::buildCreativeTabContents);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
		});

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.GENERAL_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void preInit(FMLCommonSetupEvent event) {
		network = ChannelBuilder.named(new ResourceLocation(NaturesCompass.MODID, NaturesCompass.MODID)).networkProtocolVersion(1).optionalClient().clientAcceptedVersions(Channel.VersionTest.exact(1)).simpleChannel();

		// Server packets
		network.messageBuilder(CompassSearchPacket.class).encoder(CompassSearchPacket::toBytes).decoder(CompassSearchPacket::new).consumerMainThread(CompassSearchPacket::handle).add();
		network.messageBuilder(TeleportPacket.class).encoder(TeleportPacket::toBytes).decoder(TeleportPacket::new).consumerMainThread(TeleportPacket::handle).add();

		// Client packet
		network.messageBuilder(SyncPacket.class).encoder(SyncPacket::toBytes).decoder(SyncPacket::new).consumerMainThread(SyncPacket::handle).add();

		allowedBiomes = new ArrayList<ResourceLocation>();
		dimensionKeysForAllowedBiomeKeys = ArrayListMultimap.create();
	}

	private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(new ItemStack(naturesCompass));
		}
	}

	@SubscribeEvent
	public void registerNodes(PermissionGatherEvent.Nodes event) {
		event.addNodes(TELEPORT_PERMISSION);
	}

	@OnlyIn(Dist.CLIENT)
	public void clientInit(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

		event.enqueueWork(() -> {
			ItemProperties.register(naturesCompass, new ResourceLocation("angle"), new ClampedItemPropertyFunction() {
				@OnlyIn(Dist.CLIENT)
				private double rotation;
				@OnlyIn(Dist.CLIENT)
				private double rota;
				@OnlyIn(Dist.CLIENT)
				private long lastUpdateTick;

				@OnlyIn(Dist.CLIENT)
				@Override
				public float unclampedCall(ItemStack stack, ClientLevel world, LivingEntity entityLiving, int seed) {
					if (entityLiving == null && !stack.isFramed()) {
						return 0.0F;
					} else {
						final boolean entityExists = entityLiving != null;
						final Entity entity = (Entity) (entityExists ? entityLiving : stack.getFrame());
						if (world == null && entity.level() instanceof ClientLevel) {
							world = (ClientLevel) entity.level();
						}

						double rotation = entityExists ? (double) entity.getYRot() : getFrameRotation((ItemFrame) entity);
						rotation = rotation % 360.0D;
						double adjusted = Math.PI - ((rotation - 90.0D) * 0.01745329238474369D - getAngle(world, entity, stack));

						if (entityExists) {
							adjusted = wobble(world, adjusted);
						}

						final float f = (float) (adjusted / (Math.PI * 2D));
						return Mth.positiveModulo(f, 1.0F);
					}
				}

				@OnlyIn(Dist.CLIENT)
				private double wobble(ClientLevel world, double amount) {
					if (world.getGameTime() != lastUpdateTick) {
						lastUpdateTick = world.getGameTime();
						double d0 = amount - rotation;
						d0 = Mth.positiveModulo(d0 + Math.PI, Math.PI * 2D) - Math.PI;
						d0 = Mth.clamp(d0, -1.0D, 1.0D);
						rota += d0 * 0.1D;
						rota *= 0.8D;
						rotation += rota;
					}

					return rotation;
				}

				@OnlyIn(Dist.CLIENT)
				private double getFrameRotation(ItemFrame itemFrame) {
					Direction direction = itemFrame.getDirection();
					int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
					return (double) Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + itemFrame.getRotation() * 45 + i);
				}

				@OnlyIn(Dist.CLIENT)
				private double getAngle(ClientLevel world, Entity entity, ItemStack stack) {
					if (stack.getItem() == naturesCompass) {
						NaturesCompassItem compassItem = (NaturesCompassItem) stack.getItem();
						BlockPos pos;
						if (compassItem.getState(stack) == CompassState.FOUND) {
							pos = new BlockPos(compassItem.getFoundBiomeX(stack), 0, compassItem.getFoundBiomeZ(stack));
						} else {
							pos = world.getSharedSpawnPos();
						}
						return Math.atan2((double) pos.getZ() - entity.position().z(), (double) pos.getX() - entity.position().x());
					}
					return 0.0D;
				}
			});
		});
	}

}
