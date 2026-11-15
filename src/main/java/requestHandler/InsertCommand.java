package requestHandler;

import java.util.ArrayList;
import java.util.List;

public class InsertCommand extends Command {
    private final List<String> columns = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

    public InsertCommand() {
        super(Type.INSERT);
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public void addRow(List<String> row) {
        rows.add(row);
    }
}