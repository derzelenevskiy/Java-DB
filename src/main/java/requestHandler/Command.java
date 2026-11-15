package requestHandler;

public abstract class Command {

    public enum Type {
        SELECT, INSERT, UPDATE,DELETE, CREATE_TABLE, CREATE_INDEX, DROP_TABLE
    }

    private final Type type;
    private String tableName;

    protected Command(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}