package cn.richcloud.engine.realtime.timer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.richcloud.engine.realtime.loader.ImportLifeCycle;

public abstract class IntervalTimer<T>    {

	protected Timer defaultTimer = null;
	private ImportLifeCycle<T> myJedisDbOptor = null;

	private Date firstTimer = new Date();
	private long period = 10000;//以毫秒为单位
	public IntervalTimer(){
 
	}

	public IntervalTimer(Date firstTimer,long period){
		this(null, firstTimer, period);
	}

	public IntervalTimer(Timer myTimer,Date firstTimer,long period){
		this.defaultTimer = myTimer;
		this.firstTimer = firstTimer;
		this.period = period;
	}


	/**
	 *  加载数据
	 */
	protected abstract void loadData(ImportLifeCycle<T> myJedisDbOptor);
	/**
	 * 起动定时器
	 */
	public   void startTimer(){
		if(defaultTimer == null){
			defaultTimer = new Timer();
		}

		defaultTimer.schedule(new TimerTask(){
			@Override
			public void run() {
			    loadData(myJedisDbOptor);
			}
		}, firstTimer, period);
	}
	
	/**
	 * 单次加载
	 */
	public   void singleLoad(){
		    loadData(myJedisDbOptor);
	}

	public void setDbOptor(final ImportLifeCycle<T> myJedisDbOptor){
		this.myJedisDbOptor= myJedisDbOptor;
	
	}

	public void destroyTimer(){
		if(defaultTimer != null){
			defaultTimer.cancel();
			defaultTimer = null;
		}
	}

	public Timer getDefaultTimer(){
		return defaultTimer;
	}

 
}
