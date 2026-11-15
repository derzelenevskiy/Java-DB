package requestHandler;

import db.Database;
import model.QueryResult;

import java.util.List;

public class RequestHandler {
    private final TokenMaker tokenMaker = new TokenMaker();
    private final SqlParser sqlParser = new SqlParser();
    private final QueryExecutor executor;

    public RequestHandler(Database db) {
        this.executor = new QueryExecutor(db);
    }

    public QueryResult parse(String sql) {
        long start = System.currentTimeMillis();
        try {
            sql = sql.trim();
            if (!sql.endsWith(";")) {
                throw new IllegalArgumentException("В конце запроса должна быть ';'");
            }

            List<String> tokens = tokenMaker.tokenMaker(sql);
            Command command = sqlParser.parse(tokens);
            QueryResult res = executor.execute(command);

            return new QueryResult(res.isSuccess(), res.errorMessage(), res.headers(), res.data(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.getClass().getSimpleName();
            }
            return new QueryResult(false, msg, new String[0], new Object[0][0], System.currentTimeMillis() - start);
        }
    }
}