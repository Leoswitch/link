package cn.richcloud.common.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH管理信息
 * @author Administrator
 *
 */
public class SSHConfig {
 
	private static SSHConfig instance= new SSHConfig();
	
	private final   String configFile = "/ssh.properties";
	
	public static final String HADOOP_MASTER = "HADOOP_MASTER";
	
	public static final String HADOOP_JAR_CMD = "HADOOP_JAR_CMD";
	
	public static final String HADOOP_LOCATION_BIN = "HADOOP_LOCATION_BIN";
	
	
	private Map<String,String > parameterMap = new ConcurrentHashMap<String,String >();
	
	private SSHConfig(){
		loadConfigFile( );
	}
	
	public static SSHConfig getInstance(){
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
		params[1] =  getParameter(  name + "_NAME") ;
		params[2] =  getParameter(  name + "_PASSWORD") ;
		return params;
	}
	
}
