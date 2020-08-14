package nordstromeval;

import java.util.Map;

/**
 * Definition of an eviction strategy.  None of the current strategies
 * depend on the cache value, but it's defined anyway in case some
 * future strategy wants to do that.
 *
 * @author fluffy
 * @param <K> The type of the keys in the cache
 * @param <V> The type of the values in the cache
 */
public interface EvictionStrategy<K, V>
{

  /**
   * Initialize a new EvictionStrategy.  The cache might not be empty,
   * so a copy of the cache is provided.
   *
   * @param cache The contents of the cache
   * @param size The maximum size of the cache
   */
  public void init(Map<K,V> cache, int size);

  /**
   * Call this method before adding an element to the cache to signify that a 
   * new element is being added; also works for replace
   *
   * @param key The key being added
   * @param value The value being added
   * @return The key to evict, or null if no eviction is needed
   */
  public K addElement(K key, V value);
  
  /**
   * Call this method when an element is touched, but not modified.  Useful
   * for LRU type strategies.
   *
   * @param key The key being touched
   * @param value The value being touched
   */
  default public void touchElement(K key, V value)
  {
    // Default empty implementation here
  }
}
