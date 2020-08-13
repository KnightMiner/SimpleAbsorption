package knightminer.simpleabsorption;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Client event handler to prevent accidental client side access
 */
@SuppressWarnings("unused")
@EventBusSubscriber(modid = SimpleAbsorption.MOD_ID, value = Dist.CLIENT, bus = Bus.FORGE)
public class ClientEvents {
  /** Adds the absorption tooltip to armor */
  @SubscribeEvent
  static void itemTooltip(ItemTooltipEvent event) {
    // tooltips only matter for gold armor or the enchant
    int goldBoost = Config.GOLD_ABSORPTION.get();
    if (goldBoost == 0 && Config.MAX_ENCHANT.get() == 0) {
      return;
    }
    // enchantment level
    ItemStack stack = event.getItemStack();
    int absorption = EnchantmentHelper.getEnchantmentLevel(SimpleAbsorption.ABSORPTION, stack);
    // gold boost
    Item item = stack.getItem();
    if (item instanceof ArmorItem && ((ArmorItem)item).getArmorMaterial() == ArmorMaterial.GOLD) {
      absorption += goldBoost;
    }
    // add tooltip if relevant
    if (absorption > 0) {
      event.getToolTip().add(new TranslationTextComponent("tooltip.simple_absorption.absorption", absorption).mergeStyle(TextFormatting.BLUE));
    }
  }
}
