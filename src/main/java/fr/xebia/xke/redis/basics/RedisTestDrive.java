package fr.xebia.xke.redis.basics;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisTestDrive {

    JedisPool connectionPool;

    public void initialisePool() {
        connectionPool = new JedisPool(new JedisPoolConfig(), "ec2-46-137-132-97.eu-west-1.compute.amazonaws.com", 6969, 2000, null);
    }

    public void storeSprinterAsJson(String key, String value) {
        Jedis jedis = connectionPool.getResource();
        jedis.set(key, value);
        connectionPool.returnResource(jedis);
    }

    public void storeKeyValue(String key, String value, int expire) {
        Jedis jedis = connectionPool.getResource();
        jedis.setex(key, expire, value);
        connectionPool.returnResource(jedis);
    }

    public String getSprinterAsJson(String key) {
        Jedis jedis = connectionPool.getResource();
        String result = jedis.get(key);
        connectionPool.returnResource(jedis);
        return result;
    }

    public void closePool() {
        connectionPool.destroy();
    }

    public void storeSprinterAsObject(String name, Sprinter sprinter, int expire) {
        String sprinterAsString = null;
        try {
            sprinterAsString = toString(sprinter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        storeKeyValue(name, sprinterAsString, expire);
    }

    public Sprinter getSprinterAsObject(String name) {
        try {
            return (Sprinter) fromString(getSprinterAsJson(name));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void remove(String name) {
        Jedis jedis = connectionPool.getResource();
        jedis.del(name);
        connectionPool.returnResource(jedis);
    }

    /**
     * Read the object from Base64 string.
     */
    private static Object fromString(String s) throws IOException,
            ClassNotFoundException {
        try {
            byte[] data = Base64Coder.decode(s);

            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Write the object to a Base64 string.
     */
    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String(Base64Coder.encode(baos.toByteArray()));
    }


    public void flushAll() {
        Jedis jedis = connectionPool.getResource();
        jedis.flushAll();
        connectionPool.returnResource(jedis);
    }

    public void storeSprinters(String sprinters, Map<String, String> sprintersMap) {
        Jedis jedis = connectionPool.getResource();
        jedis.hmset(sprinters, sprintersMap);
        connectionPool.returnResource(jedis);
    }

    public List<String> getSprinters(String key, String field) {
        Jedis jedis = connectionPool.getResource();
        List<String> result = jedis.hmget(key, field);
        connectionPool.returnResource(jedis);
        return result;
    }

    public void registerResult(String key, double score, String value) {
        Jedis jedis = connectionPool.getResource();
        jedis.zadd(key, score, value);
        connectionPool.returnResource(jedis);
    }

    public Long countSprinterUnderGivenTime(String key, double score) {
        Jedis jedis = connectionPool.getResource();
        Long result = jedis.zcount(key, 0, score);
        connectionPool.returnResource(jedis);
        return result;
    }

    public String findWinner(String key) {
        Jedis jedis = connectionPool.getResource();
        Set<String> result = jedis.zrange(key, 0, 0);
        connectionPool.returnResource(jedis);
        return result.iterator().next();
    }
}
