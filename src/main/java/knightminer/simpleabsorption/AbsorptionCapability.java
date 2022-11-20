package knightminer.simpleabsorption;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Capability handling absorption NBT storage */
public class AbsorptionCapability {

	/** Capability ID */
	private static final ResourceLocation ID = new ResourceLocation(SimpleAbsorption.MOD_ID, "absorption_handler");
	/** Capability type */
	public final static Capability<AbsorptionHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	/** Logic to run for the absorption handler */
	private static final NonNullConsumer<AbsorptionHandler> HANDLER_CONSUMER =  AbsorptionHandler::playerTick;

	/** Registers the event handlers and the capability */
	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(AbsorptionCapability::registerCapability);
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, AbsorptionCapability::attachCapability);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerTickEvent.class, AbsorptionCapability::playerTick);
	}

	/** Event listener to register the capability */
	private static void registerCapability(RegisterCapabilitiesEvent event) {
		event.register(AbsorptionHandler.class);
	}

	/** Event listener to attach the capability */
	private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(ID, new AbsorptionHandler((Player) event.getObject()));
		}
	}

	/** Runs on player update to update absorption shield, internal event */
	private static void playerTick(PlayerTickEvent event) {
		// use phase.start so we run before the food timer resets
		if (event.side != LogicalSide.SERVER || event.phase != Phase.START) {
			return;
		}
		event.player.getCapability(CAPABILITY).ifPresent(HANDLER_CONSUMER);
	}
}
