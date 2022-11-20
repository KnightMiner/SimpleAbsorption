package knightminer.simpleabsorption;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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
        .comment("If true, hearts from the potion effect will be included in absorption calculations. This means absorption from potions will replenish during the timer mostly.")
        .define("include_potion", false));
    GOLD_ABSORPTION = cached(builder
        .comment("Amount of half hearts of absorption granted per piece of gold armor worn.")
        .defineInRange("gold_absorption", 1, 0, 4));
    CHAIN_EFFICIENCY = cached(builder
        .comment("Absorption efficiency boost per chain armor piece. Absorption efficiency decreases hunger consumption and hunger requirements to heal.")
        .defineInRange("chain_efficiency", 1.0, 0.0, 4.0));
    MAX_ENCHANT = cached(builder
        .comment("Maximum level for the enchantment per piece. Each level gives half a heart. If 0, enchantment will be disabled.")
        .defineInRange("max_enchantment_level", 4, 0, 6));
    ENCHANT_SHIELDS = cached(builder
        .comment("If true, shields can receive the absorption enchant through an anvil. The enchant will apply when the shield is in the offhand")
        .define("enchant_shields", true));
    REPLACE_ARMOR = cached(builder
        .comment("If true, armor grants absorption hearts instead of armor values and absorption efficiency instead of toughness. Should work with most mods. May want to reconsider gold and chain bonuses if you enable this.")
        .define("replace_armor", false));

    SPEC = builder.build();
  }

  // properties
  public static final CachedValue<Integer> BASE_ABSORPTION;
  public static final CachedValue<Boolean> INCLUDE_POTION;
  public static final CachedValue<Integer> GOLD_ABSORPTION;
  public static final CachedValue<Double> CHAIN_EFFICIENCY;
  public static final CachedValue<Integer> MAX_ENCHANT;
  public static final CachedValue<Boolean> ENCHANT_SHIELDS;
  public static final CachedValue<Boolean> REPLACE_ARMOR;

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
  public static void configChanged(final ModConfigEvent configEvent) {
    ModConfig config = configEvent.getConfig();
    if (config.getModId().equals(SimpleAbsorption.MOD_ID)) {
      CACHED_VALUES.forEach(CachedValue::invalidate);
    }
  }
}
