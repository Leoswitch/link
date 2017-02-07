package cn.richcloud.engine.realtime.busi;

import cn.richcloud.engine.realtime.common.jedis.JedisLoadSpec;
import cn.richcloud.engine.realtime.common.utils.RealTimeConfig;


public class OnuDelinfoSpec extends JedisLoadSpec {



	public OnuDelinfoSpec(){
		super();
	}

	@Override
	public String getObjectName() {
		return key;
	}

	@Override
	public String getJedisPoolName() {
		return RealTimeConfig.REDIS_SLAVE1;
	}
	
	@Override
	public String toSplitMap(String key , String id){
		try{
			if(id.length()<4){
				return key + getMapKeySplit()+ id;
			}else{
				return key + getMapKeySplit()+ Long.valueOf(id.substring(id.length() -4) )%10000;
			}
		}catch (Exception e){
			return key + getMapKeySplit()+ id;
		}
	}
	
	@Override
	public boolean getIsMapsplit(){//是否对MAP分拆
		return true;
	}


	@Override
	public String getObjectKey() {
		return key;
	}


	@Override
	public String getZnode() {
		return "/onu";
	}

	public String toString(){
		return getKey();
	}

	public String getSycKey() {
		return "onusyc";
	}

	public static void main(String[] args) {
		OnuDelinfoSpec onuDelinfoSpec = new OnuDelinfoSpec();
		onuDelinfoSpec.cleanup();
		onuDelinfoSpec = new OnuDelinfoSpec();
		System.out.println(onuDelinfoSpec.getJedis());
	}
}
