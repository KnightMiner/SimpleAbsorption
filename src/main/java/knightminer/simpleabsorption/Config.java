package knightminer.simpleabsorption;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Config {
  private static final List<CachedValue<?>> CACHED_VALUES = new ArrayList<>();
  static final ForgeConfigSpec SPEC;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    BASE_ABSORPTION = cached(builder
        .comment("Base absorption amount to give to players in half hearts, replenishes like regular health. If 0, players will start with no absorption.")
        .defineInRange("base_absorption", 0, 0, 20));
    INCLUDE_POTION = cached(builder
        .comment("If true, hearts from the potion effect will be included in absorption calculations")
        .define("include_potion", false));
    GOLD_ABSORPTION = cached(builder
        .comment("Amount of half hearts of absorption granted per piece of gold armor worn.")
        .defineInRange("gold_absorption", 1, 0, 4));
    MAX_ENCHANT = cached(builder
        .comment("Maximum level for the enchantment per piece. Each level gives half a heart. If 0, enchantment will be disabled.")
        .defineInRange("max_enchantment_level", 4, 0, 6));

    SPEC = builder.build();
  }

  // properties
  public static final CachedValue<Integer> BASE_ABSORPTION;
  public static final CachedValue<Boolean> INCLUDE_POTION;
  public static final CachedValue<Integer> GOLD_ABSORPTION;
  public static final CachedValue<Integer> MAX_ENCHANT;

  /**
   * Creates a cached config value and adds it to the list to be invalidated on reload
   * @param value  Config value
   * @param <T>    Value type
   * @return  Cached config value
   */
  private static <T> CachedValue<T> cached(ConfigValue<T> value) {
    CachedValue<T> cached = new CachedValue<>(value);
    CACHED_VALUES.add(cached);
    return cached;
  }

  /** Called on config change to clear the cache */
  public static void configChanged(final ModConfig.ModConfigEvent configEvent) {
    ModConfig config = configEvent.getConfig();
    if (config.getModId().equals(SimpleAbsorption.MOD_ID)) {
      CACHED_VALUES.forEach(CachedValue::invalidate);
    }
  }
}
