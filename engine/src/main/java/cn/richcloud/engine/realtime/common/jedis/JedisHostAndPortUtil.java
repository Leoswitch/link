package cn.richcloud.engine.realtime.common.jedis;

import java.util.HashMap;
import java.util.Map;

import cn.richcloud.engine.realtime.common.utils.RealTimeConfig;

/**
 * jedis内存数据库的连接
 * @author Administrator
 *
 */
public class JedisHostAndPortUtil {
    private static Map<String,JHostAndPort> hostAndPortList = new HashMap<String,JedisHostAndPortUtil.JHostAndPort>(
            1);

    static {
         JHostAndPort defaulthnp1 = new JHostAndPort();
        defaulthnp1.host = RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_MASTER)[0];
        defaulthnp1.port = Integer.valueOf( RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_MASTER)[1]) ;//Protocol.DEFAULT_PORT;
        defaulthnp1.password = RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_MASTER)[2];
        defaulthnp1.timeout = Integer.valueOf(RealTimeConfig.getInstance().getTimeOut(RealTimeConfig.REDIS_MASTER));
        hostAndPortList.put(RealTimeConfig.REDIS_MASTER, defaulthnp1);
        
        defaulthnp1 = new JHostAndPort();
        defaulthnp1.host = RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_SLAVE1)[0];
        defaulthnp1.port = Integer.valueOf( RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_SLAVE1)[1]) ;//Protocol.DEFAULT_PORT;
        defaulthnp1.password = RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_SLAVE1)[2];
        defaulthnp1.timeout = Integer.valueOf(RealTimeConfig.getInstance().getTimeOut(RealTimeConfig.REDIS_SLAVE1));
        hostAndPortList.put(RealTimeConfig.REDIS_SLAVE1, defaulthnp1);

        defaulthnp1 = new JHostAndPort();
        defaulthnp1.host = RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_SYC)[0];
        defaulthnp1.port = Integer.valueOf( RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_SYC)[1]) ;//Protocol.DEFAULT_PORT;
        defaulthnp1.password = RealTimeConfig.getInstance().getHost(RealTimeConfig.REDIS_SYC)[2];
        defaulthnp1.timeout = Integer.valueOf(RealTimeConfig.getInstance().getTimeOut(RealTimeConfig.REDIS_SYC));
        hostAndPortList.put(RealTimeConfig.REDIS_SYC, defaulthnp1);
    }

    public static  JHostAndPort  getRedisServers(String redisName) {
        return hostAndPortList.get(redisName);
    }

    public static class JHostAndPort {
        public String host;
        public int port;
        public String password;
        public int timeout;
    }
}
