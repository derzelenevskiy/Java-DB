package requestHandler;

public class CreateIndexCommand extends Command {
    private String indexName;
    private String columnName;

    public CreateIndexCommand() {
        super(Type.CREATE_INDEX);
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
