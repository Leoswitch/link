package cn.richcloud.engine.realtime.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REDIS内存数据库管理信息
 * @author Administrator
 *
 */
public class RealTimeConfig {
 
	private static RealTimeConfig instance= new RealTimeConfig();
	
	private final   String configFile = "/realtime.properties";
	
	public static final String REDIS_MASTER = "REDIS_MASTER";
	public static final String REDIS_SLAVE1 = "REDIS_SLAVE1";
	public static final String REDIS_SYC = "REDIS_SYC";
	public static final String KESTREL_MASTER = "KESTREL_MASTER";
	public static final String KESTREL_SLAVE1= "KESTREL_SLAVE1";
	public static final String KESTREL_SLAVE2= "KESTREL_SLAVE2";
	public static final String KESTREL_SLAVE3= "KESTREL_SLAVE3";
	public static final String ACTION_MANI_SERVER = "ACTION_MANI_SERVER";
	
	
	private Map<String,String > parameterMap = new ConcurrentHashMap<String,String >();
	
	private RealTimeConfig(){
		loadConfigFile( );
	}
	
	public static RealTimeConfig getInstance(){
		return instance;
	}
	
	 public    String getParameter(String key) {
		String  parameter  = parameterMap.get(key);
		return parameter ;
	}

	public   Map<String, String> getParameterMap() {
		return parameterMap;
	}

	private void  loadConfigFile(   ) {
		Properties prop = new Properties();
		InputStream in = this.getClass().getResourceAsStream(configFile);
		try {
			prop.load(in);
		 
			for(Map.Entry entry : prop.entrySet()) {
				parameterMap.put((String)entry.getKey(), (String)entry.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public   String[] getHost(String name ) {
		String[]  params = new String[3];
		params[0] =  getParameter(  name + "_HOSTNAME") ;
		params[1] =  getParameter(  name + "_PORT") ;
		params[2] =  getParameter(  name + "_PASSWORD") ;
		return params;
	}

	public String getTimeOut(String name){
		String timeout = getParameter(name+"_TIMEOUT");
		if(timeout==null||timeout.equals("")){
			timeout= "20000";
		}
		return timeout;
	}
	
}
