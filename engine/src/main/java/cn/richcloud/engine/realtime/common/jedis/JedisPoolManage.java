package cn.richcloud.engine.realtime.common.jedis;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.richcloud.engine.realtime.common.utils.RealTimeConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * jedis内存数据库管理器
 * @author Administrator
 *
 */
public class JedisPoolManage {
	 private static JedisHostAndPortUtil.JHostAndPort master = JedisHostAndPortUtil.getRedisServers(RealTimeConfig.REDIS_MASTER) ;
	 private static JedisHostAndPortUtil.JHostAndPort slave1 = JedisHostAndPortUtil.getRedisServers(RealTimeConfig.REDIS_SLAVE1) ;
		private final static Log LOGGER = LogFactory.getLog(JedisPoolManage.class.getClass());
	   private Map<String,JedisPool> jedispoolMap = new HashMap<String,JedisPool>();
	 private static JedisPoolManage  instance = new JedisPoolManage();
	 public static JedisPoolManage getInstance(){
		 	return instance;
	 }
	
	 
	 private   JedisPoolManage(){
		 LOGGER.info("#########开始连接redis内存数据库###########");
		 System.out.println("#########开始连接redis内存数据库###########");
		  LOGGER.info("\n");
//		 JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		  JedisPool  pool = new JedisPool(new JedisPoolConfig(), master.host,
	                master.port, master.timeout);
		     jedispoolMap.put(RealTimeConfig.REDIS_MASTER, pool);
		     
		     pool = new JedisPool(new JedisPoolConfig(), slave1.host,
		    		 slave1.port, slave1.timeout);
			     jedispoolMap.put(RealTimeConfig.REDIS_SLAVE1, pool);
		     LOGGER.info("\n");
		     System.out.println("#########连接redis内存数据库完成###########");
		     System.out.println(jedispoolMap.size()+"\n");
	     LOGGER.info("#########连接redis内存数据库完成###########");
	 }
	 
	public void checkConnections() {
    
        Jedis jedis = this.getResource(RealTimeConfig.REDIS_MASTER);
        jedis.set("foo", "bar");
       String a = jedis.get("foo");
       System.out.println(a);
       this.returnResource(RealTimeConfig.REDIS_MASTER,jedis);
   
    }
	/**
	 * 取得Jedis操作对象
	 * @return
	 */
	public Jedis getResource(String redisName ){
		
		JedisPool pool = jedispoolMap.get(redisName);
		if(pool == null ) {
			return null;
		}
		Jedis	jedis = pool.getResource();
		System.out.println("poolName--==="+redisName+"||||"+(jedis!=null));
		jedis.auth(master.password);
		return jedis;
	}
	
	/**
	 * 释放对象回连接池
	 * @param jedis
	 */
	public void returnResource(String redisName ,Jedis jedis){
		JedisPool pool = jedispoolMap.get(redisName);
		if(pool == null ) {
			return;
		}
		 pool.returnResource(jedis);
	}
	
	/**
	 * 连接池销毁
	 */
	public void destroy( String redisName){
		JedisPool pool = jedispoolMap.get(redisName);
		if(pool == null ) {
			return;
		}
		synchronized (pool) {
			if( pool != null ){
				pool.destroy();
				pool = null;
				jedispoolMap.remove(redisName);
			}
		}
		
	}
	
	
	/**
	 * 连接池销毁
	 */
	public void destroyAll(  ){
		for( Iterator< Map.Entry<String, JedisPool>> it = jedispoolMap.entrySet().iterator() ; it.hasNext();){
			Map.Entry<String, JedisPool> entry =it.next();
			JedisPool pool =entry.getValue();
		   String redisName = entry.getKey();
			synchronized (pool) {
				if( pool != null ){
					pool.destroy();
					pool = null;
					it.remove();
				}
			}
		}
	}
	
	
/*	public static void main(String [] str ){
		JedisPoolManage.getInstance().checkConnections();
		Jedis jedis = JedisPoolManage.getInstance().getResource(RealTimeConfig.REDIS_MASTER);
		jedis.set("我","1");
		 JedisPoolManage.getInstance().returnResource(RealTimeConfig.REDIS_MASTER,jedis);
		JedisPoolManage.getInstance().destroy(RealTimeConfig.REDIS_MASTER);
	}*/
	
	
}
