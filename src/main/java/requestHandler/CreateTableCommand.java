package requestHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateTableCommand extends Command {
    public static class ForeignKey {
        private final String column;
        private final String refTable;
        private final String refColumn;

        public ForeignKey(String column, String refTable, String refColumn) {
            this.column = column;
            this.refTable = refTable;
            this.refColumn = refColumn;
        }

        public String getColumn() {
            return column;
        }

        public String getRefTable() {
            return refTable;
        }

        public String getRefColumn() {
            return refColumn;
        }
    }

    private final Map<String, String> columns = new LinkedHashMap<>();
    private String primaryKey;
    private final List<ForeignKey> foreignKeys = new ArrayList<>();

    public CreateTableCommand() {
        super(Type.CREATE_TABLE);
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public void addColumn(String name, String type) {
        columns.put(name, type);
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void addForeignKey(String column, String refTable, String refColumn) {
        foreignKeys.add(new ForeignKey(column, refTable, refColumn));
    }
}