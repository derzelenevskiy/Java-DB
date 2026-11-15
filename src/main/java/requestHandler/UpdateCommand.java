package requestHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateCommand extends Command {
    private final Map<String, String> updates = new LinkedHashMap<>();
    private final List<Condition> conditions = new ArrayList<>();

    public UpdateCommand() {
        super(Type.UPDATE);
    }

    public Map<String, String> getUpdates() {
        return updates;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void addUpdate(String column, String value) {
        updates.put(column, value);
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }
}