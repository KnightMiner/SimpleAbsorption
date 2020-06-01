package knightminer.simpleabsorption;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Config {
  public static final ForgeConfigSpec SPEC;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    BASE_ABSORPTION = builder
        .comment("Base absorption amount to give to players in half hearts, replenishes like regular health. If 0, players will start with no absorption.")
        .defineInRange("base_absorption", 0, 0, 20);

    SPEC = builder.build();
  }

  // properties
  public static final IntValue BASE_ABSORPTION;
}
