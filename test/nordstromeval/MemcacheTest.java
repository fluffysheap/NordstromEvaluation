package nordstromeval;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the memory cache
 *
 * @author fluffy
 */
public class MemcacheTest
{
  Memcache<String, String> theCache;

  public MemcacheTest()
  {
  }

  @BeforeClass
  public static void setUpClass()
  {

  }

  @AfterClass
  public static void tearDownClass()
  {
  }

  @Before
  public void setUp()
  {
    theCache = Memcache.create(5, new LRUStrategy());
    theCache.add("foo", "lovely foo");
    try
    {
      // Sleeps make sure the timestamps are in a defined order
      Thread.sleep(100);
      theCache.add("bar", "lovely bar");
      Thread.sleep(100);
      theCache.add("baz", "lovely baz");
      theCache.add("quux", "lovely quux");
      theCache.add("fluff", "lovely fluff");
      Thread.sleep(100);
    } catch (InterruptedException ex)
    {
    }
  }

  @After
  public void tearDown()
  {
  }

  /**
   * Test of get method, of class Memcache.
   */
  @Test
  public void testGet()
  {
    System.out.println("get");
    String result = theCache.get("foo");
    assertEquals("lovely foo", result);
    result = theCache.get("fluff");
    assertEquals("lovely fluff", result);
    result = theCache.get("nothing here");
    assertNull(result);
  }

  /**
   * Test of LRU eviction, of class Memcache.
   */
  @Test
  public void testLRUEvict()
  {
    System.out.println("evict");
    String revised = "revised foo";
    theCache.add("foo", revised);
    theCache.add("overflow", "too much stuff!");
    String result = theCache.get("foo");
    assertEquals(revised, result);
    result = theCache.get("bar");
    assertNull(result);
  }

  /**
   * Test of random eviction, of class Memcache.
   */
  @Test
  public void testRandomEvict()
  {
    System.out.println("random");
    theCache.setStrategy(new RandomStrategy());
    String randomData = "ooblegoobleooblegooble is pretty random";
    theCache.add("random", randomData);
    String result = theCache.get("random");
    assertEquals(randomData, result);
    
    int numExists = 0;
    if (theCache.exists("foo"))
      numExists++;
    if (theCache.exists("bar"))
      numExists++;
    if (theCache.exists("baz"))
      numExists++;
    if (theCache.exists("quux"))
      numExists++;
    if (theCache.exists("fluff"))
      numExists++;
    if (theCache.exists("overflow")) // Not even existing in this test
      numExists++;
    if (theCache.exists("random"))
      numExists++;
    assertEquals(numExists, 5);
  }

}
