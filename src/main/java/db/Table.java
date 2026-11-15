package db;

import db.util.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Table implements Serializable {
    private final LinkedHashMap<String, Integer> columnIndexes;
    private final int primary;
    private final Class<?>[] entryTemplate;
    private final HashMap<Object, Object[]> table;
    private final NullObject nullObject;

    public Table(String[] fieldNames, Class<?>[] types, int primary, NullObject nullObject) {
        this.entryTemplate = types;
        this.primary = primary;
        this.columnIndexes = new LinkedHashMap<>();
        for (int i = 0; i < fieldNames.length; ++i) {
            this.columnIndexes.put(fieldNames[i], i);
        }
        this.table = new HashMap<>();
        this.nullObject = nullObject;
    }

    public void insert(Object[] values) throws TypeMismatchException, NoPrimaryException, PrimaryDuplicateException, SizeMismatchException {
        this.insert(values, new ArrayList<>(this.columnIndexes.values()));
    }

    public void insert(Object[] values, String[] fields) throws TypeMismatchException, NoPrimaryException, PrimaryDuplicateException, SizeMismatchException {
        var fieldIndexes = new ArrayList<Integer>();
        for (var field: fields) {
            fieldIndexes.add(this.columnIndexes.get(field));
        }
        this.insert(values, fieldIndexes);
    }

    public void insert(Object[] values, ArrayList<Integer> fields) throws TypeMismatchException, NoPrimaryException, PrimaryDuplicateException, SizeMismatchException {
        var entry = new Object[this.entryTemplate.length];
        if (values.length != fields.size()) {
            throw new SizeMismatchException();
        }
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
                entry[i] = this.nullObject;
            }
        }

        if (entry[this.primary].equals(this.nullObject)) {
            throw new NoPrimaryException();
        }
        if (this.table.containsKey(entry[this.primary])) {
            throw new PrimaryDuplicateException();
        }

        this.table.put(entry[this.primary], entry);
    }

    public int delete(String field, Object value, Comparator comp) throws TypeMismatchException {
        var rows = this.select(field, value, comp);
        for (var row: rows) {
            this.table.remove(row[this.primary]);
        }
        return rows.length;
    }

    public int update(String[] fields, Object[] values, String compField, Object compValue, Comparator comp) throws TypeMismatchException, SizeMismatchException, IllegalUpdateException {
        var fieldIndexes = this.getFields(fields);
        if (fields.length != values.length) {
            throw new SizeMismatchException();
        }
        for (int i = 0; i < fields.length; ++i) {
            if (!this.entryTemplate[fieldIndexes.get(i)].equals(values[i].getClass())) {
                throw new TypeMismatchException();
            }
        }
        var rows = this.select(compField, compValue, comp);
        if (fieldIndexes.contains(this.primary)) {
            var primaryIndex = fieldIndexes.indexOf(this.primary);
            if (rows.length > 1 || this.table.get(values[primaryIndex]) != null || values[primaryIndex].equals(this.nullObject)) throw new IllegalUpdateException();
            if (rows.length == 0) return 0;

            this.table.remove(rows[0][this.primary]);
            for (int i = 0; i < fields.length; ++i) {
                rows[0][fieldIndexes.get(i)] = values[i];
            }
            this.table.put(values[primaryIndex], rows[0]);
            return 1;
        }
        for (var row: rows) {
            for (int i = 0; i < fields.length; ++i) {
                this.table.get(row[this.primary])[fieldIndexes.get(i)] = values[i];
            }
        }
        return rows.length;
    }

    public Object[][] select(String[] fields, String field, Object value, Comparator comp) throws TypeMismatchException {
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

    public Object[][] select(String field, Object value, Comparator comp) throws TypeMismatchException {
        var rows = new ArrayList<Object[]>();
        var fieldIndex = this.columnIndexes.get(field);
        if (fieldIndex == this.primary && comp == Comparator.EQUAL) {
            if (!this.entryTemplate[this.primary].equals(value.getClass())) {
                throw new TypeMismatchException();
            }
            var selectedRow = this.table.get(value);
            if (selectedRow != null) {
                rows.add(selectedRow);
            }
            return rows.toArray(new Object[0][0]);
        }

        var comparable = value.getClass().equals(Integer.class) || value.getClass().equals(Double.class) || value.getClass().equals(LocalDate.class);
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
                if (!comparable) {
                    throw new TypeMismatchException();
                }
                for (var entry: this.table.values()) {
                    if (((Number)entry[fieldIndex]).doubleValue() < ((Number)value).doubleValue()) {
                        rows.add(entry);
                    }
                }
            }

            case GREATER -> {
                if (!comparable) {
                    throw new TypeMismatchException();
                }
                for (var entry: this.table.values()) {
                    if (((Number)entry[fieldIndex]).doubleValue() > ((Number)value).doubleValue()) {
                        rows.add(entry);
                    }
                }
            }

            case LESSorEQUAL -> {
                if (!comparable) {
                    throw new TypeMismatchException();
                }
                for (var entry: this.table.values()) {
                    if (((Number)entry[fieldIndex]).doubleValue() <= ((Number)value).doubleValue()) {
                        rows.add(entry);
                    }
                }
            }

            case GREATERorEQUAL -> {
                if (!comparable) {
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

    private ArrayList<Integer> getFields(String[] fields) {
        var fieldIndexes = new ArrayList<Integer>();
        for (var f: fields) {
            fieldIndexes.add(this.columnIndexes.get(f));
        }
        return fieldIndexes;
    }
}
