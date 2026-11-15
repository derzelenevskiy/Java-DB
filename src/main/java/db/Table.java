package db;

import db.util.*;
import db.util.Comparator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Table implements Serializable {
    private final LinkedHashMap<String, Integer> columnIndexes;
    private final ArrayList<Reference> foreignKeys;
    private final LinkedHashMap<Integer, Table> references;
    private final int primary;
    private final Class<?>[] entryTemplate;
    private final HashMap<Object, Object[]> table;
//    private final ArrayList<TreeMap<Object, Set<Object>>> explicitIndexes;
//    private final ArrayList<TreeMap<Object, Set<Object>>> tempIndexes;
    private final NullObject nullObject;

    public Table(String[] fieldNames, Class<?>[] types, int primary, NullObject nullObject) {
        this(fieldNames, types, primary, new LinkedHashMap<>(), nullObject);
    }

    public Table(String[] fieldNames, Class<?>[] types, int primary, LinkedHashMap<String, Table> references, NullObject nullObject) {
        this.entryTemplate = types;
        this.primary = primary;
        this.columnIndexes = new LinkedHashMap<>();
        for (int i = 0; i < fieldNames.length; ++i) {
            this.columnIndexes.put(fieldNames[i], i);
        }
        this.table = new HashMap<>();
        this.nullObject = nullObject;
        this.foreignKeys = new ArrayList<>();
        this.references = new LinkedHashMap<>();
//        this.explicitIndexes = new ArrayList<>();
//        this.tempIndexes = new ArrayList<>();
        for (var field: references.keySet()) {
            var index = this.columnIndexes.get(field);
            if (index == null) throw new InvalidFieldException();
            this.references.put(index, references.get(field));
            references.get(field).addForeign(this, field);
        }
    }

    public void insert(Object[] values) {
        this.insert(values, new ArrayList<>(this.columnIndexes.values()));
    }

    public void insert(Object[] values, String[] fields) {
        this.insert(values, this.getFields(fields));
    }

    public void insert(Object[] values, ArrayList<Integer> fields) {
        var entry = new Object[this.entryTemplate.length];
        if (values.length != fields.size()) throw new SizeMismatchException();
        for (int i = 0; i < entry.length; ++i) {
            if (fields.contains(i)) {
                if (!this.entryTemplate[i].equals(values[fields.indexOf(i)].getClass()) && !(!(i == this.primary) && values[fields.indexOf(i)].equals(this.nullObject))) throw new TypeMismatchException();
                if (this.references.containsKey(i) && !this.references.get(i).getKeys().contains(values[fields.indexOf(i)]) && !values[fields.indexOf(i)].equals(this.nullObject))  throw new InvalidReferenceException();
                entry[i] = values[fields.indexOf(i)];
            }
            else {
                entry[i] = this.nullObject;
            }
        }

        if (entry[this.primary].equals(this.nullObject)) throw new NoPrimaryException();
        if (this.table.containsKey(entry[this.primary])) throw new PrimaryDuplicateException();

        this.table.put(entry[this.primary], entry);
    }

    public int delete(String field, Object value, Comparator comp) {
        var rows = this.select(field, value, comp);
        for (var row: rows) {
            this.table.remove(row[this.primary]);
            for (var foreign: this.foreignKeys) {
                foreign.table().update(new String[] {foreign.field()}, new NullObject[] {this.nullObject}, foreign.field(), row[this.primary], Comparator.EQUAL);
            }
        }
        return rows.length;
    }

    public int delete() {
        var rows = this.table.values().toArray(new Object[0][0]);
        for (var row: rows) {
            this.table.remove(row[this.primary]);
            for (var foreign: this.foreignKeys) {
                foreign.table().update(new String[] {foreign.field()}, new NullObject[] {this.nullObject}, foreign.field(), row[this.primary], Comparator.EQUAL);
            }
        }
        return rows.length;
    }

    public int update(String[] fields, Object[] values) {
        var fieldIndexes = this.getFields(fields);
        if (fields.length != values.length) {
            throw new SizeMismatchException();
        }
        for (int i = 0; i < fields.length; ++i) {
            if (!this.entryTemplate[fieldIndexes.get(i)].equals(values[i].getClass()) && !(values[i].equals(this.nullObject))) {
                throw new TypeMismatchException();
            }
        }
        var rows = this.table.values().toArray(new Object[0][0]);
        for (int i = 0; i < fields.length; ++i) {
            if (this.references.containsKey(fieldIndexes.get(i)) && !this.references.get(fieldIndexes.get(i)).getKeys().contains(values[i]) && !values[i].equals(this.nullObject)) throw new InvalidReferenceException();
        }
        if (fieldIndexes.contains(this.primary)) {
            var primaryIndex = fieldIndexes.indexOf(this.primary);
            if (rows.length == 0) return 0;
            if (rows.length > 1 || !values[primaryIndex].equals(rows[0][this.primary]) && this.table.get(values[primaryIndex]) != null || values[primaryIndex].equals(this.nullObject)) throw new IllegalUpdateException();

            var oldPrimary = rows[0][this.primary];
            for (int i = 0; i < fields.length; ++i) {
                if (fieldIndexes.get(i) == this.primary) {
                    for (var foreign: foreignKeys) foreign.table().update(new String[] {foreign.field()}, new Object[] {values[i]}, foreign.field(), oldPrimary, Comparator.EQUAL);
                }
                rows[0][fieldIndexes.get(i)] = values[i];
            }
            this.table.remove(oldPrimary);
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

    public int update(String[] fields, Object[] values, String compField, Object compValue, Comparator comp) {
        var fieldIndexes = this.getFields(fields);
        if (fields.length != values.length) {
            throw new SizeMismatchException();
        }
        for (int i = 0; i < fields.length; ++i) {
            if (!this.entryTemplate[fieldIndexes.get(i)].equals(values[i].getClass()) && !(values[i].equals(this.nullObject))) {
                throw new TypeMismatchException();
            }
        }
        var rows = this.select(compField, compValue, comp);
        for (int i = 0; i < fields.length; ++i) {
            if (this.references.containsKey(fieldIndexes.get(i)) && !this.references.get(fieldIndexes.get(i)).getKeys().contains(values[i]) && !values[i].equals(this.nullObject)) throw new InvalidReferenceException();
        }
        if (fieldIndexes.contains(this.primary)) {
            var primaryIndex = fieldIndexes.indexOf(this.primary);
            if (rows.length == 0) return 0;
            if (rows.length > 1 || !values[primaryIndex].equals(rows[0][this.primary]) && this.table.get(values[primaryIndex]) != null || values[primaryIndex].equals(this.nullObject)) throw new IllegalUpdateException();

            var oldPrimary = rows[0][this.primary];
            for (int i = 0; i < fields.length; ++i) {
                if (fieldIndexes.get(i) == this.primary) {
                    for (var foreign: foreignKeys) foreign.table().update(new String[] {foreign.field()}, new Object[] {values[i]}, foreign.field(), oldPrimary, Comparator.EQUAL);
                }
                rows[0][fieldIndexes.get(i)] = values[i];
            }
            this.table.remove(oldPrimary);
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

    public Object[][] select(String[] fields, String field, Object value, Comparator comp) {
        var rows = this.select(field, value, comp);
        var cropped = new ArrayList<Object[]>();
        var fieldIndexes = this.getFields(fields);
        for (var row: rows) {
            cropped.add(this.crop(fieldIndexes, row));
        }
        return cropped.toArray(new Object[0][0]);
    }

    public Object[][] select(String field, Object value, Comparator comp) {
        var rows = new ArrayList<Object[]>();
        var fieldIndex = this.columnIndexes.get(field);
        if (fieldIndex == null) throw new InvalidFieldException();
        if (!this.entryTemplate[fieldIndex].equals(value.getClass()) && !((comp == Comparator.EQUAL || comp == Comparator.NEQUAL) && value.equals(this.nullObject))) throw new TypeMismatchException();
        if (fieldIndex == this.primary && comp == Comparator.EQUAL) {
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
                    if (entry[fieldIndex].equals(value)) rows.add(entry);
                }
            }

            case NEQUAL -> {
                for (var entry: this.table.values()) {
                    if (!entry[fieldIndex].equals(value)) rows.add(entry);
                }
            }

            case LESS -> {
                if (!comparable) throw new TypeMismatchException();
                for (var entry: this.table.values()) {
                    if (!entry[fieldIndex].equals(this.nullObject) && this.compare(entry[fieldIndex], value) < 0) rows.add(entry);
                }
            }

            case GREATER -> {
                if (!comparable) throw new TypeMismatchException();
                for (var entry: this.table.values()) {
                    if (!entry[fieldIndex].equals(this.nullObject) && this.compare(entry[fieldIndex], value) > 0) rows.add(entry);
                }
            }

            case LESSorEQUAL -> {
                if (!comparable) throw new TypeMismatchException();
                for (var entry: this.table.values()) {
                    if (!entry[fieldIndex].equals(this.nullObject) && this.compare(entry[fieldIndex], value) <= 0) rows.add(entry);
                }
            }

            case GREATERorEQUAL -> {
                if (!comparable) throw new TypeMismatchException();
                for (var entry: this.table.values()) {
                    if (!entry[fieldIndex].equals(this.nullObject) && this.compare(entry[fieldIndex], value) >= 0) rows.add(entry);
                }
            }
        }
        return rows.toArray(new Object[0][0]);
    }

    public Object[][] select(String[] fields) {
        var rows = new ArrayList<Object[]>();
        var fieldIndexes = this.getFields(fields);
        for (var row: this.table.values()) {
            rows.add(crop(fieldIndexes, row));
        }
        return rows.toArray(new Object[0][0]);
    }

    public void addForeign(Table table, String field) {
        this.foreignKeys.add(new Reference(table, field));
    }

    public String[] getFieldNames() {
        return this.columnIndexes.keySet().toArray(new String[0]);
    }

    public Class<?> getFieldType(String field) {
        var index = this.columnIndexes.get(field);
        if (index == null) throw new InvalidFieldException();
        return this.entryTemplate[index];
    }

    private Set<Object> getKeys() {
        return this.table.keySet();
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
            var index = this.columnIndexes.get(f);
            if (index != null) fieldIndexes.add(index);
            else throw new InvalidFieldException();
        }
        return fieldIndexes;
    }

    private int compare(Object a, Object b) {
        if (a instanceof Number numA && b instanceof Number numB) {
            return Double.compare(numA.doubleValue(), numB.doubleValue());
        }
        if (a.getClass().equals(b.getClass()) && a instanceof Comparable compA) {
            return compA.compareTo(b);
        }
        throw new TypeMismatchException();
    }
}
