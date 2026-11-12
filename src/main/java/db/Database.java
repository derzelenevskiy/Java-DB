package db;

import db.util.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    private final HashMap<String, Table> tables;

    public Database() {
        this.tables = new HashMap<>();
    }

    public void createTable(String tableName, String[] fieldNames, Class<?>[] types, int primary) {
        this.tables.put(tableName, new Table(fieldNames, types, primary));
    }

    public void dropTable(String tableName) {
        this.tables.remove(tableName);
    }

    public void insert(String tableName, Object[] values) throws PrimaryDuplicateException, TypeMismatchException, NoPrimaryException {
        this.tables.get(tableName).insert(values);
    }

    public void insert(String tableName, Object[] values, String[] fields) throws PrimaryDuplicateException, TypeMismatchException, NoPrimaryException {
        this.tables.get(tableName).insert(values, fields);
    }

    public void insert(String tableName, Object[] values, ArrayList<Integer> fields) throws PrimaryDuplicateException, TypeMismatchException, NoPrimaryException {
        this.tables.get(tableName).insert(values, fields);
    }

    public int delete(String tableName, String field, Object value, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        return this.tables.get(tableName).delete(field, value, comp);
    }

    public int update(String tableName, String field, Object value, String compField, Object compValue, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        return this.tables.get(tableName).update(field, value, compField, compValue, comp);
    }

    public Object[][] select(String tableName, String[] fields, String field, Object value, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        return this.tables.get(tableName).select(fields, field, value, comp);
    }

    public Object[][] select(String tableName, String field, Object value, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        return this.tables.get(tableName).select(field, value, comp);
    }

    public Object[][] select(String tableName, String[] fields) throws TypeMismatchException, IllegalComparatorException {
        return this.tables.get(tableName).select(fields);
    }
}
