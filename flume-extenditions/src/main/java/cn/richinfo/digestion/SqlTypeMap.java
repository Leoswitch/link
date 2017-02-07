package cn.richinfo.digestion;

/**
 * Created by root on 6/16/16.
 */
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Using java.utils.HashMap to store primitive data types such as int can
 * be unsafe because auto-unboxing a null value in the map can cause a NPE.
 *
 * SqlTypeMap is meant to be safer because it provides validation for arguments
 * and fails fast with informative messages if invalid arguments are given.
 */
public class SqlTypeMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 1L;

//    public static final Log LOG = LogFactory.getLog(SqlTypeMap.class.getName());

    @Override
    public V get(Object col) {
        V sqlType = super.get(col.toString().toUpperCase());
        if (sqlType == null) {
            System.out.println("It seems like you are looking up a column that does not");
            System.out.println("exist in the table. Please ensure that you've specified");
            System.out.println("correct column names in Sqoop options.");
            throw new IllegalArgumentException("column not found: " + col);
        }
        return sqlType;
    }

    @Override
    public V put(K col, V sqlType) {
        if (sqlType == null) {
            throw new IllegalArgumentException("sql type cannot be null");
        }
        return super.put(col, sqlType);
    }
}