package nordstromeval;

import java.util.HashMap;
import java.util.Map;

/**
 * A cache implementation conforming to the spec provided
 *
 * The cache's methods are synchronized, but that does not guarantee full
 * concurrency. For example, if (exists(key)) get(key) is not guaranteed to
 * work, because another access to the cache could cause key to be evicted after
 * exists() but before get().
 *
 * @author fluffy
 * @param <K> The type of the key
 * @param <V> The type of the value
 *
 */
public class Memcache<K, V>
{
  private final Map<K, V> cache = new HashMap();
  private final int size;
  private EvictionStrategy<K, V> strategy;

  private Memcache(int size)
  {
    this.size = size;
  }

  /**
   * Create a new instance of the cache
   *
   * @param <K> The type of the key
   * @param <V> The type of the value
   * @param size The size of the cache
   * @param strategy The strategy to use
   * @return The created cache
   */
  // Could also be created by a builder class, but probably not 
  // worth the trouble here
  public static <K, V> Memcache<K, V> create(int size, EvictionStrategy strategy)
  {
    Memcache<K, V> cache = new Memcache<>(size);
    cache.setStrategy(strategy);
    return cache;
  }

  /**
   * Choose an eviction strategy. Must be assigned at creation time; can be
   * reassigned later.
   *
   * @param strategy The strategy to use
   */
  public synchronized void setStrategy(EvictionStrategy<K, V> strategy)
  {
    // Prevent strategy from mangling the raw cache
    // As initialization cost, copying the cache should cause minimal overhead -
    // especially since it will usually be called on an empty cache.
    HashMap clone = new HashMap<>(cache);
    strategy.init(clone, size);
    this.strategy = strategy;
  }

  /**
   * Retrieve an element from the cache.
   *
   * @param key
   * @return The value associated with the key, or null if the key is unknown.
   */
  public synchronized V get(K key)
  {
    V value = cache.get(key);
    if (value != null)
      strategy.touchElement(key, value);
    return value;
  }

  /**
   * Test if a key exists in the cache. Do not depend on this result for
   * correctness in the presence of concurrent write operations.
   *
   * @param key The key to test
   * @return True if the key is known to the cache, false otherwise
   */
  public boolean exists(K key)
  {
    // This method doesn't have to be synchronized because it doesn't touch
    // any state directly.  But it could be, and it wouldn't hurt anything.
    return (get(key) != null);
  }

  /**
   *
   * @param key The key to add
   * @param value The value to add associated with the key
   * @return True if the key already existed, false if not
   */
  public synchronized boolean add(K key, V value)
  {
    K toRemove = strategy.addElement(key, value);
    if (toRemove != null)
      cache.remove(toRemove);
    return (cache.put(key, value) != null);
  }

}
