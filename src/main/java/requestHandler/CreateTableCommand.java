package requestHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class CreateTableCommand extends Command {
    private final Map<String, String> columns = new LinkedHashMap<>();

    public CreateTableCommand() {
        super(Type.CREATE_TABLE);
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public void addColumn(String name, String type) {
        columns.put(name, type);
    }
}