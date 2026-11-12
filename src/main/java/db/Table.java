package db;

import db.util.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Table {
    private final HashMap<String, Integer> columnIndexes;
    private final int primary;
    private final Class<?>[] entryTemplate;
    private final HashMap<Object, Object[]> table;

    public Table(String[] fieldNames, Class<?>[] types, int primary) {
        this.entryTemplate = types;
        this.primary = primary;
        this.columnIndexes = new HashMap<>();
        for (int i = 0; i < fieldNames.length; ++i) {
            this.columnIndexes.put(fieldNames[i], i);
        }
        this.table = new HashMap<>();
    }

    public void insert(Object[] values) throws TypeMismatchException, NoPrimaryException, PrimaryDuplicateException {
        this.insert(values, new ArrayList<>(this.columnIndexes.values()));
    }

    public void insert(Object[] values, String[] fields) throws TypeMismatchException, NoPrimaryException, PrimaryDuplicateException {
        var fieldIndexes = new ArrayList<Integer>();
        for (var field: fields) {
            fieldIndexes.add(this.columnIndexes.get(field));
        }
        this.insert(values, fieldIndexes);
    }

    public void insert(Object[] values, ArrayList<Integer> fields) throws TypeMismatchException, NoPrimaryException, PrimaryDuplicateException {
        var entry = new Object[this.entryTemplate.length];
        for (int i = 0; i < entry.length; ++i) {
            if (fields.contains(i)) {
                if (this.entryTemplate[i].equals(values[fields.indexOf(i)].getClass())) {
                    entry[i] = values[fields.indexOf(i)];
                }
                else {
                    throw new TypeMismatchException();
                }
            }
            else {
                entry[i] = null;
            }
        }

        if (entry[this.primary] == null) {
            throw new NoPrimaryException();
        }
        if (this.table.containsKey(entry[this.primary])) {
            throw new PrimaryDuplicateException();
        }

        this.table.put(entry[this.primary], entry);
    }

    public int delete(String field, Object value, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        var rows = this.select(field, value, comp);
        for (var row: rows) {
            this.table.remove(row[this.primary]);
        }
        return rows.length;
    }

    public int update(String field, Object value, String compField, Object compValue, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        var fieldIndex = this.columnIndexes.get(field);
        if (!this.entryTemplate[fieldIndex].equals(value.getClass())) {
            throw new TypeMismatchException();
        }
        var rows = this.select(compField, compValue, comp);
        for (var row: rows) {
            this.table.get(row[this.primary])[fieldIndex] = value;
        }
        return rows.length;
    }

    public Object[][] select(String[] fields, String field, Object value, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        var rows = this.select(field, value, comp);
        var cropped = new ArrayList<Object[]>();
        var fieldIndexes = new ArrayList<Integer>();
        for (var f: fields) {
            fieldIndexes.add(this.columnIndexes.get(f));
        }
        for (var row: rows) {
            cropped.add(this.crop(fieldIndexes, row));
        }
        return cropped.toArray(new Object[0][0]);
    }

    public Object[][] select(String field, Object value, Comparator comp) throws TypeMismatchException, IllegalComparatorException {
        var rows = new ArrayList<Object[]>();
        var fieldIndex = this.columnIndexes.get(field);
        if (fieldIndex == this.primary) {
            if (!this.entryTemplate[this.primary].equals(value.getClass())) {
                throw new TypeMismatchException();
            }
            if (comp != Comparator.EQUAL) {
                throw new IllegalComparatorException();
            }
            var selectedRow = this.table.get(value);
            if (selectedRow != null) {
                rows.add(selectedRow);
            }
            return rows.toArray(new Object[0][0]);
        }

        switch (comp) {
            case EQUAL -> {
                for (var entry: this.table.values()) {
                    if (entry[fieldIndex].equals(value)) {
                        rows.add(entry);
                    }
                }
            }

            case NEQUAL -> {
                for (var entry: this.table.values()) {
                    if (!entry[fieldIndex].equals(value)) {
                        rows.add(entry);
                    }
                }
            }

            case LESS -> {
                if (!this.entryTemplate[fieldIndex].getSuperclass().equals(Number.class) || !value.getClass().getSuperclass().equals(Number.class)) {
                    throw new TypeMismatchException();
                }
                for (var entry: this.table.values()) {
                    if (((Number)entry[fieldIndex]).doubleValue() < ((Number)value).doubleValue()) {
                        rows.add(entry);
                    }
                }
            }

            case GREATER -> {
                if (!this.entryTemplate[fieldIndex].getSuperclass().equals(Number.class) || !value.getClass().getSuperclass().equals(Number.class)) {
                    throw new TypeMismatchException();
                }
                for (var entry: this.table.values()) {
                    if (((Number)entry[fieldIndex]).doubleValue() > ((Number)value).doubleValue()) {
                        rows.add(entry);
                    }
                }
            }

            case LESSorEQUAL -> {
                if (!this.entryTemplate[fieldIndex].getSuperclass().equals(Number.class) || !value.getClass().getSuperclass().equals(Number.class)) {
                    throw new TypeMismatchException();
                }
                for (var entry: this.table.values()) {
                    if (((Number)entry[fieldIndex]).doubleValue() <= ((Number)value).doubleValue()) {
                        rows.add(entry);
                    }
                }
            }

            case GREATERorEQUAL -> {
                if (!this.entryTemplate[fieldIndex].getSuperclass().equals(Number.class) || !value.getClass().getSuperclass().equals(Number.class)) {
                    throw new TypeMismatchException();
                }
                for (var entry: this.table.values()) {
                    if (((Number)entry[fieldIndex]).doubleValue() >= ((Number)value).doubleValue()) {
                        rows.add(entry);
                    }
                }
            }
        }
        return rows.toArray(new Object[0][0]);
    }


    public Object[][] select(String[] fields) {
        var rows = new ArrayList<Object[]>();
        var fieldIndexes = new ArrayList<Integer>();
        for (var f: fields) {
            fieldIndexes.add(this.columnIndexes.get(f));
        }
        for (var row: this.table.values()) {
            rows.add(crop(fieldIndexes, row));
        }
        return rows.toArray(new Object[0][0]);
    }

    private Object[] crop(ArrayList<Integer> fields, Object[] entry) {
        var croppedRow = new ArrayList<>();
        for (var i: fields) {
            croppedRow.add(entry[i]);
        }
        return croppedRow.toArray();
    }
}
