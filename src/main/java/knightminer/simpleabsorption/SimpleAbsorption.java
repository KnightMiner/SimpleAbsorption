package knightminer.simpleabsorption;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

@Mod(SimpleAbsorption.MOD_ID)
public class SimpleAbsorption {
	// IDs
	protected static final String MOD_ID = "simple_absorption";
	private static final String ENCHANT_ID = MOD_ID + ":absorption";

	@ObjectHolder(ENCHANT_ID)
	public static Enchantment ABSORPTION;

	public SimpleAbsorption() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Enchantment.class, SimpleAbsorption::registerEnchants);
		MinecraftForge.EVENT_BUS.addListener(AbsorptionHandler::playerTick);
		MinecraftForge.EVENT_BUS.addListener(SimpleAbsorption::itemTooltip);
	}

	/** Registers mod applicable enchants */
	private static void registerEnchants(Register<Enchantment> event) {
		Enchantment absorption = new AbsorptionEnchantment(Rarity.RARE, EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET);
		absorption.setRegistryName(ENCHANT_ID);
		event.getRegistry().register(absorption);
	}

	private static void itemTooltip(ItemTooltipEvent event) {
		int goldBoost = Config.GOLD_ABSORPTION.get();
		if (goldBoost <= 0) {
			return;
		}
		// add tooltip to golden armor
		Item item = event.getItemStack().getItem();
		if (item instanceof ArmorItem && ((ArmorItem)item).getArmorMaterial() == ArmorMaterial.GOLD) {
			event.getToolTip().add(new TranslationTextComponent("tooltip.simple_absorption.golden_armor", goldBoost).applyTextStyle(TextFormatting.BLUE));
		}
	}
}
