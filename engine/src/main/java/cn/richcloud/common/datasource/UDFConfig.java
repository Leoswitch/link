package cn.richcloud.common.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class UDFConfig {
 
	private static UDFConfig instance= new UDFConfig();
	
	private final String configFile = "/udf.properties";
	
	public static final String JAR_PATH = "JAR_PATH";

	
	private Map<String,String > parameterMap = new ConcurrentHashMap<String,String >();
	
	private UDFConfig(){
		loadConfigFile( );
	}
	
	public static UDFConfig getInstance(){
		return instance;
	}
	
	public String getParameter(String key) {
		String  parameter  = parameterMap.get(key);
		return parameter ;
	}

	public Map<String, String> getParameterMap() {
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
}
