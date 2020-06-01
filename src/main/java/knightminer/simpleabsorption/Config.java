package knightminer.simpleabsorption;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Config {
  public static final ForgeConfigSpec SPEC;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    BASE_ABSORPTION = builder
        .comment("Base absorption amount to give to players in half hearts, replenishes like regular health. If 0, players will start with no absorption.")
        .defineInRange("base_absorption", 0, 0, 20);
    INCLUDE_POTION = builder
        .comment("If true, hearts from the potion effect will be included in absorption calculations")
        .define("include_potion", false);
    GOLD_ABSORPTION = builder
        .comment("Amount of half hearts of absorption granted per piece of gold armor worn.")
        .defineInRange("gold_absorption", 1, 0, 4);
    MAX_ENCHANT = builder
        .comment("Maximum level for the enchantment per piece. Each level gives half a heart. If 0, enchantment will be disabled.")
        .defineInRange("max_enchantment_level", 4, 0, 6);

    SPEC = builder.build();
  }

  // properties
  public static final IntValue BASE_ABSORPTION;
  public static final BooleanValue INCLUDE_POTION;
  public static final IntValue GOLD_ABSORPTION;
  public static final IntValue MAX_ENCHANT;
}
