package cn.richcloud.engine.realtime.loader;

import java.util.Map;

public interface ImportLifeCycle<T> {

	 void importBefore();

	 void imports(Map<String,Object> h);

	 void importCompete();
	
	/**
	 * 当线程退出时进行释放
	 */
	 void destroyAll();
}
