package knightminer.simpleabsorption;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionExpiryEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** Logic adding absorption from all relevant sources */
public class AbsorptionSources {
	/** Generates a UUID map for all slot types from a string key */
	private static Map<EquipmentSlot,UUID> makeUUIDMap(String key) {
		Map<EquipmentSlot,UUID> map = new EnumMap<>(EquipmentSlot.class);
		for (EquipmentSlot type : EquipmentSlot.values()) {
			map.put(type, UUID.nameUUIDFromBytes((key + type.getName()).getBytes()));
		}
		return map;
	}

	/** UUID for potions */
	private static final UUID POTION_UUID = UUID.fromString("e7c88f6c-4d46-11eb-ae93-0242ac130002");

	/** Map of slot to UUID to ensure consistent removals */
	private static final Map<EquipmentSlot,UUID> ARMOR_ADD_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_armor_add");
	private static final Map<EquipmentSlot,UUID> ARMOR_MULTIPLY_TOTAL_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_armor_multiply_total");
	private static final Map<EquipmentSlot,UUID> ARMOR_MULTIPLY_BASE_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_armor_multiply_base");
	private static final Map<EquipmentSlot,UUID> EFFICIENCY_ADD_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_regen_add");
	private static final Map<EquipmentSlot,UUID> EFFICIENCY_MULTIPLY_TOTAL_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_efficiency_multiply_total");
	private static final Map<EquipmentSlot,UUID> EFFICIENCY_MULTIPLY_BASE_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_efficiency_multiply_base");

	/** Cached object for removing the potion attribute */
	private static final Multimap<Attribute, AttributeModifier> POTION_REMOVAL = ImmutableMultimap.of(SimpleAbsorption.ABSORPTION_MAX, new AttributeModifier(POTION_UUID, "simple_absorption_potion", 0, Operation.ADDITION));


	/* Absorption sources */

	/**
	 * Replaces an attribute on the item
	 * @param event        Event
	 * @param original     Original attribute to replace
	 * @param replacement  New atttribute
	 * @param baseUUID     UUID for the multiply base modifier
	 * @param totalUUID    UUID for the multiply total modifier
	 * @return  Additive value for attribute
	 */
	private static float replaceAttribute(ItemAttributeModifierEvent event, Attribute original, Attribute replacement, String name, UUID baseUUID, UUID totalUUID) {
		float additiveBoost = 0;
		float multiplyBase = 0;
		float multiplyTotal = 1;
		for (AttributeModifier modifier : event.removeAttribute(original)) {
			switch (modifier.getOperation()) {
				case ADDITION -> additiveBoost += modifier.getAmount();
				case MULTIPLY_BASE -> multiplyBase += modifier.getAmount();
				case MULTIPLY_TOTAL ->
						// operation is (1 + x1) * (1 + x2) * ..., so add the 1 before multiplying for the total
						multiplyTotal *= (1 + modifier.getAmount());
			}
		}
		// add in armor unique modifiers
		if (multiplyBase != 0) {
			event.addModifier(replacement, new AttributeModifier(baseUUID, name + "_multiply_base", multiplyBase, Operation.MULTIPLY_BASE));
		}
		// add in armor unique modifiers
		if (multiplyTotal != 1) {
			event.addModifier(replacement, new AttributeModifier(totalUUID, name + "_multiply_total", multiplyTotal - 1, Operation.MULTIPLY_TOTAL));
		}

		return additiveBoost;
	}

	/** Adds the attribute for relevant armors */
	@SubscribeEvent
	static void itemAttributeModifiers(ItemAttributeModifierEvent event) {
		// must be in the right slot
		float max = 0;
		float efficiency = 0;
		ItemStack stack = event.getItemStack();
		EquipmentSlot slot = event.getSlotType();
		if (slot == Mob.getEquipmentSlotForItem(stack)) {
			// boost from enchant
			max += EnchantmentHelper.getItemEnchantmentLevel(SimpleAbsorption.ABSORPTION, stack);

			// boost from gold
			int goldBoost = Config.GOLD_ABSORPTION.get();
			double chainBoost = Config.CHAIN_EFFICIENCY.get();
			if (goldBoost > 0 || chainBoost > 0) {
				Item item = stack.getItem();
				if (item instanceof ArmorItem) {
					ArmorMaterial material = ((ArmorItem)item).getMaterial();
					if (material == ArmorMaterials.GOLD) {
						max += goldBoost;
					} else if (material == ArmorMaterials.CHAIN) {
						efficiency += chainBoost;
					}
				}
			}
		}

		// replace armor means attributes on all 6 slots are replaced with absorption
		if (Config.REPLACE_ARMOR.get()) {
			// armor -> absorption max
			max += replaceAttribute(event, Attributes.ARMOR, SimpleAbsorption.ABSORPTION_MAX, "simple_absorption_max",
															ARMOR_MULTIPLY_BASE_UUID.get(slot), ARMOR_MULTIPLY_TOTAL_UUID.get(slot));
			// toughness -> absorption efficiency
			efficiency += replaceAttribute(event, Attributes.ARMOR_TOUGHNESS, SimpleAbsorption.ABSORPTION_EFFICIENCY, "simple_absorption_efficiency",
																		 EFFICIENCY_MULTIPLY_BASE_UUID.get(slot), EFFICIENCY_MULTIPLY_TOTAL_UUID.get(slot));
		}

		// add the attributes if we have any changes
		if (max != 0) event.addModifier(SimpleAbsorption.ABSORPTION_MAX, new AttributeModifier(ARMOR_ADD_UUID.get(slot), "simple_absorption_armor", max, Operation.ADDITION));
		if (efficiency != 0) event.addModifier(SimpleAbsorption.ABSORPTION_EFFICIENCY, new AttributeModifier(EFFICIENCY_ADD_UUID.get(slot), "simple_absorption_efficiency", efficiency, Operation.ADDITION));
	}

	/** Adds the attribute when absorption is added */
	@SubscribeEvent
	static void onAddPotion(PotionAddedEvent event) {
		// if we added absorption, add the modifier based on the level
		MobEffectInstance added = event.getPotionEffect();
		if (Config.INCLUDE_POTION.get() && added.getEffect() == MobEffects.ABSORPTION) {
			event.getEntityLiving().getAttributes().addTransientAttributeModifiers(ImmutableMultimap.of(SimpleAbsorption.ABSORPTION_MAX,
																																													new AttributeModifier(POTION_UUID, "simple_absorption_potion", (added.getAmplifier() + 1) * 4, Operation.ADDITION)));
		}
	}

	/** Removes the attribute when absorption is removed */
	@SubscribeEvent
	static void onRemovePotion(PotionRemoveEvent event) {
		// if we removed absorption, remove the modifier
		// remove regardless of config in case it changed since the attribute was added
		if (event.getPotion() == MobEffects.ABSORPTION) {
			event.getEntityLiving().getAttributes().removeAttributeModifiers(POTION_REMOVAL);
		}
	}

	/** Removes the attribute when absorption timer runs out */
	@SubscribeEvent
	static void onPotionExpire(PotionExpiryEvent event) {
		// see above comment
		MobEffectInstance instance = event.getPotionEffect();
		if (instance != null && instance.getEffect() == MobEffects.ABSORPTION) {
			event.getEntityLiving().getAttributes().removeAttributeModifiers(POTION_REMOVAL);
		}
	}
}
