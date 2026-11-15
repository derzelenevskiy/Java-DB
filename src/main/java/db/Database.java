package db;

import db.util.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Database implements Serializable {
    private final LinkedHashMap<String, Table> tables;
    private final NullObject nullObject;

    public Database() {
        this.tables = new LinkedHashMap<>();
        this.nullObject = new NullObject();
    }

    public void createTable(String tableName, String[] fieldNames, Class<?>[] types, int primary) {
        this.tables.put(tableName, new Table(fieldNames, types, primary, this.nullObject));
    }

    public void dropTable(String tableName) {
        this.tables.remove(tableName);
    }

    public void insert(String tableName, Object[] values) {
        this.tables.get(tableName).insert(values);
    }

    public void insert(String tableName, Object[] values, String[] fields) {
        this.tables.get(tableName).insert(values, fields);
    }

    public void insert(String tableName, Object[] values, ArrayList<Integer> fields) {
        this.tables.get(tableName).insert(values, fields);
    }

    public int delete(String tableName, String field, Object value, Comparator comp) {
        return this.tables.get(tableName).delete(field, value, comp);
    }

    public int delete(String tableName) {
        return this.tables.get(tableName).delete();
    }

    public int update(String tableName, String[] fields, Object[] values, String compField, Object compValue, Comparator comp) {
        return this.tables.get(tableName).update(fields, values, compField, compValue, comp);
    }

    public int update(String tableName, String[] fields, Object[] values) {
        return this.tables.get(tableName).update(fields, values);
    }

    public Object[][] select(String tableName, String[] fields, String field, Object value, Comparator comp) {
        return this.tables.get(tableName).select(fields, field, value, comp);
    }

    public Object[][] select(String tableName, String field, Object value, Comparator comp) {
        return this.tables.get(tableName).select(field, value, comp);
    }

    public Object[][] select(String tableName, String[] fields) {
        return this.tables.get(tableName).select(fields);
    }

    public NullObject getNull() {
        return this.nullObject;
    }

    public String[] getTableNames() {
        return this.tables.keySet().toArray(new String[0]);
    }

    public String[] getFieldNames(String tableName) {
        return this.tables.get(tableName).getFieldNames();
    }

    public Class<?> getFieldType(String tableName, String field) {
        return this.tables.get(tableName).getFieldType(field);
    }
}