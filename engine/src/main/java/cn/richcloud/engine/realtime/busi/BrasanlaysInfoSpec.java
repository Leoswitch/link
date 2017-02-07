package cn.richcloud.engine.realtime.busi;

import cn.richcloud.engine.realtime.common.jedis.JedisLoadSpec;
import cn.richcloud.engine.realtime.common.utils.RealTimeConfig;


/**
 * MDN对象
 * @author Administrator
 *
 */
public class BrasanlaysInfoSpec extends JedisLoadSpec {


	 public BrasanlaysInfoSpec(){
		 super();
	 }

	@Override
	public String getObjectName() {
		return key;//"brasanlaysinfo";
	}

	@Override
	public String getJedisPoolName() {
		return RealTimeConfig.REDIS_MASTER;
	}
	@Override
	protected String toSplitMap(String key , String id){
		return key;
	}


	@Override
	public String getObjectKey() {
		return key;//"brasanlaysinfo";
	}




	public String toString(){
		return this.key;
	}

	@Override
	public String getZnode() {
		return "/bras";
	}

	public String getSycKey() {
		return "brasyc";
	}

}
