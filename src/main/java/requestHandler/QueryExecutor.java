package requestHandler;
import db.Database;
import db.util.*;
import model.QueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryExecutor {
    private final Database db;

    public QueryExecutor(Database db) {
        this.db = db;
    }

    public QueryResult execute(Command command)
            throws PrimaryDuplicateException, TypeMismatchException, NoPrimaryException, SizeMismatchException, IllegalUpdateException {
        switch (command.getType()) {
            case CREATE_TABLE:
                return executeCreateTable((CreateTableCommand) command);
            case DROP_TABLE:
                return executeDropTable((DropTableCommand) command);
            case INSERT:
                return executeInsert((InsertCommand) command);
            case SELECT:
                return executeSelect((SelectCommand) command);
            case UPDATE:
                return executeUpdate((UpdateCommand) command);
            case DELETE:
                return executeDelete((DeleteCommand) command);
            default:
                throw new IllegalArgumentException("Неподдерживаемая команда: " + command.getType());
        }
    }

    private QueryResult executeCreateTable(CreateTableCommand cmd) {
        Map<String, String> columns = cmd.getColumns();
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("В таблице должен быть хотя бы один столбец");
        }
        if (cmd.getPrimaryKey() == null) {
            throw new IllegalArgumentException("В таблице должен быть PRIMARY KEY");
        }

        String[] fieldNames = new String[columns.size()];
        Class<?>[] types = new Class<?>[columns.size()];
        int primary = 0;

        int i = 0;
        for (String columnName : columns.keySet()) {
            String columnType = columns.get(columnName);

            fieldNames[i] = columnName;
            types[i] = ValueConverter.parseType(columnType);

            if (columnName.equals(cmd.getPrimaryKey())) {
                primary = i;
            }
            i++;
        }

        if (cmd.getReferences().isEmpty()) {
            db.createTable(cmd.getTableName(), fieldNames, types, primary);
        } else {
            db.createTable(cmd.getTableName(), fieldNames, types, primary, cmd.getReferences());
        }

        return Message("Таблица " + cmd.getTableName() + " создана");
    }

    private QueryResult executeDropTable(DropTableCommand cmd) {
        db.dropTable(cmd.getTableName());
        return Message("Таблица " + cmd.getTableName() + " удалена");
    }

    private Object convertValue(String rawValue, Class<?> colType) {
        Object value = ValueConverter.parseValue(rawValue, colType);
        if (value == null) {
            return db.getNull();
        }
        return value;
    }

    private QueryResult executeInsert(InsertCommand cmd)
            throws PrimaryDuplicateException, TypeMismatchException, NoPrimaryException, SizeMismatchException {
        String tableName = cmd.getTableName();

        String[] Columns;
        if (cmd.getColumns().isEmpty()) {
            Columns = db.getFieldNames(tableName);
        } else {
            List<String> cols = cmd.getColumns();
            Columns = new String[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                Columns[i] = cols.get(i);
            }
        }

        for (List<String> rows : cmd.getRows()) {
            if (rows.size() != Columns.length) {
                throw new IllegalArgumentException("Количество значений не совпадает с количеством столбцов");
            }

            Object[] rowValue = new Object[Columns.length];
            for (int i = 0; i < Columns.length; i++) {
                Class<?> colType = db.getFieldType(tableName, Columns[i]);
                rowValue[i] = convertValue(rows.get(i), colType);
            }

            if (cmd.getColumns().isEmpty()) {
                db.insert(tableName, rowValue);
            } else {
                db.insert(tableName, rowValue, Columns);
            }
        }

        return Message("Добавлено записей: " + cmd.getRows().size());
    }

    private QueryResult executeSelect(SelectCommand cmd) throws TypeMismatchException {
        String tableName = cmd.getTableName();
        List<String> Cols = new ArrayList<>();

        for (String col : cmd.getColumns()) {
            if (col.equals("*")) {
                for (String tableCol : db.getFieldNames(tableName)) {
                    Cols.add(tableCol);
                }
            } else {
                Cols.add(col);
            }
        }

        String[] fields = Cols.toArray(new String[0]);
        Object[][] rows;

        if (cmd.getConditions().isEmpty()) {
            rows = db.select(tableName, fields);
        } else {
            Condition cond = cmd.getConditions().get(0);
            Class<?> colType = db.getFieldType(tableName, cond.getColumn());
            Object value = convertValue(cond.getValue(), colType);
            Comparator comp = ValueConverter.parseComparator(cond.getOperator());
            rows = db.select(tableName, fields, cond.getColumn(), value, comp);
        }

        return new QueryResult(true, null, fields, rows, 0);
    }

    private QueryResult executeUpdate(UpdateCommand cmd)
            throws TypeMismatchException, SizeMismatchException, IllegalUpdateException {
        String tableName = cmd.getTableName();
        String[] fields = new String[cmd.getUpdates().size()];
        Object[] values = new Object[cmd.getUpdates().size()];

        int i = 0;
        for (String field : cmd.getUpdates().keySet()) {
            String rawValue = cmd.getUpdates().get(field);
            Class<?> colType = db.getFieldType(tableName, field);

            fields[i] = field;
            values[i] = convertValue(rawValue, colType);
            i++;
        }

        int cnt;
        if (cmd.getConditions().isEmpty()) {
            cnt = db.update(tableName, fields, values);
        } else {
            Condition cond = cmd.getConditions().get(0);
            Class<?> colType = db.getFieldType(tableName, cond.getColumn());
            Object Value = convertValue(cond.getValue(), colType);
            Comparator comp = ValueConverter.parseComparator(cond.getOperator());
            cnt = db.update(tableName, fields, values, cond.getColumn(), Value, comp);
        }

        return Message("Обновлено записей: " + cnt);
    }

    private QueryResult executeDelete(DeleteCommand cmd) throws TypeMismatchException {
        String tableName = cmd.getTableName();
        int cnt;

        if (cmd.getConditions().isEmpty()) {
            cnt = db.delete(tableName);
        } else {
            Condition cond = cmd.getConditions().get(0);
            Class<?> colType = db.getFieldType(tableName, cond.getColumn());
            Object value = convertValue(cond.getValue(), colType);
            Comparator comp = ValueConverter.parseComparator(cond.getOperator());
            cnt = db.delete(tableName, cond.getColumn(), value, comp);
        }

        return Message("Удалено записей: " + cnt);
    }

    private QueryResult Message(String message) {
        return new QueryResult(true, null, new String[]{"Status"}, new Object[][]{{message}}, 0);
    }
}