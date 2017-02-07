package cn.richinfo.digestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 6/16/16.
 */
public class OracleManager {

    private String url;
    private String user;
    private String password;
    private Connection connection;
    private Statement statement;

    public OracleManager(String url,String user,String password){
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection makeConnection() throws SQLException{
        try {
            //调用Class.forName()方法加载驱动程序
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return connection;
    }

    public Connection getConnection() throws SQLException {
        if (null == this.connection|| this.connection.isClosed()) {
            this.connection = makeConnection();
        }

        return this.connection;
    }

    public void release() {
        if (null != this.statement) {
            try {
                this.statement.close();
            } catch (SQLException e) {
            }

            this.statement = null;
        }
    }

    protected ResultSet execute(String stmt, Integer fetchSize, Object... args)
            throws SQLException {
        // Release any previously-open statement.
        release();

        PreparedStatement preparedstatement = null;
        preparedstatement = this.getConnection().prepareStatement(stmt,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if (fetchSize != null) {
            preparedstatement.setFetchSize(fetchSize);
        }
        this.statement = preparedstatement;
        if (null != args) {
            for (int i = 0; i < args.length; i++) {
                preparedstatement.setObject(i + 1, args[i]);
            }
        }

        System.out.println("Executing SQL statement: " + stmt);
        return preparedstatement.executeQuery();
    }

    protected Map<String, Integer> getColumnTypesForRawQuery(String stmt) {
        Map<String, List<Integer>> colInfo = getColumnInfoForRawQuery(stmt);
        if (colInfo == null) {
            return null;
        }
        Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
        for (String s : colInfo.keySet()) {
            List<Integer> info = colInfo.get(s);
            colTypes.put(s, info.get(0));
        }
        return colTypes;
    }

    private Map<String, List<Integer>> getColumnInfoForRawQuery(String stmt) {
        ResultSet results;
        System.out.println("Execute getColumnInfoRawQuery : " + stmt);
        try {
            results = execute(stmt,1000);
        } catch (SQLException sqlE) {
            release();
            throw new RuntimeException("Error executing statement: " + sqlE.toString() + "\r\n excuteSql:"+stmt);
        }

        try {
            Map<String, List<Integer>> colInfo = new SqlTypeMap<String, List<Integer>>();

            int cols = results.getMetaData().getColumnCount();
            ResultSetMetaData metadata = results.getMetaData();
            for (int i = 1; i < cols + 1; i++) {
                int typeId = metadata.getColumnType(i);
                int precision = -1;
                if(typeId != Types.CLOB&&
                        typeId != Types.BLOB){
                    precision = metadata.getPrecision(i);
                }
                int scale = metadata.getScale(i);

                // If we have an unsigned int we need to make extra room by
                // plopping it into a bigint
                if (typeId == Types.INTEGER &&  !metadata.isSigned(i)){
                    typeId = Types.BIGINT;
                }

                String colName = metadata.getColumnLabel(i);
                if (colName == null || colName.equals("")) {
                    colName = metadata.getColumnName(i);
                }
                List<Integer> info = new ArrayList<Integer>(3);
                info.add(Integer.valueOf(typeId));
                info.add(precision);
                info.add(scale);
                colInfo.put(colName, info);
            }

            return colInfo;
        } catch (SQLException sqlException) {
            return null;
        } finally {
            try {
                results.close();
                getConnection().commit();
            } catch (SQLException sqlE) {
                sqlE.printStackTrace();
            }

            release();
        }
    }

    public String datetimeToQueryString(String datetime, int columnType) {
        if (columnType == Types.TIMESTAMP) {
            return "TO_TIMESTAMP('" + datetime + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
        } else if (columnType == Types.DATE) {
            // converting timestamp of the form 2012-11-11 11:11:11.00 to
            // date of the form 2011:11:11 11:11:11
            datetime = datetime.split("\\.")[0];
            return "TO_DATE('" + datetime + "', 'YYYY-MM-DD HH24:MI:SS')";
        } else {
            String msg = "Column type is neither timestamp nor date!";
            System.out.println(msg);
            throw new RuntimeException(msg);
        }
    }


    public String appendCodition(String selectSql,String condition){

        StringBuilder ret = new StringBuilder(selectSql);
        if(selectSql.contains("order")||selectSql.contains("ORDER")){
//            throw new RuntimeException("selectsql contain order by key:"+selectSql);
        }
        if(selectSql.contains("where")){
            ret.append(" and "+condition);
        }else{
            ret.append(" where "+condition);
        }
        return ret.toString();
    }

    public String appendOrderBy(String selectSql,String increamentCol){

        StringBuilder ret = new StringBuilder(selectSql);
        if(selectSql.contains("order")||selectSql.contains("ORDER")){
//            throw new RuntimeException("selectsql contain order by key:"+selectSql);
        }else{
            ret.append(" order by "+increamentCol+" asc");
        }
        return ret.toString();
    }

}
