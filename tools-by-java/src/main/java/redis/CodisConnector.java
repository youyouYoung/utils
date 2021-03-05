package redis;

import logger.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 功能描述: 线程池工具类
 *
 * @author youyou
 * @date 3/25/20 12:08 PM
 */
public class CodisConnector {
    private static Logger logger = Logger.getLogger(CodisConnector.class, "redis");

    private static int expire;
    private static JedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.valueOf("codis.pool.maxActive"));
        config.setMaxIdle(Integer.valueOf("codis.pool.maxIdle"));
        config.setTestOnBorrow(Boolean.valueOf("codis.pool.testOnBorrow"));
        config.setTestOnReturn(Boolean.valueOf("codis.pool.testOnReturn"));


        pool = new JedisPool(config
                , "codis.ip"
                , Integer.valueOf("codis.port")
                , Integer.parseInt("codis.pool.timeout")
                , "codis.password", 0);

        logger.info("redis.ip: codis.ip");
        expire = Integer.valueOf("codis.expire");
    }

    public CodisConnector() {

    }

    private static Jedis getJedis() {
        return pool.getResource();
    }

    public static long del(String key) {
        Jedis jedis = pool.getResource();
        try {
            return jedis.del(key);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            jedis.close();
        }
        return 0;
    }

    public static long setnx(String key, int expire, String value) {
        Jedis jedis = pool.getResource();
        try {

            if ("OK".equals(jedis.set(key, value, "NX", "EX", expire))) {
                return 1;
            }
            return 0;

        } catch (Exception e) {
            logger.error("codis异常:", e);
        } finally {
            jedis.close();
        }
        return -1L;
    }
}
