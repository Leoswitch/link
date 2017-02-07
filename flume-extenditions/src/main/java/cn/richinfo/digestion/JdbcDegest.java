package cn.richinfo.digestion;

import com.google.common.base.Joiner;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;

import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 6/16/16.
 */
public class JdbcDegest {

    private RpcClient client;

    private String hostname;
    private int port;
    private String jdbcUrl;
    private String user;
    private String password;

    private String selectSql;

    private String increamentTestColume;
    private OracleManager manager;

    public void init(String hostname,int port,String selectSql,String increamentTestColume){
        this.hostname = hostname;
        this.port = port;
        this.client = RpcClientFactory.getDefaultInstance(hostname,port);
        manager = new OracleManager("jdbc:oracle:thin:@192.168.0.153:1521:eshore","test","test");
        this.selectSql = selectSql;
        this.increamentTestColume = increamentTestColume;
    }

    private void sendDataToFlume(String data){
        System.out.println("send   "+data);
        Event event = EventBuilder.withBody(data, Charset.forName("utf-8"));

        try{
            client.append(event);
        }catch (EventDeliveryException e){
            e.printStackTrace();
            client.close();
            client = null;
            client = RpcClientFactory.getDefaultInstance(hostname,port);
        }
    }



    public void digestData(){
        boolean loop = true;
        String preEndPoint = null;

        Map<String, Integer> columnTypesForRawQuery = manager.getColumnTypesForRawQuery(manager.appendCodition(selectSql," 1=0"));
        Integer checkColumnType = columnTypesForRawQuery.get(increamentTestColume);
        while(loop){
            try {
                String selectSqlWithCondition = selectSql;
                if(preEndPoint!=null){
                    String increamentCondition = increamentTestColume +">"+manager.datetimeToQueryString(preEndPoint,checkColumnType);
                    selectSqlWithCondition = manager.appendCodition(selectSql,increamentCondition);
                }
                selectSqlWithCondition = manager.appendOrderBy(selectSqlWithCondition,increamentTestColume);
                ResultSet resultSet= manager.execute(selectSqlWithCondition, 1000);
                while(resultSet.next()){

                    List<String> colVal =new ArrayList<String>();
                    Set<String> colNames = columnTypesForRawQuery.keySet();
                    for (String colN : colNames) {
                        String colV = resultSet.getString(colN);
                        colVal.add(colV);
                        if(increamentTestColume.equalsIgnoreCase(colN)){
                            preEndPoint = colV;
                        }
                    }

                    if(!colVal.isEmpty()){
                        sendDataToFlume(Joiner.on(",").join(colVal));
                    }
                }


                try {
                    System.out.println("sleep....."+preEndPoint);
                    Thread.currentThread().sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void cleanUp(){
        client.close();
        manager.release();
    }


    public static void main(String[] args) {
        JdbcDegest digest = new JdbcDegest();
        digest.init("192.168.2.38",41414,"select * from test","c");
        digest.digestData();
        digest.cleanUp();
    }
}
