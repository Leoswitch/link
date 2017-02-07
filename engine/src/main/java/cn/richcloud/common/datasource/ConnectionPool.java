package cn.richcloud.common.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.richcloud.common.Debug;

/**
 * 连接池
 * 
 */
public class ConnectionPool {
	private final List<DefineConnection> connectionPool = Collections
			.synchronizedList(new ArrayList<DefineConnection>());
	private final ConcurrentMap<Integer, DefineConnection> connectionPoolIndex = new ConcurrentHashMap<Integer, DefineConnection>();
	private int poolMax = 10;// 最大连接数
	private String dsName;//数据源名称
	private int initial = 1;// 初始连接数
	
	private final static int defaultThreadTime = 60;
	private final static int defaultTimeOut = 600;
	private int poolThreadTime;//连接池线程轮训间隔(秒)
	private int idleConnectTimeOut;//被占用连接多久超时(秒)
	private PoolHandlerThread poolHandler ;
	
	private ConnectionPool() {
	}

	public ConnectionPool(String dsName) {
		this(dsName,defaultTimeOut,defaultThreadTime);
	}
	
	public ConnectionPool(String dsName,int idleConnectTimeOut,int poolThreadTime) {
		this.dsName = dsName;
		this.idleConnectTimeOut = idleConnectTimeOut;
		this.poolThreadTime = poolThreadTime;
		initialPool();
		poolHandler = new PoolHandlerThread();
		poolHandler.start();
	}

	private void initialPool() {
		for (int i = 0; i < initial; i++) {
			Connection connection = DataSourceX.getConnection(this.dsName);
			
			this.put(connection);
		}

	}

	/**
	 * 判断是否满了
	 * 
	 * @return
	 */
	private boolean isFull() {
		if (connectionPool.size() >= poolMax) {
			return true;
		}
		return false;
	}

	/**
	 * 放入一个连接
	 * 
	 * @param connection
	 */
	private void put(Connection connection) {
		if (connection == null || isFull()) {
			return;
		}
		DefineConnection defineConnection = new DefineConnection(connection);
		defineConnection.setIdle();
		connectionPool.add(defineConnection);
		connectionPoolIndex.put(connection.hashCode(), defineConnection);
		Debug.print("新的连接" + connection.hashCode() + "被放入连接池" + dsName
				+ ",当前连接数:" + connectionPool.size());
	}

	/**
	 * 使用完后归还池
	 * 
	 * @param connection
	 */
	public void replace(Connection connection) {
		DefineConnection defineConnection = connectionPoolIndex.get(connection
				.hashCode());
		if (defineConnection != null) {
			defineConnection.setIdle();
//			Debug.print("一个使用完毕的连接被归还到连接池" + dsName + ":"
//					+ connection.hashCode() + ",当前连接数:" + connectionPool.size());
		}
	}

	/**
	 * 获得一个连接，无需等待，如果都繁忙，连接池没有满就创建一个新的连接，否则返回空。
	 * 
	 * @return
	 */
	public synchronized Connection getConnection() {
		for (DefineConnection connection : connectionPool) {
			if (!connection.isBusy()) {
				connection.setBusy();
				Connection conn = connection.getConnection();
//				Debug.print("一个空闲的连接被获取:" + conn.hashCode());
				return conn;
			}
		}
		if (!this.isFull()) {
			Connection connection = DataSourceX.getConnection(this.dsName);
			this.put(connection);
			return connection;
		}
		return null;
	}

	/**
	 * 清理池
	 */
	public void clear() {
		Iterator<DefineConnection> it = connectionPool.iterator();
		while (it.hasNext()) {
			try {
				DefineConnection connection= it.next();
				connection.getConnection().commit();
				connection.getConnection().close();
				Debug.print("## 一个被关闭的Connection从连接池中清除!"
						+ connection.getConnection().hashCode());
				connectionPoolIndex.remove(connection
						.getConnection().hashCode());
				it.remove();
			} catch (SQLException e) {
				Debug.print(e);
			}
		}
		poolHandler.setRunning(false);
	}
	
	public void rollback() {
		Iterator<DefineConnection> it = connectionPool.iterator();
		while (it.hasNext()) {
			try {
				DefineConnection connection= it.next();
				connection.getConnection().rollback();
				Debug.print("## 一个连接被回滚!"
						+ connection.getConnection().hashCode());
			} catch (SQLException e) {
				Debug.print(e);
			}
		}
		poolHandler.setRunning(false);
	}

	
	
	/**
	 * @return 连接池监控轮询时间
	 */
	public int getPoolThreadTime() {
		return poolThreadTime;
	}

	/**
	 * @param poolThreadTime 连接池监控轮询时间
	 */
	public void setPoolThreadTime(int poolThreadTime) {
		this.poolThreadTime = poolThreadTime;
	}

	/**
	 * @return 链接超时回收时间（秒）
	 */
	public int getBusyConnectTimeOut() {
		return idleConnectTimeOut;
	}

	/**
	 * @param busyConnectTimeOut 链接超时回收时间（秒）
	 */
	public void setBusyConnectTimeOut(int busyConnectTimeOut) {
		this.idleConnectTimeOut = busyConnectTimeOut;
	}



	/**
	 * 连接池状态
	 * 
	 */
	public enum CONNECTION_STATUS {
		BUSY, IDLE;
	}

	/**
	 * 自定义连接
	 * 
	 */
	private class DefineConnection {
		private Connection connection;
		private CONNECTION_STATUS status = CONNECTION_STATUS.IDLE;
		private long idleLong = 0L;

		public DefineConnection(Connection connection) {
			this.connection = connection;
		}

		/**
		 * 设置为繁忙
		 */
		public synchronized void setBusy() {
			this.status = CONNECTION_STATUS.BUSY;
		}

		/**
		 * 设置为空闲
		 */
		public synchronized void setIdle() {
			this.status = CONNECTION_STATUS.IDLE;
			this.idleLong = System.currentTimeMillis();
		}

		/**
		 * 判断是否繁忙
		 * 
		 * @return
		 */
		public synchronized boolean isBusy() {
			if (this.status == CONNECTION_STATUS.BUSY) {
				return true;
			}
			return false;
		}

		/**
		 * 获得连接空闲时间
		 * 
		 * @return
		 */
		public long getIdleLong() {
			return this.idleLong;
		}

		/**
		 * 获得连接
		 * 
		 * @return
		 */
		public synchronized Connection getConnection() {
			return this.connection;
		}


	}

	/**
	 * 数据源服务线程
	 */
	private class PoolHandlerThread extends Thread {
		private boolean running = true;
		
		public PoolHandlerThread() {
			Debug.print(this.hashCode() + "数据库连接池连接释放线程监控已启动!");
		}

		public void setRunning(boolean running) {
			this.running = running;
			
		}

		public void run() {
			clearPool();
		}

		/**
		 * 清理连接池 一定时间之内没有人使用连接池，将会把所有连接关闭，并清空连接池
		 */
		private void clearPool() {
			while (running) {
				try {
					sleep(poolThreadTime * 1000);// 指定轮询间隔清理使用完毕的Connection
					if (connectionPool == null || connectionPool.isEmpty()) {
						continue;
					}
					long currentTime = System.currentTimeMillis();
					Iterator<DefineConnection> it = connectionPool.iterator();
					while (it.hasNext()) {
						DefineConnection connection= it.next();
						if (!connection.isBusy()) {
							int bw = (int) ((currentTime - connection.
									getIdleLong()) / 1000);// 空闲的时长
							if(bw>=idleConnectTimeOut){
								connection.getConnection().close();
								connectionPoolIndex.remove(connection
										.getConnection().hashCode());
								it.remove();
								Debug.print("##连接池监控"
										+ this.hashCode()
										+ "## 一个长时间未被使用的Connection从连接池中清除!已经闲置了" + bw + "秒。"
										+ connection.getConnection().hashCode());
							}
						}
					}
				} catch (Exception ex) {
					Debug.print(ex);
				}
			}
		}

	}

}