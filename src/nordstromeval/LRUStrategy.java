package nordstromeval;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A strategy implementing LRU cache policy. The performance is dominated by the
 * O(log(n)) operations on Treemap.
 *
 * @author fluffy
 * @param <K> The type of the keys in the cache
 * @param <V> The type of the values in the cache
 */
public class LRUStrategy<K, V> implements EvictionStrategy<K, V>
{
  // Set of known cache keys; used to quickly determine if a key is known,
  // and the number of known elements
  private final Map<K, Long> knownKeys = new java.util.HashMap();
  // Map from timestamps to cache keys.  Assuming only one key per
  // timestamp would be unsafe, so a Set of keys is stored for each timestamp.
  // Timestamps are longs, and have a natural ordering that does just what 
  // is needed here; otherwise a Comparator would be needed
  private final TreeMap<Long, Set<K>> timestamps = new TreeMap();
  private int maxSize;

  @Override
  public synchronized void init(Map<K, V> cache, int size)
  {
    this.maxSize = size;
    knownKeys.clear();
    timestamps.clear();
    cache.forEach((key, value) -> this.addElement(key, value));
  }

  // A method that is factored out from addElement/touchElement as common
  // functionality
  private void removeExistingTimestamp(K key)
  {
    Long existingTimestamp = knownKeys.get(key);
    Set<K> keySet = timestamps.get(existingTimestamp);
    keySet.remove(key);
    // If multiple keys were assigned to this timestamp, remove one;
    // otherwise clear the timestamp
    if (keySet.size() > 0)
      timestamps.put(existingTimestamp, keySet);
    else
      timestamps.remove(existingTimestamp);
  }

  // Another common functionality method
  private void addNewTimestamp(K key)
  {
    Long now = System.currentTimeMillis();
    // Add the new data to the timestamped set, creating or adding to
    // the set of keys, as needed
    Set<K> newCacheKeyset = timestamps.get(now);
    if (newCacheKeyset == null)
      newCacheKeyset = new HashSet<>();
    newCacheKeyset.add(key);
    timestamps.put(now, newCacheKeyset);
    // Finally add the new key to the set of known keys
    knownKeys.put(key, now);
  }

  @Override
  public synchronized K addElement(K key, V value)
  {
    K toEvict = null;
    // First thing to do is remove the key's old timestamp, if it exists
    // Key is not removed from set of known keys because it will be replaced
    // anyway
    Long existingKeyTimestamp = knownKeys.get(key);
    if (existingKeyTimestamp != null)
      removeExistingTimestamp(key);
    // If the cache is full and the element is new, evict the oldest
    // element (the one with the smallest timestamp), otherwise evict nothing
    // reaching this point always means element is new, otherwise if block above
    // would be taken
    else if (knownKeys.size() == this.maxSize)
    {
      // Get the oldest map entry
      Map.Entry<Long, Set<K>> oldest = timestamps.firstEntry();
      // Get the set of cache keys added at this timestamp
      Set<K> cacheKeyset = oldest.getValue();
      Long oldestTimestamp = oldest.getKey();
      Iterator<K> iterator = cacheKeyset.iterator();
      // Any of the cache keys in the set will do, grab the first one
      toEvict = iterator.next();
      // If multiple keys were assigned to this timestamp, remove one;
      // otherwise clear the timestamp
      if (iterator.hasNext())
        iterator.remove();
      else
        timestamps.remove(oldestTimestamp);
      // Finally remove the key being evicted from the known set
      knownKeys.remove(toEvict);
    }

    // Now that the evicted data is cleaned up, track the new data
    addNewTimestamp(key);
    return toEvict;
  }

  @Override
  public synchronized void touchElement(K key, V value)
  {
    removeExistingTimestamp(key);
    addNewTimestamp(key);
  }

}
