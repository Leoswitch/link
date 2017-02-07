package cn.richcloud.engine.realtime.loader;

import cn.richcloud.engine.realtime.common.jedis.JedisLoadSpec;


public abstract class JedisImportLifeCycle<T extends JedisLoadSpec> implements ImportLifeCycle<T>{

	private T jedisSpec = null;
	public JedisImportLifeCycle(  ){

	}

	public JedisImportLifeCycle(T jedisSpec){
		this.jedisSpec = jedisSpec;
	}

	public T getJedisSpec() {
		return jedisSpec;
	}

	public void setJedisSpec(T jedisSpec) {
		this.jedisSpec = jedisSpec;
	}

	public void destroyAll(){
		if(this.getJedisSpec()!=null){
			this.getJedisSpec().cleanup();
			this.setJedisSpec(null);
		}
	}

}
