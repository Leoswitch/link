package cn.richcloud.engine.realtime.busi;

import cn.richcloud.engine.realtime.common.jedis.JedisHostAndPortUtil;
import cn.richcloud.engine.realtime.common.utils.RealTimeConfig;
import redis.clients.jedis.Jedis;

/**
 * Created by root on 10/9/16.
 */
public class SycKeyService {
    static JedisHostAndPortUtil.JHostAndPort master = JedisHostAndPortUtil.getRedisServers(RealTimeConfig.REDIS_SYC) ;

    public static void syc(String sycKey){
        Jedis jedis = null;
        try{
            jedis =new Jedis(master.host,master.port);
            jedis.auth(master.password);
            jedis.incr(sycKey);
            System.out.println("----============"+sycKey+"incr");
        }catch (Exception e){
            System.out.println("!!!syc"+sycKey+"fail----");
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }

    }
}
