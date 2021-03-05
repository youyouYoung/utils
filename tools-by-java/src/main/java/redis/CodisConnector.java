package redis;

import logger.Logger;
import property.CustomProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * 功能描述: 线程池工具类
 *
 * @author youyou
 * @date 3/25/20 12:08 PM
 */
public class CodisConnector {
    private static Logger logger = Logger.getLogger(CodisConnector.class, "redis");

    private static JedisPool pool;

    static {
        Properties properties = CustomProperties.getProperties();

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.valueOf(properties.getProperty("redis.pool.maxActive")));
        config.setMaxIdle(Integer.valueOf(properties.getProperty("redis.pool.maxIdle")));


        pool = new JedisPool(config
                , properties.getProperty("redis.ip")
                , Integer.valueOf(properties.getProperty("redis.port"))
                , Integer.parseInt(properties.getProperty("redis.pool.timeout"))
                , properties.getProperty("redis.password"), 0);

        logger.info("redis.ip: redis.ip");
    }

    private static Jedis getJedis() {
        return pool.getResource();
    }

    public static long del(String key) {
        Jedis jedis = getJedis();
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
        Jedis jedis = getJedis();
        try {

            if ("OK".equals(jedis.set(key, value, "NX", "EX", expire))) {
                return 1;
            }
            return 0;

        } catch (Exception e) {
            logger.error("redis异常:", e);
        } finally {
            jedis.close();
        }
        return -1L;
    }
}
