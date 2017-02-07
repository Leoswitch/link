package cn.richcloud.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.richcloud.common.exception.DBAccessException;
import cn.richcloud.common.datasource.DataSourceX;

public class DAOUtils {

	// 系统默认日期格式
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String DATE_FORMAT_ = "yyyyMMdd";
	// 系统默认日期时间格式
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_TIME_FORMAT_ALL = "yyyyMMddHHmmss";
	

	/**
	 * @param :result 需要关闭的结果集
	 */
	public static void free(ResultSet result) throws DBAccessException {
		try {
			if (result != null) {
				result.close();
			}
		} catch (SQLException se) {
			throw new DBAccessException("SQL Exception while closing "
					+ "Result Set : \n" + se);
		}
	}

	/**
	 * 直接取得连接
	 * 
	 * @param dsName
	 * @return
	 */
	public static Connection getDirectCon(String dsName) {
		Connection conn = DataSourceX.getConnection(dsName);
		return conn;
	}

	/**
	 * 直接关闭连接
	 * 
	 * @param connection
	 * @throws DBAccessException
	 */
	public static void closeDirectCon(Connection connection)
			throws DBAccessException {
		try {
			if (connection != null) {
				if (connection.getAutoCommit() == false) {
					connection.commit();
				}
				connection.close();
			}
		} catch (SQLException se) {
			throw new DBAccessException("SQL Exception while closing "
					+ "Result Set : \n" + se);
		}
	}
	
	/**
	 * 回滚
	 * @param connection
	 * @throws DBAccessException
	 */
	public static void rollbackCon(Connection connection)
			throws DBAccessException {
		try {
			if (connection != null) {
				if (connection.getAutoCommit() == false) {
					connection.rollback();
				}
			}
		} catch (SQLException se) {
			throw new DBAccessException("SQL Exception while rollback "
					+ "Result Set : \n" + se);
		}
	}

	public static void free(Connection connection) {
		if (connection != null) {
			SystemContext.getContext().freeConnection(connection);
		}
	}

	/**
	 * 
	 * @param stmt
	 * @throws DAOSystemException
	 */
	public static void free(Statement stmt) throws DBAccessException {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException se) {
			throw new DBAccessException("SQL Exception while closing "
					+ "Statement : \n" + se);
		}
	}

	/**
	 * 将Date转换成统一的日期格式文本。
	 * 
	 * @return
	 */
	public static String getFormatedDate(Date date) {
		if (null == date)
			return "";

		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_FORMAT);
		return dateFormator.format(new java.sql.Date(date.getTime()));
	}

	/**
	 * 将Date转换成统一的日期时间格式文本。
	 * 
	 * @return
	 */
	public static String getFormatedDateTime(Date date) {
		if (null == date)
			return "";

		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT);
		return dateFormator.format(new java.sql.Date(date.getTime()));
	}

	public static Map<String, String> row2Map(ResultSet rs) throws SQLException {
		Map<String, String> result = new HashMap<String, String>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		for (int i = 1; i <= cols; i++) {
			result.put(rsmd.getColumnName(i).toLowerCase(), rs.getString(i));
		}
		return result;
	}

	public static java.sql.Date formateDate(String time) {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_FORMAT);
		try {
			Date date = dateFormator.parse(time);
			return new java.sql.Date(date.getTime());
		} catch (ParseException e) {
			return null;
		}
	}

	public static java.sql.Timestamp formateDateTime(String time) {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT);
		try {
			Date date = dateFormator.parse(time);
			return new java.sql.Timestamp(date.getTime());
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static String getFormateDateTime(String time) {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT);
		try {
			Date date = dateFormator.parse(time);
			SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT_ALL);
			return df.format(date);
		} catch (ParseException e) {
			return time;
		}
	}
	
	public static String getFormateDateTime3(String time) {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT_ALL);
		try {
			Date date = dateFormator.parse(time);
			SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
			return df.format(date);
		} catch (ParseException e) {
			return time;
		}
	}
	/**
	 * 添加日期格式转换方法（yyyymmdd  转成 yyyy-mm-dd）
	 * @param time
	 * @return
	 */
	public static String getFormateDateTime4(String time) {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_FORMAT_);
		try {
			Date date = dateFormator.parse(time);
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
			return df.format(date);
		} catch (ParseException e) {
			return time;
		}
	}
	/**
	 * 计算时间差(单位秒)
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static String getTimeDiff(String startTime ,String endTime){
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT);
		long diff = 0 ;
		Date d1;
		Date d2;
		try {
			d1 = dateFormator.parse(endTime);
			d2 = dateFormator.parse(startTime);
			diff = d1.getTime() - d2.getTime();
		} catch (ParseException e) {
			return "0";
		}
		return diff/1000 +"";//毫秒转秒
	}
	
	public static String getFormateDateTime2(String time) {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT);
		try {
			Date date = dateFormator.parse(time);
			SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
			return df.format(date);
		} catch (ParseException e) {
			return time;
		}
	}

	public static String getCurrentDateTime() {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT);
		return dateFormator.format(new Date());
	}
	public static String getCurDateTime() {
		SimpleDateFormat dateFormator = new SimpleDateFormat(DATE_TIME_FORMAT_ALL);
		return dateFormator.format(new Date());
	}
	
	public static void  main(String [] str) {
		System.out.println(DAOUtils.getFormateDateTime3("20130525174144"));
	}

}
