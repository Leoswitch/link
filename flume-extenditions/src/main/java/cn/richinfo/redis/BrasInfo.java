package cn.richinfo.redis;

import cn.richinfo.common.HbaseHelper;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by root on 10/19/16.
 */
public class BrasInfo {

    private HbaseHelper hbaseHelper = new HbaseHelper();
    private String tableName = "brasinfo";

    ArrayList<String> fieldNames = Lists.newArrayList("c4", "c1", "c2", "c3");

    public Map<String,String> getData(String ip){
        Map<String, String> data = hbaseHelper.getData(tableName, ip);

        if(data==null||data.isEmpty()||data.size()!=5){
            return null;
        }else{
            Map<String, String> ret = new TreeMap<String, String>();
            for(String key:fieldNames){
                if(key.equals("c3")){
                    ret.put("c3",data.get("key"));
                }else{
                    ret.put(key,data.get(key));
                }
            }
            return ret;
        }
    }

}
