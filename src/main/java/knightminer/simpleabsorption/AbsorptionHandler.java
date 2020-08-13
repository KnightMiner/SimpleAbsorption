package knightminer.simpleabsorption;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.FoodStats;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * Actual logic that grants players absorption.
 */
@SuppressWarnings("WeakerAccess")
public class AbsorptionHandler {
  // NBT
  private static final String TAG_TIMER = "simple_absorption_timer";
  private static final String TAG_MAX = "simple_absorption_max";

  /**
   * Gets the ABSORPTION maximum for a player
   * @param player  Player to check absorption level
   * @return  Number of absorption hearts allowed
   */
  private static int getMaxAbsorption(PlayerEntity player) {
    int max = Config.BASE_ABSORPTION.get();

    // potions
    if (Config.INCLUDE_POTION.get()) {
      EffectInstance effect = player.getActivePotionEffect(Effects.ABSORPTION);
      if (effect != null) {
        max += (effect.getAmplifier() + 1) * 4;
      }
    }

    // armor
    int goldBoost = Config.GOLD_ABSORPTION.get();
    for (ItemStack stack : player.getArmorInventoryList()) {
      max += EnchantmentHelper.getEnchantmentLevel(SimpleAbsorption.ABSORPTION, stack);
      // increase by amount for a gold piece
      if (goldBoost > 0) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem && ((ArmorItem)item).getArmorMaterial() == ArmorMaterial.GOLD) {
          max += goldBoost;
        }
      }
    }

    return max;
  }

  /** Runs on player update to update absorption shield, internal event */
  protected static void playerTick(PlayerTickEvent event) {
    // use phase.start so we run before the food timer resets
    if(event.side != LogicalSide.SERVER || event.phase != Phase.START) {
      return;
    }

    // fetch NBT data
    PlayerEntity player = event.player;
    CompoundNBT nbt = player.getPersistentData();

    // determine max ABSORPTION amount
    float absorption = player.getAbsorptionAmount();
    int max;
    if (nbt.contains(TAG_MAX, 99)) {
      max = nbt.getInt(TAG_MAX);
      // every 2.5 seconds, update their max amount
      if (player.ticksExisted % 100 == 0) {
        int newMax = getMaxAbsorption(player);
        nbt.putInt(TAG_MAX, newMax);
        // if our max dropped and we now have too much, reduce by the difference
        // do not reduce below the cap, or reduce more than the difference (another mod granted absorption)
        if (max > newMax && absorption > newMax) {
          player.setAbsorptionAmount(Math.max(absorption - (max - newMax), newMax));
          return;
        }
        max = newMax;
      }
    } else {
      // just fetch and immediately cache
      max = getMaxAbsorption(player);
      nbt.putInt(TAG_MAX, max);
    }

    // if at full ABSORPTION, done processing
    if(absorption >= max) {
      return;
    }

    // if natural regen is enabled, player must have full health
    // absorption acts like an extension on regular health there
    if(player.getHealth() < player.getMaxHealth() && player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
      return;
    }

    // food stat props
    FoodStats stats = player.getFoodStats();
    float saturation = stats.getSaturationLevel();
    int foodLevel = stats.getFoodLevel();
    // full food: rapid heal
    float heal = 0;
    int timer;
    if(saturation > 0.0F && foodLevel >= 20) {
      timer = nbt.getByte(TAG_TIMER) + 1;
      if(timer >= 10) {
        float amount = Math.min(saturation, 6.0f);
        stats.addExhaustion(amount);
        heal = amount / 6.0f;
        timer = 0;
      }
      // 18 to 20: regular heal
    } else if(foodLevel >= 18) {
      timer = nbt.getByte(TAG_TIMER) + 1;
      if(timer >= 80) {
        player.addExhaustion(6.0f);
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
}
