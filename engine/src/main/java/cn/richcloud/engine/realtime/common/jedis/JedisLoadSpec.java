package cn.richcloud.engine.realtime.common.jedis;

public abstract class JedisLoadSpec extends JedisSpec implements IJedisLoad,IJedisSyc{
	protected String key = "init";
	protected  String syckey;
	public JedisLoadSpec(){
		super();
	}
	/**
	 * 正在加载
	 */
	public void setLoading(){
		this.getJedis().set(this.getObjectKey()+ getLoadKeySplit(), getLoadFlag());
	}
	
	/**
	 * 加载完成
	 */
	public void setUnLoading(){
		this.getJedis().del(this.getObjectKey()+ getLoadKeySplit());
	}
	
	/**
	 * 是否加载
	 */
	public boolean isLoading(){
		String r = this.getJedis().get(this.getObjectKey()+ getLoadKeySplit());
		return getLoadFlag().equals(r);
	}
	
	private String getLoadKeySplit(){
		return "#load#";
	}
	
	private String getLoadFlag(){
		return "1";
	}

	public void setKey(String key){
		this.key = key;
	}

	public abstract String getZnode();

	public String getKey(){return key;}

}
