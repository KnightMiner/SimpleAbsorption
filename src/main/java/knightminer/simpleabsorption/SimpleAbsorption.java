package knightminer.simpleabsorption;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("WeakerAccess")
@Mod(SimpleAbsorption.MOD_ID)
public class SimpleAbsorption {
	// IDs
	protected static final String MOD_ID = "simple_absorption";
	private static final String ENCHANT_ID = "absorption";
	private static final String ATTRIBUTE_MAX_ID = "absorption_max";
	private static final String ATTRIBUTE_EFFICIENCY_ID = "absorption_efficiency";

	private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MOD_ID);
	private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MOD_ID);

	public static final RegistryObject<Enchantment> ABSORPTION = ENCHANTMENTS.register(ENCHANT_ID, () -> new AbsorptionEnchantment(Rarity.RARE, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
	public static final RegistryObject<Attribute> ABSORPTION_MAX = ATTRIBUTES.register(ATTRIBUTE_MAX_ID, () -> new RangedAttribute(MOD_ID + "." + ATTRIBUTE_MAX_ID, 0, 0, 100).setSyncable(true));
	public static final RegistryObject<Attribute> ABSORPTION_EFFICIENCY = ATTRIBUTES.register(ATTRIBUTE_EFFICIENCY_ID, () -> new RangedAttribute(MOD_ID + "." + ATTRIBUTE_EFFICIENCY_ID, 0, 0, 20).setSyncable(true));

	public SimpleAbsorption(FMLJavaModLoadingContext ctx) {
		IEventBus modBus = ctx.getModEventBus();
		ctx.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
		modBus.addListener(SimpleAbsorption::setupAttributes);
		AbsorptionCapability.init(modBus);
		MinecraftForge.EVENT_BUS.register(AbsorptionSources.class);

		ENCHANTMENTS.register(modBus);
		ATTRIBUTES.register(modBus);
	}

	/** Adds attributes to the player */
	private static void setupAttributes(EntityAttributeModificationEvent event) {
		if (event.getTypes().contains(EntityType.PLAYER)) {
			event.add(EntityType.PLAYER, ABSORPTION_MAX.get());
			event.add(EntityType.PLAYER, ABSORPTION_EFFICIENCY.get());
		}
	}
}
