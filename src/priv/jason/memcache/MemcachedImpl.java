package priv.jason.memcache;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;



import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.MemcachedClientStateListener;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;


public final class MemcachedImpl extends AbstractMemcached {
	// default nio connection pool size 
	private final static int defaultPoolSize = 5;
	// default PrimitiveAsString false
	private final static boolean defaultPrimitiveAsString = true;
	// default Connect Time out
	private final static int defaultConnectTimeout = 1000 * 3;	
	private MemcachedClientBuilder builder;
	private MemcachedClient memcachedClient;
	private final static ICache<String, Object> cache = new DefaultCacheImpl();
	
	public MemcachedImpl(String serverAddress, int[] weights) {
		builder = new XMemcachedClientBuilder(
				AddrUtil.getAddressMap(serverAddress), weights);		
		builder.setConnectionPoolSize(getConnectPoolSize() == 0 ? defaultPoolSize : getConnectPoolSize());
		builder.setFailureMode(true);
		builder.setSessionLocator(new KetamaMemcachedSessionLocator());
	}

	/**
	 * 
	 * 
	 */
	public void init() 
	throws MemcachedException {
		try {
			memcachedClient = builder.build();
			memcachedClient.setPrimitiveAsString(defaultPrimitiveAsString);
			memcachedClient.setConnectTimeout((getConnectTimeout() == 0) ? 
					defaultConnectTimeout : getConnectTimeout());
			memcachedClient.setOpTimeout(1500);
		} catch (IOException e) {
			throw new MemcachedException(e);
		}
	}

	public void addStateListener(MemcachedClientStateListener listener) {
		builder.addStateListener(listener);
	}

	public void set(String key, Object value, int expTime) 
	throws MemcachedException {
		try {
			cache.put(key, value, expTime);
			memcachedClient.set(key, expTime, value);
		} catch (TimeoutException e) {
			throw new MemcachedException(e);
		} catch (InterruptedException e) {
			throw new MemcachedException(e);
		} catch (MemcachedException e) {
			throw new MemcachedException(e);
		}
	}
	
	public Object getReal(String key, int opTimeout) 
	throws MemcachedException {
		try {
			Object value = memcachedClient.get(key);
			return value;
		} catch (TimeoutException e) {
			throw new MemcachedException(e);
		} catch (InterruptedException e) {
			throw new MemcachedException(e);
		} catch (MemcachedException e) {
			throw new MemcachedException(e);
		}		
	}

	public Object get(String key, int opTimeout) 
	throws MemcachedException {
		Object value = cache.get(key);
		if (value != null) {
			return value;
		} else {
			try {
				value = memcachedClient.get(key);
				return value;
			} catch (TimeoutException e) {
				throw new MemcachedException(e);
			} catch (InterruptedException e) {
				throw new MemcachedException(e);
			} catch (MemcachedException e) {
				throw new MemcachedException(e);
			}
		}
	}

	public void delete(String key) 
	throws MemcachedException {
		try {
			cache.remove(key);
			memcachedClient.delete(key);
		} catch (TimeoutException e) {
			throw new MemcachedException(e);
		} catch (InterruptedException e) {
			throw new MemcachedException(e);
		} catch (MemcachedException e) {
			throw new MemcachedException(e);
		}
	}

	public void incr(String key) 
	throws MemcachedException {
		try {			
			memcachedClient.incr(key, 1);
		} catch (TimeoutException e) {
			throw new MemcachedException(e);
		} catch (InterruptedException e) {
			throw new MemcachedException(e);
		} catch (MemcachedException e) {
			throw new MemcachedException(e);
		}
	}
	
	public void add(String key, Object value, Date expiryDate) 
	throws MemcachedException {
		try {
			cache.put(key, value, expiryDate);
			memcachedClient.add(key, (int) expiryDate.getTime(), value);
		} catch (TimeoutException e) {
			throw new MemcachedException(e);
		} catch (InterruptedException e) {
			throw new MemcachedException(e);
		} catch (MemcachedException e) {
			throw new MemcachedException(e);
		}
	}
	
	@Override
	public void shutdown() {
		try {
			memcachedClient.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
