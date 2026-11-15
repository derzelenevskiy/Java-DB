package requestHandler;

import java.util.ArrayList;
import java.util.List;

public class DeleteCommand extends Command {
    private final List<Condition> conditions = new ArrayList<>();

    public DeleteCommand() {
        super(Type.DELETE);
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }
}