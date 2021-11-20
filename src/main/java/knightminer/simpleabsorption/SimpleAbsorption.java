package knightminer.simpleabsorption;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

@SuppressWarnings("WeakerAccess")
@Mod(SimpleAbsorption.MOD_ID)
public class SimpleAbsorption {
	// IDs
	protected static final String MOD_ID = "simple_absorption";
	private static final String ENCHANT_ID = MOD_ID + ":absorption";
	private static final String ATTRIBUTE_MAX_ID = MOD_ID + ":absorption_max";
	private static final String ATTRIBUTE_EFFICIENCY_ID = MOD_ID + ":absorption_efficiency";

	@ObjectHolder(ENCHANT_ID)
	public static Enchantment ABSORPTION;

	@ObjectHolder(ATTRIBUTE_MAX_ID)
	public static Attribute ABSORPTION_MAX;

	@ObjectHolder(ATTRIBUTE_EFFICIENCY_ID)
	public static Attribute ABSORPTION_EFFICIENCY;

	public SimpleAbsorption() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addGenericListener(Enchantment.class, SimpleAbsorption::registerEnchants);
		modBus.addGenericListener(Attribute.class, SimpleAbsorption::registerAttributes);
		modBus.addListener(Config::configChanged);
		modBus.addListener(SimpleAbsorption::setupAttributes);
		modBus.addListener(SimpleAbsorption::commonSetup);
	}

	/** General setup */
	private static void commonSetup(FMLCommonSetupEvent event) {
		AbsorptionCapability.init();
		MinecraftForge.EVENT_BUS.register(AbsorptionSources.class);
	}

	/** Registers mod applicable enchants */
	private static void registerEnchants(Register<Enchantment> event) {
		Enchantment absorption = new AbsorptionEnchantment(Rarity.RARE, EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET);
		absorption.setRegistryName(ENCHANT_ID);
		event.getRegistry().register(absorption);
	}

	/** Registers mod applicable attributes */
	private static void registerAttributes(Register<Attribute> event) {
		Attribute absorptionMax = new RangedAttribute("simple_absorption.absorption_max", 0, 0, 100).setShouldWatch(true).setRegistryName(ATTRIBUTE_MAX_ID);
		event.getRegistry().register(absorptionMax);
		Attribute absorptionEfficiency = new RangedAttribute("simple_absorption.absorption_efficiency", 0, 0, 20).setShouldWatch(true).setRegistryName(ATTRIBUTE_EFFICIENCY_ID);
		event.getRegistry().register(absorptionEfficiency);
	}

	/** Adds attributes to the player */
	private static void setupAttributes(EntityAttributeModificationEvent event) {
		if (event.getTypes().contains(EntityType.PLAYER)) {
			event.add(EntityType.PLAYER, ABSORPTION_MAX);
			event.add(EntityType.PLAYER, ABSORPTION_EFFICIENCY);
		}
	}
}
