package priv.jason.memcache;

import java.util.Date;

import net.rubyeye.xmemcached.exception.MemcachedException;

public final class MemcachedUtil {	
	private static AbstractMemcached memcached;
	// 默认cache失效时间 in second  60 * 60
	private final static int cacheExpireTime = 60 * 60 * 2;
	// 默认get操作超时时间
	private final static int defaultGetOpTimeout = 1000 * 3;	
	
	public static void connect(String address) {
		if (memcached == null) {
			memcached = AbstractMemcached.getInstance(address, new int[]{5});
			// 状态监听器
			memcached.addStateListener(new MemcachedListener());
			try {
				memcached.init();
			} catch (MemcachedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (memcached == null) {
				throw new NullPointerException("Null MemcachedClient,please check memcached has been started");
			}
		}
	}
	
	public static void disconnect() {
		if (memcached != null) {
			memcached.shutdown();
			memcached = null;
		}
	}

	public static void set(String key, Object value) 
	throws MemcachedException {		
		memcached.set(key, value, cacheExpireTime);
	}
	
	public static void set(String key, Object value, int expireTime) 
	throws MemcachedException {
		memcached.set(key, value, expireTime);
	}
	
	public static void incr(String key) 
	throws MemcachedException {
		memcached.incr(key);
	}
	
	public static Object get(String key) 
	throws MemcachedException {
		return memcached.get(key, defaultGetOpTimeout);
	}
	
	public static Object getReal(String key) 
	throws MemcachedException {
		return memcached.getReal(key, defaultGetOpTimeout);
	}
	
	public static Object getReal(String key, int opTimeout) 
	throws MemcachedException {
		return memcached.getReal(key, opTimeout);
	}
	
	public static Object get(String key, int opTimeout) 
	throws MemcachedException {
		return memcached.get(key, opTimeout);
	}
	
	public static void delete(String key) 
	throws MemcachedException {
		memcached.delete(key);
	}
	
	public static void add(String key, Object value, Date expireTime) 
	throws MemcachedException {
		memcached.add(key, value, expireTime);
	}
	
}
