package knightminer.simpleabsorption;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.FoodStats;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionExpiryEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Actual logic that grants players absorption.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AbsorptionHandler {
  // NBT
  /** Tag for timer for absorption regen */
  private static final String TAG_TIMER = "simple_absorption_timer";
  /** Tag for last max for absorption reduction */
  private static final String TAG_MAX = "simple_absorption_max";

  /** Generates a UUID map for all slot types from a string key */
  private static Map<EquipmentSlotType,UUID> makeUUIDMap(String key) {
    Map<EquipmentSlotType,UUID> map = new EnumMap<>(EquipmentSlotType.class);
    for (EquipmentSlotType type : EquipmentSlotType.values()) {
      map.put(type, UUID.nameUUIDFromBytes((key + type.getName()).getBytes()));
    }
    return map;
  }

  /** Map of slot to UUID to ensure consistent removals */
  private static final Map<EquipmentSlotType,UUID> ARMOR_ADD_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_armor_add");
  private static final Map<EquipmentSlotType,UUID> ARMOR_MULTIPLY_TOTAL_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_armor_multiply_total");
  private static final Map<EquipmentSlotType,UUID> ARMOR_MULTIPLY_BASE_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_armor_multiply_base");
  private static final Map<EquipmentSlotType,UUID> EFFICIENCY_ADD_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_regen_add");
  private static final Map<EquipmentSlotType,UUID> EFFICIENCY_MULTIPLY_TOTAL_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_efficiency_multiply_total");
  private static final Map<EquipmentSlotType,UUID> EFFICIENCY_MULTIPLY_BASE_UUID = makeUUIDMap(SimpleAbsorption.MOD_ID + "_efficiency_multiply_base");

  /** UUID for potions */
  private static final UUID POTION_UUID = UUID.fromString("e7c88f6c-4d46-11eb-ae93-0242ac130002");
  /** Cached object for removing the potion attribute */
  private static final Multimap<Attribute, AttributeModifier> POTION_REMOVAL = ImmutableMultimap.of(SimpleAbsorption.ABSORPTION_MAX, new AttributeModifier(POTION_UUID, "simple_absorption_potion", 0, Operation.ADDITION));

  /** Runs on player update to update absorption shield, internal event */
  @SubscribeEvent
  static void playerTick(PlayerTickEvent event) {
    // use phase.start so we run before the food timer resets
    if(event.side != LogicalSide.SERVER || event.phase != Phase.START) {
      return;
    }

    // fetch NBT data
    PlayerEntity player = event.player;

    // determine the max from the attribute
    ModifiableAttributeInstance maxAttribute = player.getAttribute(SimpleAbsorption.ABSORPTION_MAX);
    float max = 0;
    if (maxAttribute != null) {
      max = (int) maxAttribute.getValue();
    }

    // every five seconds, ensure we are not over the max
    float absorption = player.getAbsorptionAmount();
    CompoundNBT nbt = player.getPersistentData();
    // note its important the check is == 99, for the first couple ticks of the players existance item attributes are not yet initialized
    if (player.ticksExisted % 100 == 99) {
      if (nbt.contains(TAG_MAX, NBT.TAG_ANY_NUMERIC)) {
        float oldMax = nbt.getFloat(TAG_MAX);
        // if our max dropped and we now have too much, reduce by the difference
        // do not reduce below the cap, or reduce more than the difference (in case another mod granted absorption)
        if (oldMax > max && absorption > max) {
          player.setAbsorptionAmount(Math.max(absorption - (oldMax - max), max));
          return;
        }
      }
      nbt.putFloat(TAG_MAX, max);
    }

    // if at full absorption, done processing
    if(absorption >= max) {
      return;
    }

    // if natural regen is enabled, player must have full health
    // absorption acts like an extension on regular health there
    if(player.getHealth() < player.getMaxHealth() && player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
      return;
    }

    // determine efficiency before we regen
    ModifiableAttributeInstance attributeEfficiency = player.getAttribute(SimpleAbsorption.ABSORPTION_EFFICIENCY);
    float efficiency = 0;
    if (attributeEfficiency != null) {
      efficiency = (float)attributeEfficiency.getValue();
    }

    // food stat props
    FoodStats stats = player.getFoodStats();
    float saturation = stats.getSaturationLevel();
    int foodLevel = stats.getFoodLevel();
    // full food: rapid heal
    float heal = 0;
    int timer;
    // reduce the exhaustion rate with efficiency, need to ensure its above 0
    float exhaustionRate = Math.max(0.1f, 6.0f - (efficiency / 4));

    // full food: rapid heal
    if(saturation > 0.0F && foodLevel >= 20) {
      timer = nbt.getByte(TAG_TIMER) + 1;
      if(timer >= 10) {
        float amount = Math.min(saturation, exhaustionRate);
        stats.addExhaustion(amount);
        heal = amount / exhaustionRate;
        timer = 0;
      }
      // 18 to 20: regular heal, efficiency makes it a lesser requirement
    } else if(foodLevel >= (18 - efficiency)) {
      timer = nbt.getByte(TAG_TIMER) + 1;
      if(timer >= 80) {
        player.addExhaustion(exhaustionRate);
        heal = 1.0f;
        timer = 0;
      }
    } else {
      timer = 0;
    }
    // update the player's absorption
    if(heal > 0) {
      player.setAbsorptionAmount(Math.min(absorption + heal, max));
    }
    // update the timer
    nbt.putByte(TAG_TIMER, (byte)timer);
  }


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
      Operation operation = modifier.getOperation();
      switch (modifier.getOperation()) {
        case ADDITION:
          additiveBoost += modifier.getAmount();
          break;
        case MULTIPLY_BASE:
          multiplyBase += modifier.getAmount();
          break;
        case MULTIPLY_TOTAL:
          // operation is (1 + x1) * (1 + x2) * ..., so add the 1 before multiplying for the total
          multiplyTotal *= (1 + modifier.getAmount());
          break;
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
    EquipmentSlotType slot = event.getSlotType();
    if (slot == MobEntity.getSlotForItemStack(stack)) {
      // boost from enchant
      max += EnchantmentHelper.getEnchantmentLevel(SimpleAbsorption.ABSORPTION, stack);

      // boost from gold
      int goldBoost = Config.GOLD_ABSORPTION.get();
      double chainBoost = Config.CHAIN_EFFICIENCY.get();
      if (goldBoost > 0 || chainBoost > 0) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem) {
          IArmorMaterial material = ((ArmorItem)item).getArmorMaterial();
          if (material == ArmorMaterial.GOLD) {
            max += goldBoost;
          } else if (material == ArmorMaterial.CHAIN) {
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
    EffectInstance added = event.getPotionEffect();
    if (Config.INCLUDE_POTION.get() && added.getPotion() == Effects.ABSORPTION) {
      event.getEntityLiving().getAttributeManager().reapplyModifiers(ImmutableMultimap.of(SimpleAbsorption.ABSORPTION_MAX,
                                                                                          new AttributeModifier(POTION_UUID, "simple_absorption_potion", (added.getAmplifier() + 1) * 4, Operation.ADDITION)));
    }
  }

  /** Removes the attribute when absorption is removed */
  @SubscribeEvent
  static void onRemovePotion(PotionRemoveEvent event) {
    // if we removed absorption, remove the modifier
    // remove regardless of config in case it changed since the attribute was added
    if (event.getPotion() == Effects.ABSORPTION) {
      event.getEntityLiving().getAttributeManager().removeModifiers(POTION_REMOVAL);
    }
  }

  /** Removes the attribute when absorption timer runs out */
  @SubscribeEvent
  static void onPotionExpire(PotionExpiryEvent event) {
    // see above comment
    EffectInstance instance = event.getPotionEffect();
    if (instance != null && instance.getPotion() == Effects.ABSORPTION) {
      event.getEntityLiving().getAttributeManager().removeModifiers(POTION_REMOVAL);
    }
  }
}
