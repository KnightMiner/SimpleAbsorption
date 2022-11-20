package knightminer.simpleabsorption;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.enchantment.Enchantment.Rarity;

/**
 * Enchantment granting bonus absorption hearts
 */
@SuppressWarnings("WeakerAccess")
public class AbsorptionEnchantment extends Enchantment {
  public AbsorptionEnchantment(Rarity rarity, EquipmentSlot... slots) {
    super(rarity, EnchantmentCategory.ARMOR, slots);
  }

  @Override
  public int getMaxLevel() {
    return Math.max(Config.MAX_ENCHANT.get(), 1);
  }

  @Override
  protected boolean checkCompatibility(Enchantment enchant) {
    return this != enchant && !(enchant instanceof ProtectionEnchantment);
  }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack) {
    return Config.MAX_ENCHANT.get() > 0 && stack.canApplyAtEnchantingTable(this);
  }

  @Override
  public boolean canEnchant(ItemStack stack) {
    // apply to shields on anvils if enabled
    if (Config.ENCHANT_SHIELDS.get() && stack.isShield(null)) {
      return true;
    }
    return super.canEnchant(stack);
  }

  @Override
  public boolean isAllowedOnBooks() {
    return Config.MAX_ENCHANT.get() > 0;
  }
}
