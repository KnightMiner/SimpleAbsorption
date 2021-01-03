package knightminer.simpleabsorption;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Consumer;

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
		MinecraftForge.EVENT_BUS.register(AbsorptionHandler.class);
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

		addAttributes(EntityType.PLAYER, builder -> {
			builder.createMutableAttribute(absorptionMax);
			builder.createMutableAttribute(absorptionEfficiency);

		});
	}

	/**
	 * Adds attributes to an entity type
	 * @param type     Entity type
	 * @param builder  Consumer for builder to add attributes
	 */
	@SuppressWarnings("SameParameterValue")
	private static void addAttributes(EntityType<? extends LivingEntity> type, Consumer<MutableAttribute> builder) {
		AttributeModifierMap.MutableAttribute newAttrs = AttributeModifierMap.createMutableAttribute();
		if (GlobalEntityTypeAttributes.doesEntityHaveAttributes(type)) {
			newAttrs.attributeMap.putAll(GlobalEntityTypeAttributes.getAttributesForEntity(type).attributeMap);
		}
		builder.accept(newAttrs);
		GlobalEntityTypeAttributes.put(type, newAttrs.create());
	}
}
