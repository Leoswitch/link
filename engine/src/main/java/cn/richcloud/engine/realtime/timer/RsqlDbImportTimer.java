package cn.richcloud.engine.realtime.timer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import cn.richcloud.common.DAOUtils;
import cn.richcloud.common.datasource.Config;
import cn.richcloud.common.exception.DBAccessException;
import cn.richcloud.engine.realtime.loader.ImportLifeCycle;
import org.apache.commons.lang.StringUtils;

import cn.richcloud.common.datasource.DataSourceX;

/**
 *
 * @author leo
 *  1：实例化对象，设置关系型数据获取数据sql语句与数据源
 *  2：构造JedisImportLifeCycle对象
 * @param <T>
 */
public class RsqlDbImportTimer<T> extends IntervalTimer<T>{

	private String sql = "";
	private String dsName = Config.EPS;
	public RsqlDbImportTimer(){}
/**
 *
 * @param firstTimer
 * @param period 以毫秒为单位
 */
	public RsqlDbImportTimer(Date firstTimer,long period){
		super(firstTimer, period);
	}

	public RsqlDbImportTimer(Timer myTimer,Date firstTimer,long period){
		super(myTimer, firstTimer, period);
	}

	@Override
	protected void loadData(ImportLifeCycle<T> dbOptor) {
		System.out.println("loadData=======================");
		if(StringUtils.isEmpty(sql)){
			throw new IllegalArgumentException("the sql is empty~");
		}

		PreparedStatement stmt = null;
		ResultSet result = null;
		Connection dbConnection = null;
		try {
			dbConnection = DataSourceX.getConnection(dsName);
			stmt = dbConnection.prepareStatement(sql);
			result = stmt.executeQuery();
			ResultSetMetaData rsm = result.getMetaData();
			dbOptor.importBefore();
			while (result.next()) {
				Map<String, Object> h = new HashMap<String, Object>();

				for (int j = 1; j <= rsm.getColumnCount(); j++) {
					String nam = rsm.getColumnName(j).toLowerCase();// 转换为小写，与代码生成器字段小写相匹配
					String val = "";
					String columnType = rsm.getColumnTypeName(j);
					if ("DATE".equals(columnType)) {
						val = result.getString(j);
						if (val != null && !"".equals(val)) {
							if (val.indexOf("00:00:00.0") != -1) // 作为Date类型处理，否则为Timestamp
								val = DAOUtils.getFormatedDate(result.getDate(j));
							else
								val = DAOUtils.getFormatedDateTime(result.getTimestamp(j));
						}
					} else if ("TIMESTAMP".equals(columnType)) {
						val = DAOUtils.getFormatedDateTime(result.getTimestamp(j));
					} else{
						val = result.getString(j);
					}
					h.put(nam, val);
				}
				dbOptor.imports(h);
			}
		} catch (Exception se) {
			se.printStackTrace();
			throw new DBAccessException("SQLException while execSQL:" + sql + "\n", se);
		} finally {
			DAOUtils.free(result);
			DAOUtils.free(stmt);
			DAOUtils.closeDirectCon(dbConnection);
			dbOptor.importCompete();
		}
	}



	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}



	public void setDsName(String dsName) {
		this.dsName = dsName;
	}
}
