package priv.jason.memcache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 默认缓存实现
 * @author caohuihui
 *
 */
@SuppressWarnings("unchecked")
public class DefaultCacheImpl implements ICache<String, Object> {
	private static transient Logger logger = LoggerFactory.getLogger(DefaultCacheImpl.class);
	// 缓存区
	ConcurrentHashMap<String, Object>[] caches;
	// 缓存失效信息
	ConcurrentHashMap<String, Long> expiryCache;
	// 缓存管理线程池
	private  ScheduledExecutorService scheduleService;
	// 失效时间扫描间隔
	private int expiryInterval = 10;
	// 缓存区默认模区
	private int moduleSize = 10;	
	
	public DefaultCacheImpl() {
		init();
	}
	
	public DefaultCacheImpl(int expiryInterval, int moduleSize) {
		this.expiryInterval = expiryInterval;
		this.moduleSize = moduleSize;
		init();
	}

	/**
	 * 缓存初始化
	 */
	private void init() {
		caches = new ConcurrentHashMap[moduleSize];		
		for(int i = 0 ; i < moduleSize ;i ++)
			caches[i] = new ConcurrentHashMap<String, Object>();		
		expiryCache = new ConcurrentHashMap<String, Long>();		
		scheduleService = Executors.newScheduledThreadPool(1);		
		scheduleService.scheduleAtFixedRate(new CheckOutOfDateSchedule(caches, expiryCache), 
				0, expiryInterval * 60, TimeUnit.SECONDS);
		
		if (logger.isInfoEnabled())
			logger.info("[pook default cache check service is running...]");
	}
	
	public boolean clear() {
		if (caches != null)
			for(ConcurrentHashMap<String, Object> cache : caches) {
				cache.clear();
			}
		
		if (expiryCache != null)
			expiryCache.clear();
		
		return true;
	}

	public boolean containsKey(String key) {
		checkValidate(key);
		return getCache(key).containsKey(key);
	}

	public Object get(String key) {
		checkValidate(key);
		return getCache(key).get(key);
	}
	
	public Set<String> keySet() {
		checkAll();
		return expiryCache.keySet();
	}

	public Object put(String key, Object value) {
		Object result = getCache(key).put(key, value);
		expiryCache.put(key, (long) -1);		
		return result;
	}

	public Object put(String key, Object value, Date expiry) {
		Object result = getCache(key).put(key, value);
		expiryCache.put(key, expiry.getTime());		
		return result;
	}

	public Object remove(String key) {
		Object result = getCache(key).remove(key);
		expiryCache.remove(key);
		return result;
	}

	public int size() {
		checkAll();		
		return expiryCache.size();
	}

	public Collection<Object> values() {
		checkAll();		
		Collection<Object> values = new ArrayList<Object>();		
		for(ConcurrentHashMap<String, Object> cache : caches) {
			values.addAll(cache.values());	
		}

		return values;
	}
	
	private ConcurrentHashMap<String, Object> getCache(String key) {
		long hashCode = (long)key.hashCode();
		
		if (hashCode < 0)
			hashCode = -hashCode;
		
		int moudleNum = (int)hashCode % moduleSize;
		
		return caches[moudleNum];
	}
	
	private void checkValidate(String key) {
		if (expiryCache.get(key) != null && expiryCache.get(key) != -1 
				&& new Date(expiryCache.get(key)).before(new Date())) {
			getCache(key).remove(key);
			expiryCache.remove(key);
		}
	}
	
	private void checkAll() {
		Iterator<String> iter = expiryCache.keySet().iterator();
		
		while(iter.hasNext()) {
			String key =  iter.next();
			checkValidate(key);
		}
	}
	
	class CheckOutOfDateSchedule implements java.lang.Runnable {
		ConcurrentHashMap<String, Object>[] caches;
		ConcurrentHashMap<String, Long> expiryCache;
		
		public CheckOutOfDateSchedule(ConcurrentHashMap<String, Object>[] caches,
				ConcurrentHashMap<String, Long> expiryCache) {
			this.caches = caches;
			this.expiryCache = expiryCache;
		}
		

		public void run() {
			check();
		}
		
		public void check() {
			try {
				for(ConcurrentHashMap<String, Object> cache : caches) {
					Iterator<String> keys = cache.keySet().iterator();
					
					while(keys.hasNext()) {
						String key = keys.next();
						
						if (expiryCache.get(key) == null)
							continue;
						
						long date = expiryCache.get(key);
						
						if ((date > 0)&&(new Date(date).before(new Date()))) {
							expiryCache.remove(key);
							cache.remove(key);
						}
							
					}
					
				}
			} catch(Exception e) {
				logger.error("[pook default cache check service exception occurred...]", e);
			}
		}
		
	}

	
	public Object put(String key, Object value, int TTL) {
		Object result = getCache(key).put(key, value);
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, TTL);
		expiryCache.put(key,calendar.getTime().getTime());
		
		return result;
	}

	public void destroy()  {
		try {
			clear();
			
			if (scheduleService != null)
				scheduleService.shutdown();
			
			scheduleService = null;
		} catch(Exception e) {
			logger.error("[pook default cache check service exception occurred...]", e);
		}
	}

}
