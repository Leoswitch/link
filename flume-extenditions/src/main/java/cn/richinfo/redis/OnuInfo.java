package cn.richinfo.redis;

import cn.richinfo.common.HbaseHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by root on 10/19/16.
 */
public class OnuInfo {

    private HbaseHelper hbaseHelper = new HbaseHelper();
    private String tableName = "onuinfo";

//    ArrayList<String> fieldNames = Lists.newArrayList("areaid", "useraccount", "portname", "provicename", "districtname", "buildingname", "unitname", "onuname");
//    ArrayList<String> fieldNames = Lists.newArrayList("c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8");
                                          //buildingname   unitname districtname areaid provicename portname onuname
    ArrayList<String> fieldNames = Lists.newArrayList("c6", "c7", "c5", "c1", "c4", "c3", "c8");

    public Map<String,String> getData(String useracount){
        Map<String, String> data = hbaseHelper.getData(tableName, useracount);
        if(data==null||data.isEmpty()||data.size()!=8){
            return null;
        }else{
            Map<String, String> ret = new LinkedHashMap<String, String>();
            for(String key:fieldNames){
                if(key.equals("c2")){
                    ret.put("c2",data.get("key").trim());
                }else{
                    ret.put(key,data.get(key).trim());
                }
            }
            return ret;
        }

    };

    public static void main(String[] args) {
        OnuInfo onuInfo = new OnuInfo();
        Map<String, String> data = onuInfo.getData("13508530270");
        System.out.println(Joiner.on(",").join(data.values()));
    }
}
