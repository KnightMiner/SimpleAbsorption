package knightminer.simpleabsorption;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class AbsorptionEnchantment extends Enchantment {
  protected AbsorptionEnchantment(Rarity rarity, EquipmentSlotType... slots) {
    super(rarity, EnchantmentType.ARMOR, slots);
  }

  @Override
  public int getMaxLevel() {
    return Math.max(Config.MAX_ENCHANT.get(), 1);
  }

  @Override
  protected boolean canApplyTogether(Enchantment enchant) {
    return this != enchant && !(enchant instanceof ProtectionEnchantment);
  }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack) {
    return Config.MAX_ENCHANT.get() > 0 && stack.canApplyAtEnchantingTable(this);
  }

  @Override
  public boolean isAllowedOnBooks() {
    return Config.MAX_ENCHANT.get() > 0;
  }
}
