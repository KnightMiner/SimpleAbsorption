package knightminer.simpleabsorption;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

@SuppressWarnings("WeakerAccess")
public class Config {
  static final ForgeConfigSpec SPEC;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    BASE_ABSORPTION = builder
        .comment("Base absorption amount to give to players in half hearts, replenishes like regular health. If 0, players will start with no absorption.")
        .defineInRange("base_absorption", 0, 0, 20);
    INCLUDE_POTION = builder
        .comment("If true, hearts from the potion effect will be included in absorption calculations. This means absorption from potions will replenish during the timer mostly.")
        .define("include_potion", false);
    GOLD_ABSORPTION = builder
        .comment("Amount of half hearts of absorption granted per piece of gold armor worn.")
        .defineInRange("gold_absorption", 1, 0, 4);
    CHAIN_EFFICIENCY = builder
        .comment("Absorption efficiency boost per chain armor piece. Absorption efficiency decreases hunger consumption and hunger requirements to heal.")
        .defineInRange("chain_efficiency", 1.0, 0.0, 4.0);
    MAX_ENCHANT = builder
        .comment("Maximum level for the enchantment per piece. Each level gives half a heart. If 0, enchantment will be disabled.")
        .defineInRange("max_enchantment_level", 4, 0, 6);
    ENCHANT_SHIELDS = builder
        .comment("If true, shields can receive the absorption enchant through an anvil. The enchant will apply when the shield is in the offhand")
        .define("enchant_shields", true);
    REPLACE_ARMOR = builder
        .comment("If true, armor grants absorption hearts instead of armor values and absorption efficiency instead of toughness. Should work with most mods. May want to reconsider gold and chain bonuses if you enable this.")
        .define("replace_armor", false);

    SPEC = builder.build();
  }

  // properties
  public static final IntValue BASE_ABSORPTION;
  public static final BooleanValue INCLUDE_POTION;
  public static final IntValue GOLD_ABSORPTION;
  public static final DoubleValue CHAIN_EFFICIENCY;
  public static final IntValue MAX_ENCHANT;
  public static final BooleanValue ENCHANT_SHIELDS;
  public static final BooleanValue REPLACE_ARMOR;
}
