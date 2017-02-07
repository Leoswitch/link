package cn.richcloud.common.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DataSourceX {
	
	public static Connection getConnection(String source){
		Connection conn = null;
		String[] params = Config.getInstance().getParameters(source);
		try {
			Class.forName(params[0]);
			conn = DriverManager.getConnection(params[1], params[2], params[3]);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	public  static Connection getHiveConn(){
		return getConnection(Config.HIVE);
	}
	
	public  static Connection getEdaConn(){
		return getConnection(Config.EDA);
	}
	public  static Connection getEtlConn(){
		return getConnection(Config.ETL);
	}
	
	public  static Connection getGPConn(){
		return getConnection(Config.GP);
	}
	
	public  static Connection getSybaseConn(){
		return getConnection(Config.SYBASE);
	}
	
	 
}
