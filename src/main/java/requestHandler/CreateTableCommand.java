package requestHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class CreateTableCommand extends Command {
    private final Map<String, String> columns = new LinkedHashMap<>();
    private String primaryKey;
    private final Map<String, String> references = new LinkedHashMap<>();

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

    public Map<String, String> getReferences() {
        return references;
    }

    public void addReference(String column, String refTable) {
        references.put(column, refTable);
    }
}