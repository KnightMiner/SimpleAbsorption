package knightminer.simpleabsorption;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;

/** Capability handling absorption NBT storage */
public class AbsorptionCapability implements Capability.IStorage<AbsorptionHandler> {

	/** Capability ID */
	private static final ResourceLocation ID = new ResourceLocation(SimpleAbsorption.MOD_ID, "absorption_handler");
	/** Capability type */
	@CapabilityInject(AbsorptionHandler.class)
	public static Capability<AbsorptionHandler> CAPABILITY = null;
	/** Logic to run for the absorption handler */
	private static final NonNullConsumer<AbsorptionHandler> HANDLER_CONSUMER =  AbsorptionHandler::playerTick;

	/** Registers the event handlers and the capability */
	public static void init() {
		CapabilityManager.INSTANCE.register(AbsorptionHandler.class, new AbsorptionCapability(), () -> new AbsorptionHandler(null));
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, AbsorptionCapability::attachCapability);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerTickEvent.class, AbsorptionCapability::playerTick);
	}

	/** Event listener to attach the capability */
	private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(ID, new AbsorptionHandler((PlayerEntity) event.getObject()));
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

	@Nullable
	@Override
	public INBT writeNBT(Capability<AbsorptionHandler> capability, AbsorptionHandler instance, Direction side) {
		return null;
	}

	@Override
	public void readNBT(Capability<AbsorptionHandler> capability, AbsorptionHandler instance, Direction side, INBT nbt) {}
}
