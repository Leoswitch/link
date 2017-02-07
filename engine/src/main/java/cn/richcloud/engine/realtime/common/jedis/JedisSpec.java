package cn.richcloud.engine.realtime.common.jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.richcloud.engine.realtime.common.utils.RealTimeConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONValue;

import redis.clients.jedis.Jedis;


/**
 * 实现内存数据库对象的管理
 * 一个spec对应一个表
 * id代表一条数据的主键
 * value代表一行数据（通常是一个map）
 * 默认分对象作内存对象的key时
 * map = key #m#
 * set = key #s#
 * list = key #l#
 * @author Administrator
 *
 */
public abstract class JedisSpec{
	protected final static Log LOGGER = LogFactory.getLog(JedisPoolManage.class.getClass());

	private static JedisHostAndPortUtil.JHostAndPort master = JedisHostAndPortUtil.getRedisServers(RealTimeConfig.REDIS_MASTER) ;
	private static JedisHostAndPortUtil.JHostAndPort slave1 = JedisHostAndPortUtil.getRedisServers(RealTimeConfig.REDIS_SLAVE1) ;
//	private   Jedis jedis =null;
	private ThreadLocal<Jedis> jedisThreadLocal = new ThreadLocal<Jedis>();

	public  Jedis getJedis() {
		Jedis jedis = jedisThreadLocal.get();
		if(jedis==null){
				synchronized(this){
				JedisHostAndPortUtil.JHostAndPort hostAndPort = getResource(this.getJedisPoolName());
				jedis = new Jedis(hostAndPort.host,hostAndPort.port);
				jedis.auth(hostAndPort.password);
				jedisThreadLocal.set(jedis);
				System.out.println(Thread.currentThread().getName()+":poolName>>>=="+this.getJedisPoolName()+"||||"+(jedis!=null));
			}
		}
		return jedis;
	}

	public JedisSpec( ){
//		JedisHostAndPortUtil.JHostAndPort hostAndPort = getResource(this.getJedisPoolName());
//		System.out.println("poolName>>>=="+this.getJedisPoolName()+"||||"+(getJedis()!=null));
//		this.jedis =  JedisPoolManage.getInstance().getResource(this.getJedisPoolName());
		getJedis();
	}

	private JedisHostAndPortUtil.JHostAndPort getResource(String poolName){
		JedisHostAndPortUtil.JHostAndPort ret = null;
		if(RealTimeConfig.REDIS_MASTER.equalsIgnoreCase(poolName)){
			ret = master;
		}else if(RealTimeConfig.REDIS_SLAVE1.equalsIgnoreCase(poolName)){
			ret = slave1;
		}
		return ret;
	}

	/**
	 * 取得对象
	 * @param key
	 * @param id
	 * @return
	 */
	protected String hget(String key, String id) {
		if(getIsMapsplit( )) {
			key = toSplitMap(key , id);
		}
		return this.getJedis().hget(key,id);
	}
	/**
	 * 取得对象
	 * @param key
	 * @param id
	 * @return
	 */
	public String hget(  String id) {
		return this.hget(this.getObjectKey() , id);
	}

	/**
	 * 取得对象
	 * @param key
	 * @param id
	 * @return
	 */
	public Object hgetObject(  String id) {
		String str = this.hget(this.getObjectKey() , id);
		if(str == null ) {
			return null;
		}
		return  JSONValue.parse(str);
	}

	/**
	 * 保存map对象
	 * @param key
	 * @param id
	 * @return
	 */
	public Long hsetObject( String id ,Object value) {
		return this.hset( id, JSONValue.toJSONString(value));
	}
	/**
	 * 保存String对象
	 * @param key
	 * @param id
	 * @return
	 */
	public Long hset( String id ,String value) {
		return this.hset(this.getObjectKey(),id,value);
	}

	/**
	 * 保存map对象
	 * @param key
	 * @param id
	 * @return
	 */
	protected Long hset(String key, String id ,String value) {
		if(getIsMapsplit( )) {
			key = toSplitMap(key, id);
		}
		return this.getJedis().hset(key,id,value);
	}
	/**
	 * 删除map对象
	 * @param key
	 * @param id
	 * @return
	 */
	public Long hdel( String id ) {
		return this.hdel(this.getObjectKey(),id );
	}

	/**
	 * 删除map对象
	 * @param key
	 * @param id
	 * @return
	 */
	protected Long hdel(String key, String id ) {
		if(getIsMapsplit( )) {
			key = toSplitMap(key, id);
		}
		return this.getJedis().hdel(key,id);
	}

	/**
	 * map对象是否存在
	 */
	protected boolean hexists(String key, String id) {
		if(getIsMapsplit( )){
			key = toSplitMap(key , id);
		}
		return this.getJedis().hexists(key,id );
	}
	/**
	 * map对象是否存在
	 */
	public boolean hexists(  String id) {
		return this.hexists(this.getObjectKey() , id);
	}

	/**
	 * 删除所有的Map
	 * @param key
	 * @return
	 */
	public   void hdelAll(   ){
		String key = this.getObjectKey();
		Set<String> set = this.getJedis().keys(key + getMapKeySplit() + "*");
		LOGGER.info("所有前缀为 key " + key + " 的主键为 " + set.toString());
		if(getIsMapsplit( )){
			LOGGER.info("删除主键为 " +  set.toString());
			if(set.size() > 0 ) {
				this.getJedis().del(set.toArray(new String[]{}) );
			}
		} else {
			LOGGER.info("删除主键为 " + key);
				this.getJedis().del(key );
		}
	}
	/**
	 * 取所有的Map
	 * @param key
	 * @return
	 */
	public   Map<String,Map<String, String>> hgetAllObject(   ){
		String key = this.getObjectKey();
		Set<String> set = this.getJedis().keys(key + getMapKeySplit() + "*");
		LOGGER.info("所有前缀为 key " + key + " 的主键为 " + set.toString());
		Map<String,Map<String, String>>  result = new HashMap<String,Map<String, String>> ();
		if(getIsMapsplit( )){
			for(String id : set ){
				Map<String, String> o = this.getJedis().hgetAll(id);
				for(Map.Entry<String, String>  entry : o.entrySet()) {
					result.put( entry.getKey(),(Map<String,String>)JSONValue.parse( entry.getValue()));
				}
			}
		} else {
			Map<String, String> o = this.getJedis().hgetAll(key);
			for(Map.Entry<String, String>  entry : o.entrySet()) {
				result.put(entry.getKey(), (Map<String,String>)JSONValue.parse( entry.getValue()));
			}
		}
		return result;
	}

	/**
	 * 取所有的Map
	 * @param key
	 * @return
	 */
	public   Map<String,String> hgetAll(   ){
		String key = this.getObjectKey();
		Set<String> set = this.getJedis().keys(key + getMapKeySplit() + "*");
		LOGGER.info("所有前缀为 key " + key + " 的主键为 " + set.toString());
		Map<String,String>  result = new HashMap<String,String> ();
		if(getIsMapsplit( )){
			for(String id : set ){
				Map<String, String> o = this.getJedis().hgetAll(id);
				for(Map.Entry<String, String>  entry : o.entrySet()) {
					result.put( entry.getKey(),(String)entry.getValue());
				}
				
			}
		} else {
			Map<String, String> o = this.getJedis().hgetAll(key);
			for(Map.Entry<String, String>  entry : o.entrySet()) {
				result.put(entry.getKey(), (String)entry.getValue());
			}
		}
		return result;
	}
	/**
	 * 保存set对象
	 * @param key
	 * @param id
	 * @return
	 */
	public Long sadd(  String setId ,String...  members) {
		String key = this.getObjectKey();
		return this.getJedis().sadd(getSkey(  key ,  setId), members);
	}
	/**
	 * set对象中是否存在指定值
	 * @param key
	 * @param id
	 * @return
	 */
	public boolean sismember(  String setId ,String   member ) {
		String key = this.getObjectKey();
		return this.getJedis().sismember( getSkey(  key ,  setId), member );
	}

	/**
	 * 返回set对象
	 * @param key
	 * @param id
	 * @return
	 */
	public Set<String> smembers(  String setId  ) {
		String key = this.getObjectKey();
		return this.getJedis().smembers( getSkey(  key ,  setId)  );
	}
	/**
	 * 删除set对象的值
	 */
	public   void sdel(  String setId  ){
		String key = this.getObjectKey();
		key = getSkey(  key ,  setId);
		 LOGGER.debug("删除主键为 " + key);
		  this.getJedis().del(key );
	}
	/**
	 * 删除set对象所有的值
	 */
	public   void sdelAll(   ){
		String key = this.getObjectKey();
		Set<String> set = this.getJedis().keys(key +  getSetKeySplit() + "*");
		LOGGER.debug("所有前缀为 key " + key + " 的主键为 " + set.toString());
		 LOGGER.debug("删除主键为 " +  set.toString());
		 if(set.size() > 0) {
			 this.getJedis().del(set.toArray(new String[]{}) );
		 }

	}
	/**
	 * 取得set对象所有的值
	 */
	public   Map<String,Set<String>> sgetAll(   ){
		String key = this.getObjectKey();
		Set<String> set = this.getJedis().keys(key +  getSetKeySplit() + "*");
		Map<String,Set<String>>  map= new HashMap<String,Set<String>>();
		 for( String id : set ) {
			 Set<String> mem =  this.getJedis().smembers( id  );
			 map.put(id.replaceAll(this.getObjectKey()+this.getSetKeySplit(), "" ), mem);
		 }
		 return map;
	}

	/**
	 * 往list里面存放数据
	 * @param value
	 * @return
	 */
	public void lpushObject ( String id ,Object  value) {
		  this.lpush(id , JSONValue.toJSONString(value));

	}
	/**
	 * 往list里面存放数据
	 * @param value
	 * @return
	 */
	public void lpush (String id , String value) {
		   this.getJedis().lpush(this.getObjectKey()+ this.getListKeySplit() + id, value);

	}

	/**
	 * 取得list里面所有的数据
	 * @return
	 */
	public Map<String ,List<String>> lgetAll (  ) {
		Set<String> set = this.getJedis().keys(this.getObjectKey() +  getListKeySplit() + "*");
		Map<String ,List<String>> result = new HashMap<String ,List<String>>();
		for(String key : set ) {
			long len =  this.getJedis().llen(key);
			List<String>  value = this.getJedis().lrange(key, 0,len);
 
			result.put(key.replace(this.getObjectKey()+ this.getListKeySplit(), ""), value);
		}
		return result;
	}
	
	/**
	 * 取得list里面所有的数据
	 * @return
	 */
	public Map<String ,List<Object>> lgetAllObject (  ) {
		Set<String> set = this.getJedis().keys(this.getObjectKey() +  getListKeySplit() + "*");
		Map<String ,List<Object>> result = new HashMap<String ,List<Object>>();
		for(String key : set ) {
			long len =  this.getJedis().llen(key);
			List<String>  value = this.getJedis().lrange(key, 0,len);
			List<Object> list = new ArrayList<Object>();
			for(String v : value ){
				list.add( JSONValue.parse(v));
			}
			result.put(key.replace(this.getObjectKey()+ this.getListKeySplit(), ""), list);
		}
		return result;
	}

	/**
	 * 删除list里面所有的数据
	 */
	public void ldelAll(  ) {
		 this.getJedis().del(this.getObjectKey()+this.getListKeySplit() );
	}
/**
 * 当map对象太大时，要把一个对象分拆成多个map
 * 如key = aa
 * 拆成 aa+01
 *         aa+02
 * @param key
 * @return
 */
	protected String toSplitMap(String key, String id  ){
		return key;
	}
	public  abstract  String getObjectName( );
	public  abstract  String getObjectKey( );//保存对象的 key值
	public  abstract  String getJedisPoolName( );//保存到那个内存数据库
	public     boolean getIsMapsplit( ){//是否对MAP分拆
		return false;
	}
	/**
	 * map 的分拆符 objectKey + #m# + id
	 * @return
	 */
	public String getMapKeySplit(){
		return "#m#";
	}
	/**
	 * set 的分拆符 objectKey + #s# + id
	 * @return
	 */
	public String getSetKeySplit(){
		return "#s#";
	}

	/**
	 * set 的分拆符 objectKey + #s# + id
	 * @return
	 */
	public String getListKeySplit(){
		return "#l#";
	}


	public String getSkey(String key ,String setId){
		return key + getSetKeySplit() + setId;
	}

	/**
	 * 退出时必须释放回连接池
	 */
    public void cleanup() {
//    	JedisPoolManage.getInstance().returnResource(this.getJedisPoolName() ,jedis);
		synchronized (this){
			if(this.jedisThreadLocal.get()!=null){
				try{
					this.jedisThreadLocal.get().close();
				}catch (Exception e){
					System.out.println("cleanup error--"+e.getMessage());
				}
				//reset the thread jedis
				this.jedisThreadLocal.set(null);
			}
		}
		this.getJedis();
//    	JedisPoolManage.getInstance().destroy(this.getJedisPoolName());
    }
}
