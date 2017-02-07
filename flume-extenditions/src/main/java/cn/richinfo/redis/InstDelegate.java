package cn.richinfo.redis;

import cn.richcloud.engine.realtime.common.jedis.IJedisLoad;

import java.util.Random;


public abstract class InstDelegate {
	private static long whileUpdateTime = 0L;
	private long autoLoadTime = 0;
	protected abstract void loadData();
	public abstract long getLoadInterval();
	public void autoReload(){
		if( (System.currentTimeMillis() - autoLoadTime) <  getLoadInterval()){
			//60秒内不加载
			return ;
		}
		try {
			Thread.currentThread().sleep(Math.abs(new Random().nextInt(100)));
		} catch (InterruptedException e) {
		}
		synchronized (this) {
			if( (System.currentTimeMillis() - autoLoadTime) <  getLoadInterval()){
				//60秒内不加载
				return ;
			}
		}
		autoLoadTime = System.currentTimeMillis() ;
		this.loadData();
	}
	
	protected void loadingBlock(IJedisLoad spec, int maxTimes){
		int currentTimes = 0;
    	while(spec.isLoading()){
    		
    		if(System.currentTimeMillis() - whileUpdateTime <= 1000*60*30){
    			break;
    		}
    		
    		if(currentTimes++>=maxTimes){
    			whileUpdateTime = System.currentTimeMillis();
    			break;
    		}
    		
    		try {
				Thread.currentThread().sleep(1000L);
			} catch (InterruptedException e) {
			}
    	}
	}

	public  class AutoLoader implements Runnable{

		public void run() {
			boolean loop = true;
			while(loop){

				if( (System.currentTimeMillis() - autoLoadTime) <  getLoadInterval()){
					try {
						Thread.currentThread().sleep(Math.abs(new Random().nextInt(100)));
					} catch (InterruptedException e) {

					}

				}else{
					loadData();
				}

				autoLoadTime = System.currentTimeMillis();


			}


		}
	}
	
}
