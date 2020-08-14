package nordstromeval;

import java.util.ArrayList;
import java.util.Map;

/**
 * Evict elements at random from the cache
 *
 * Works by maintaining a copy of the set of keys, then picking a random element
 * from the copy to evict.
 *
 * Choice of data structure matters: Naive implementations may not choose
 * random elements very randomly.  An array is the most efficient way to
 * choose elements randomly, while a hashmap from key to array position is
 * used to check if an element already exists.  This enables both insertion
 * and eviction in O(1) time.
 *
 * A FIFO queue implemented as a linked list allows both add and evict in O(1)
 * and has better performance compared to random; FIFO is just better. But the
 * spec says random.
 *
 * @author fluffy
 * @param <K> The type of the keys in the cache
 * @param <V> The type of the values in the cache
 */
public class RandomStrategy<K, V> implements EvictionStrategy<K, V>
{
  private final Map<K, Integer> knownKeys = new java.util.HashMap();
  // Arraylist chosen over array due to arrays not working with generics
  private final ArrayList<K> orderedKeys = new ArrayList();
  private int maxSize;
  // system library RNG is not very good, but should be good enough for this
  private final java.util.Random RNG = new java.util.Random();

  @Override
  public synchronized void init(Map<K, V> cache, int size)
  {
    this.maxSize = size;
    knownKeys.clear();
    orderedKeys.clear();
    orderedKeys.ensureCapacity(size);
    cache.forEach((key, value) -> this.addElement(key, value));
  }

  @Override
  public synchronized K addElement(K key, V value)
  {
    K toEvict = null;

    // If key is unknown, it should be added.  If key is known, nothing to do.
    if (!knownKeys.containsKey(key))
    {
      // If array of keys is not full, simply add the new key to it
      if (orderedKeys.size() < maxSize)
      {
        // New key will be added to array at tail position (== current size)
        knownKeys.put(key, orderedKeys.size());
        orderedKeys.add(key);
      } // Eviction is required as the cache is full
      else
      {
        int position = RNG.nextInt(maxSize);
        // Replace the old element at the random position with the new one
        toEvict = orderedKeys.set(position, key);
        knownKeys.remove(toEvict);
        knownKeys.put(key, position);
      }
    } // else known key, do nothing

    return toEvict;
  }
}
