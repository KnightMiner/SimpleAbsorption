package knightminer.simpleabsorption;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

@SuppressWarnings("WeakerAccess")
@Mod(SimpleAbsorption.MOD_ID)
public class SimpleAbsorption {
	// IDs
	protected static final String MOD_ID = "simple_absorption";
	private static final String ENCHANT_ID = MOD_ID + ":absorption";

	@ObjectHolder(ENCHANT_ID)
	public static Enchantment ABSORPTION;

	public SimpleAbsorption() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addGenericListener(Enchantment.class, SimpleAbsorption::registerEnchants);
		modBus.addListener(Config::configChanged);
		MinecraftForge.EVENT_BUS.addListener(AbsorptionHandler::playerTick);
	}

	/** Registers mod applicable enchants */
	private static void registerEnchants(Register<Enchantment> event) {
		Enchantment absorption = new AbsorptionEnchantment(Rarity.RARE, EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET);
		absorption.setRegistryName(ENCHANT_ID);
		event.getRegistry().register(absorption);
	}
}
