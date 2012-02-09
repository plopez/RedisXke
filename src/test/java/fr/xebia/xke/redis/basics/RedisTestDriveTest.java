package fr.xebia.xke.redis.basics;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RedisTestDriveTest extends TestCase {

    private  RedisTestDrive redisTestDrive;

    @Before
    public void setUp(){
        redisTestDrive = new RedisTestDrive();
        redisTestDrive.initialisePool();
        redisTestDrive.flushAll();
    }

    @After
    public void tearDown(){
        redisTestDrive.closePool();
    }

    @Test
    public void testStringStorage() throws Exception {
        String name = "Lemaitre";
        String jsonLemaitre = "{name:Lemaitre, firstName:Christophe, country:FRA}";
        redisTestDrive.storeSprinterAsJson(name, jsonLemaitre);
        assertEquals(redisTestDrive.getSprinterAsJson(name), jsonLemaitre);
        redisTestDrive.remove(name);
        assertNull(redisTestDrive.getSprinterAsJson(name));

    }

    @Test
    public void testObjectStorage() throws Exception {
        String name = "Lemaitre";
        Sprinter lemaitre = new Sprinter("Lemaitre", "Christophe", "FRA");
        int expire = 5;
        redisTestDrive.storeSprinterAsObject(name, lemaitre, expire);
        assertEquals(redisTestDrive.getSprinterAsObject(name), lemaitre);
        Thread.sleep(6000);
        assertNull(redisTestDrive.getSprinterAsObject(name));
    }

    @Test
    public void testHashMapStorage() throws Exception {
        Map<String, String> sprintersMap = new HashMap();
        Sprinter bolt = new Sprinter("Bolt", "Usain", "JAM");
        Sprinter lewis = new Sprinter("Lewis", "Carl", "USA");
        Sprinter lemaitre = new Sprinter("Lemaitre", "Christophe", "FRA");
        sprintersMap.put("bolt", bolt.toString());
        sprintersMap.put("lewis", lewis.toString());
        sprintersMap.put("lemaitre", lemaitre.toString());
        redisTestDrive.storeSprinters("sprinters", sprintersMap);
        List<String> expected = new ArrayList<String>();
        expected.add(lewis.toString());
        assertEquals(expected, redisTestDrive.getSprinters("sprinters", "lewis"));
        redisTestDrive.remove("sprinters");
    }

    @Test
    public void testSortedSet() throws Exception {
        redisTestDrive.registerResult("results", 9.70, "lewis");
        redisTestDrive.registerResult("results", 9.98, "lemaitre");
        redisTestDrive.registerResult("results", 9.60, "bolt");
        assertEquals(2, redisTestDrive.countSprinterUnderGivenTime("results", 9.75).longValue());
        assertEquals("bolt", redisTestDrive.findWinner("results"));
        redisTestDrive.remove("results");


    }


}
