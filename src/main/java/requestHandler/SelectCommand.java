package requestHandler;

import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends Command {
    private final List<String> columns = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();

    public SelectCommand() {
        super(Type.SELECT);
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }
}
