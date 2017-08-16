package priv.jason.memcache;

import java.util.Date;
import net.rubyeye.xmemcached.MemcachedClientStateListener;
import net.rubyeye.xmemcached.exception.MemcachedException;

public abstract class AbstractMemcached {
	private int connectPoolSize;        
	private boolean primitiveAsString;
	private int connectTimeout;
	
	/**
	 * 获取Memcached客户端实例
	 * @param serverAddress
	 * @return
	 */
	public static AbstractMemcached getInstance(String serverAddress, int[] weights) {
		return new MemcachedImpl(serverAddress, weights);
	}
	
	/**
	 * 添加memcached状态监听
	 * @param listener
	 */
	public abstract void addStateListener(MemcachedClientStateListener listener);
	
	/**
	 * memcached初始化
	 * 
	 */
	public abstract void init()
	throws MemcachedException;
	
	/**
	 * 存储
	 * @param key
	 * @param value
	 * @param expTime
	 */
	public abstract void set(String key, Object value, int expTime)
	throws MemcachedException;
	
	/**
	 * 获取
	 * @param key
	 */
	public abstract Object get(String key, int opTimeout)
	throws MemcachedException;
	
	/**
	 * 获取
	 * @param key
	 * @param opTimeout
	 * @return
	 */
	public abstract Object getReal(String key, int opTimeout)
	throws MemcachedException;
	
	/**
	 * 删除
	 * @param key
	 */
	public abstract void delete(String key)
	throws MemcachedException;
	
	/**
	 * 自增
	 * @param key
	 */
	public abstract void incr(String key)
	throws MemcachedException;
	
	public abstract void add(String key, Object value, Date expiryDate)
	throws MemcachedException;
	
	
	public abstract void shutdown();
	
	public int getConnectPoolSize() {
		return connectPoolSize;
	}

	public void setConnectPoolSize(int connectPoolSize) {
		this.connectPoolSize = connectPoolSize;
	}

	public boolean isPrimitiveAsString() {
		return primitiveAsString;
	}

	public void setPrimitiveAsString(boolean primitiveAsString) {
		this.primitiveAsString = primitiveAsString;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
}
