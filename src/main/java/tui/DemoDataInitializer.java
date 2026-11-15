package tui;

import db.Database;
import model.QueryResult;
import requestHandler.RequestHandler;
import storage.FileStorageService;

public class DemoDataInitializer {

    public QueryResult initializeDemo(Database db, RequestHandler requestHandler, FileStorageService storageService) {
        try {
            safeDropTable(requestHandler, "guests");
            safeDropTable(requestHandler, "rooms");

            requestHandler.parse("CREATE TABLE rooms (id INT, number INT, type STRING, price FLOAT, is_occupied BOOLEAN);");
            requestHandler.parse("CREATE TABLE guests (id INT, full_name STRING, room_id INT, check_in STRING, check_out STRING, is_paid BOOLEAN);");

            requestHandler.parse("INSERT INTO rooms VALUES (1, 101, 'Single', 2500.0, TRUE);");
            requestHandler.parse("INSERT INTO rooms VALUES (2, 102, 'Double', 4500.0, TRUE);");
            requestHandler.parse("INSERT INTO rooms VALUES (3, 201, 'Suite', 12000.0, FALSE);");
            requestHandler.parse("INSERT INTO rooms VALUES (4, 202, 'Double', 4800.0, FALSE);");

            requestHandler.parse("INSERT INTO guests VALUES (1, 'Иван Иванов', 1, '2023-10-01', '2023-10-05', TRUE);");
            requestHandler.parse("INSERT INTO guests VALUES (2, 'Анна Петрова', 2, '2023-10-03', '2023-10-10', FALSE);");

            if (storageService != null && db != null) {
                storageService.save(db);
            }

            return requestHandler.parse("SELECT * FROM rooms;");

        } catch (Exception e) {
            return new QueryResult(false, "Demo initialization error: " + e.getMessage(), null, null, 0);
        }
    }
    public QueryResult clean(Database db, RequestHandler requestHandler, FileStorageService storageService) {
        try {
            safeDropTable(requestHandler, "guests");
            safeDropTable(requestHandler, "rooms");

            if (storageService != null && db != null) {
                storageService.save(db);
            }
            return new QueryResult(true, "Demo tables 'guests' and 'rooms' dropped successfully", null, null, 0);
        } catch (Exception e) {
            return new QueryResult(false, "Demo cleanup failed: " + e.getMessage(), null, null, 0);
        }
    }
    private void safeDropTable(RequestHandler requestHandler, String tableName) {
        try {
            requestHandler.parse("DROP TABLE " + tableName + ";");
        } catch (Exception ignored) {}
    }
}