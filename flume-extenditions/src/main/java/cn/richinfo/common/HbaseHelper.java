/**
 *
 */
package cn.richinfo.common;

import com.google.common.base.Joiner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

public class HbaseHelper {

    public static final String COL_CF = "cf";

    private HTable hTable;

    public static Configuration conf = HBaseConfiguration.create();

    public HbaseHelper(){
    }





    public Map<String,String> getData(String tableName,String row,String... colName){
        Map<String,String> ret = new HashMap<String, String>();
        if(hTable==null){
            try {
                hTable = new HTable(conf, tableName);
            } catch (IOException e) {
                if(hTable!=null){
                    try {
                        hTable.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                hTable = null;
                e.printStackTrace();
            }
        }
            Get get = new Get(row.getBytes());
            Result result = null;
            try {
                result = hTable.get(get);
            } catch (IOException e) {
                if(hTable!=null){
                    try {
                        hTable.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                hTable = null;
                e.printStackTrace();
            }
            NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(COL_CF.getBytes());
            if(familyMap!=null&&!familyMap.isEmpty()){
                byte[] row1 = result.getRow();
                ret.put("key",new String(row1));
                for (byte[] bytes : familyMap.keySet()) {
                    ret.put(new String(bytes),new String(familyMap.get(bytes)));
                }
            }
       return ret;

    }
}

