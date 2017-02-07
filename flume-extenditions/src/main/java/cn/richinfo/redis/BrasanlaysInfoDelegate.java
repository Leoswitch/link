package cn.richinfo.redis;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.richcloud.engine.realtime.busi.BrasanlaysInfoSpec;

/**
 * 实时营销目标用户提取表
 * @author Administrator
 *
 */
public class BrasanlaysInfoDelegate extends InstDelegate{
	   private static final Logger LOGGER = LoggerFactory.getLogger(BrasanlaysInfoDelegate.class);
		private static Map<String,Map<String, String>> userRuleMap  ;
		private long updateTime = 0;
	private static BrasanlaysInfoDelegate delegate = new BrasanlaysInfoDelegate();

	
	public BrasanlaysInfoDelegate(){
		autoReload();
	}
	public static BrasanlaysInfoDelegate getInstance(){
		
		return delegate;
	}
	
	public Map<String,Map<String, String>> getUserRuleMap(){
		return userRuleMap;
	}
	
	@Override
	protected void loadData(){
		
		if( (System.currentTimeMillis() - updateTime) <  60*1000){
			//60秒内不加载
			return ;
		}
		long oldtime = updateTime;
		updateTime = System.currentTimeMillis();
		synchronized (BrasanlaysInfoDelegate.class ) {

			BrasanlaysInfoSpec spec = new BrasanlaysInfoSpec();
			int num = 0;
			boolean reload= true;
			while(spec.isLoading()) {
				num ++ ;
				if(num > 1000) {
					reload = false;
					break ;
				}
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					reload = false;
					break;
				}
			}
			if(reload){
				Map<String,Map<String, String>> map = spec.hgetAllObject();
				userRuleMap = map;
			} else {
				updateTime = oldtime;
				LOGGER.warn("重新加载数据失败", new RuntimeException("重新加载数据失败"));
			}
			spec.cleanup();
		}
	}
	
	@Override
	public long getLoadInterval() {
		return 60*60*1000;
	}

}
