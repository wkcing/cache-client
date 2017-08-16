package priv.jason.memcache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Tuple;

public final class RedisUtil {
	
	/**
	 * 可用连接实例的最大数目，默认值为8；
	 * 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
	 */
	private static int MAX_TOTAL = 10;

	/**
	 * 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
	 */
	private static int MAX_IDLE = 10;

	/**
	 * 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
	 */
	private static long MAX_WAIT_MS = 10000;
	
	/**
	 * 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
	 */
	private static boolean TEST_ON_BORROW = true;
	
	/**
	 * Redis连接池
	 */
	private static JedisPool jedisPool = null;
	
	public static void connect(String address) {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(MAX_TOTAL);
			config.setMaxIdle(MAX_IDLE);
			config.setMaxWaitMillis(MAX_WAIT_MS);
			config.setTestOnBorrow(TEST_ON_BORROW);
			String[] redis = address.split(":");
			jedisPool = new JedisPool(config, redis[0], Integer.parseInt(redis[1]), Protocol.DEFAULT_TIMEOUT, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void disconnect() {
		if (jedisPool != null) {
			jedisPool.close();
			jedisPool = null;
		}
	}
	
	/**
	 * 获取Jedis实例
	 */
	public static Jedis getJedisInstance() {
		try {
			return jedisPool.getResource();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 释放jedis资源
	 */
	public static void closeJedis(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}
	
	/**
	 * set,设置值,包含所有类型
	 * @param key
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void set(String prefix, Object key, Object value) {
		Jedis jedis = getJedisInstance();
		try {
			if (value instanceof String) {
				jedis.set(prefix + key, value.toString());
			} else
			if (value instanceof Map<?, ?>) {
				jedis.hmset(prefix + key, (Map<String, String>)value);
			} else
			if (value instanceof List<?>) {
				for (String str : (List<String>) value) {
					jedis.lpush(prefix + key, str);
				}
			} else
			if (value instanceof Set<?>) {
				for (String str : (Set<String>) value) {
					jedis.sadd(prefix + key, str);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * get,获取String
	 * @param key
	 * @param value
	 * @return String
	 */
	public static String get(String prefix, Object key) {
		Jedis jedis = getJedisInstance();
		try {
			return jedis.get(prefix + key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * lrange,根据下标范围获取list中的值
	 * @param key
	 * @param start 从0开始
	 * @param end -1为全部
	 * @return List
	 */
	public List<String> lrange(String prefix, Object key, long start, long end) {
		Jedis jedis = getJedisInstance();
		try {
			return jedis.lrange(prefix + key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * sort,根据key排序list或者set
	 * @param key
	 * @return List
	 */
	public List<String> sort(String prefix, Object key) {
		Jedis jedis = getJedisInstance();
		try {
			return jedis.sort(prefix + key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * zadd,放值到有序集合，集合中无此value中时增加，有则更新score
	 * @param key
	 * @param score 分数
	 * @param member 集合成员
	 */
	public static void zadd(String prefix, Object key, long score, String member) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.zadd(prefix + key, score, member);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * zadd,放值到有序集合，集合中无此value中时增加，有则更新score
	 * @param key
	 * @param score 分数
	 * @param member 集合成员
	 */
	public static void zadd(String key, long score, String member) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.zadd(key, score, member);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 获取有序集合的值,按照score从大到小的顺序
	 * @param key
	 * @param start 从0开始
	 * @param end -1为全部
	 */
	public static Set<String> zrevrange(String prefix, Object key, int start, int end) {
		Jedis jedis = getJedisInstance();
		try {
			return jedis.zrevrange(prefix + key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 获取有序集合的成员和分数,按照score从大到小的顺序
	 * @param key
	 * @param start 从0开始
	 * @param end -1为全部
	 */
	public static Set<Tuple> zrevrangeWithScores(String prefix, Object key, int start, int end) {
		Jedis jedis = getJedisInstance();
		try {
			return jedis.zrevrangeWithScores(prefix + key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 获取有序集合的成员和分数,按照score从大到小的顺序,固定key值
	 * @param key
	 * @param start 从0开始
	 * @param end -1为全部
	 */
	public static String zrevrangeWithScores(String key) {
		Jedis jedis = getJedisInstance();
		StringBuilder sb = new StringBuilder();
		try {
			Set<Tuple> set = jedis.zrevrangeWithScores(key, 0, -1);
			if (set != null) {
				for (Tuple tuple : set) {
					sb.append(tuple.getElement());//带前缀的UserId
					sb.append(" -- ");
					sb.append((int) (((long)tuple.getScore()) >>> 32));//分数时间处理score
					sb.append("\n");
				}
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 删除指定key
	 * @param prefix
	 * @param key
	 */
	public static void delete(String prefix, Object key) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.del(prefix + key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 删除指定key,固定key
	 * @param key
	 */
	public static void delete(String key) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 设置指定key的过期时间,从调用开始的指定秒数后删除value
	 * @param key
	 * @prama seconds 过期时间，单位秒
	 */
	public static void expire(String prefix, Object key, int seconds) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.expire(prefix + key, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 从有序集合中删除指定值
	 * @param prefix
	 * @param key
	 * @param members 指定的member数组
	 */
	public static void zrem(String prefix, Object key, String... members) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.zrem(prefix + key, members);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 从有序集合中删除指定值
	 * @param members 指定的member数组
	 */
	public static void zrem(String prefix, String... members) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.zrem(prefix, members);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 在list的左边添加元素
	 * @param strings 指定的元素数组
	 */
	public static void lpush(String key, String... strings) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.lpush(key, strings);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 在list的右边取出一个元素
	 * @param key
	 */
	public static String rpop(String key) {
		Jedis jedis = getJedisInstance();
		try {
			return jedis.rpop(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 在list的右边取出多个元素
	 * @param args 注意此args的长度必须为2, 第一个为指定的key,第二个为超时时间:timeout,单位为秒 
	 */
	public static List<String> brpop(String... args) {
		Jedis jedis = getJedisInstance();
		try {
			return jedis.brpop(args);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 对指定member增加分数
	 * @param prefix
	 * @param key
	 * @param score 增加的分数
	 * @param member
	 */
	public static void zincrby(String prefix, Object key, int score, String member) {
		Jedis jedis = getJedisInstance();
		try {
			jedis.zincrby(prefix + key, score, member);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 对指定member增加分数,附带时间
	 * @param prefix
	 * @param key
	 * @param add
	 * @param member
	 */
	public static void zincrbyWithTime(String key, String value) {
		Jedis jedis = getJedisInstance();
		try {
			String[] arr = value.split("-");
			Double dscore = jedis.zscore(key, arr[0]);
			if (dscore == null) {
				dscore = Double.valueOf(0);
			}
			long score = (dscore.longValue() >>> 32) + Integer.parseInt(arr[1]);
			jedis.zadd(key, score << 32 | (Integer.MAX_VALUE - System.currentTimeMillis()/1000), arr[0]);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 获取指定成员分数
	 * @param prefix
	 * @param key
	 * @param member
	 */
	public static int zscore(String prefix, Object key, String member) {
		Jedis jedis = getJedisInstance();
		try {
			Double dscore = jedis.zscore(prefix + key, member);
			if (dscore == null) {
				return 0;
			}
			return (int) (dscore.longValue() >>> 32) ;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 获取指定成员排名下标(从0开始)
	 * @param prefix
	 * @param key
	 * @param member
	 */
	public static int zrank(String prefix, Object key, String member) {
		Jedis jedis = getJedisInstance();
		try {
			Long lrank = jedis.zrevrank(prefix + key, member);
			if (lrank == null) {
				return -1;
			}
			return lrank.intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			closeJedis(jedis);
		}
	}
	
}