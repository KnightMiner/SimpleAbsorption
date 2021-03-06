package knightminer.simpleabsorption;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.function.Supplier;

/**
 * Cached version of a config value, since the get method is a bit expensive
 * @param <T>  Config value type
 */
@SuppressWarnings("WeakerAccess")
public class CachedValue<T> implements Supplier<T> {
  private boolean resolved = false;
  private T cached;
  private final Supplier<T> supplier;

  /**
   * Creates a new vaue using a supplier
   * @param supplier  Supplier instance
   */
  @SuppressWarnings("WeakerAccess")
  public CachedValue(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  /**
   * Creates a new value using a config value
   * @param config  Config value
   */
  public CachedValue(ConfigValue<T> config) {
    this(config::get);
  }

  @Override
  public T get() {
    if (!resolved) {
      cached = supplier.get();
      resolved = true;
    }
    return cached;
  }

  /**
   * Invalidates this value's cache
   */
  public void invalidate() {
    resolved = false;
    cached = null;
  }
}
