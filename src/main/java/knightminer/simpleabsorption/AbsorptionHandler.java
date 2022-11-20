package knightminer.simpleabsorption;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.FoodStats;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

/**
 * Actual logic that grants players absorption.
 */
public class AbsorptionHandler implements ICapabilitySerializable<CompoundNBT> {
  /** Capability lazy instance */
  private final LazyOptional<AbsorptionHandler> lazy = LazyOptional.of(() -> this);
  /** Player instance, nullable because forge wants default caps or something weird like that */
  @Nullable
  private final PlayerEntity player;
  /** Timer to delay healing */
  private int timer = 0;
  /** Max absorption value last tick */
  private float lastMax = 0;

  public AbsorptionHandler(@Nullable PlayerEntity player) {
    this.player = player;
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    return AbsorptionCapability.CAPABILITY.orEmpty(cap, lazy);
  }

  /** Runs on player update to update absorption shield, internal event */
  void playerTick() {
    // do nothing if attributes are not yet initialized
    if (player == null || player.tickCount < 10) {
      return;
    }

    // determine the max from the attribute
    float max = (float)(Config.BASE_ABSORPTION.get() + player.getAttributeValue(SimpleAbsorption.ABSORPTION_MAX));
    float absorption = player.getAbsorptionAmount();

    // if our max dropped and we now have too much, reduce by the difference
    // do not reduce below the cap, or reduce more than the difference (in case another mod granted absorption)
    if (lastMax > max && absorption > max) {
      player.setAbsorptionAmount(Math.max(absorption - (lastMax - max), max));
      return;
    }
    lastMax = max;

    // if at full absorption, done processing
    if(absorption >= max) {
      return;
    }

    // if natural regen is enabled, player must have full health
    // absorption acts like an extension on regular health there
    if(player.getHealth() < player.getMaxHealth() && player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
      return;
    }

    // determine efficiency before we regen
    float efficiency = (float)player.getAttributeValue(SimpleAbsorption.ABSORPTION_EFFICIENCY);

    // food stat props
    FoodStats stats = player.getFoodData();
    float saturation = stats.getSaturationLevel();
    int foodLevel = stats.getFoodLevel();
    // full food: rapid heal
    float heal = 0;
    // reduce the exhaustion rate with efficiency, need to ensure its above 0
    float exhaustionRate = Math.max(0.1f, 6.0f - (efficiency / 4));

    // full food: rapid heal
    if (saturation > 0.0F && foodLevel >= 20) {
      timer += 1;
      if (timer >= 10) {
        float amount = Math.min(saturation, exhaustionRate);
        stats.addExhaustion(amount);
        heal = amount / exhaustionRate;
        timer = 0;
      }
      // 18 to 20: regular heal, efficiency makes it a lesser requirement
    } else if (foodLevel >= Math.max(1, (18 - efficiency))) {
      timer += 1;
      // efficiency makes it a bit faster too
      if(timer >= (80 - (2 * efficiency))) {
        player.causeFoodExhaustion(exhaustionRate);
        heal = 1.0f;
        timer = 0;
      }
    } else {
      timer = 0;
    }
    // update the player's absorption
    if (heal > 0) {
      player.setAbsorptionAmount(Math.min(absorption + heal, max));
    }
  }


  /* NBT */

  /** Tag for timer for absorption regen */
  private static final String TAG_TIMER = "simple_absorption_timer";
  /** Tag for last max for absorption reduction */
  private static final String TAG_MAX = "simple_absorption_max";

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putByte(TAG_TIMER, (byte)timer);
    nbt.putFloat(TAG_MAX, lastMax);
    return nbt;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    timer = nbt.getByte(TAG_TIMER);
    lastMax = nbt.getFloat(TAG_MAX);
  }
}
