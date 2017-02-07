package cn.richcloud.common.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import cn.richcloud.common.exception.ConfigNotFoundException;


public class Config {
	public final static String ETL = "ETL";
	public final static String IUBA_SYS = "IUBA_SYS";
	public final static String IUBA_DATA = "IUBA_DATA";
	public final static String HIVE = "HIVE";
	public final static String EDA = "EDA";
	public final static String GP = "GP";
	public final static String SYBASE = "SYBASE";
	public final static String EPS = "EPS";
	 
	
	private static Config instance= new Config();
	private final String configFile = "/datasource.properties";
	private final String DRIVER = "DRIVER";
	private final String URL = "URL";
	private final String USERNAME = "USERNAME";
	private final String PASSWORD = "PASSWORD";
	
	private Map<String,String[]> parameterMap = new ConcurrentHashMap<String,String[]>();
	
	private Config(){
	}
	
	public static Config getInstance(){
		return instance;
	}
	
	public String[] getParameters(String sourceName) {
		String[] parameters = parameterMap.get(sourceName);
		if(parameters == null){
			parameters = loadConfigFile(sourceName);
		}
		return parameters;
	}

	private String[] loadConfigFile(String sourceName) {
		String[] parameters = new String[4];
		Properties prop = new Properties();
		InputStream in = this.getClass().getResourceAsStream(configFile);
		try {
			prop.load(in);
			parameters[0] = prop.getProperty(sourceName+"_"+DRIVER);
			parameters[1] = prop.getProperty(sourceName+"_"+URL);
			parameters[2] = prop.getProperty(sourceName+"_"+USERNAME);
			parameters[3] = prop.getProperty(sourceName+"_"+PASSWORD);
			if(parameters[0]==null||"".equals(parameters[0])
					||parameters[1]==null||"".equals(parameters[1])
					||parameters[2]==null||parameters[3]==null){
				throw new ConfigNotFoundException("数据源 "+sourceName +"的配置有误，请检查"+configFile+"中的配置！");
			}
			parameterMap.put(sourceName, parameters);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return parameters;
	}
}
