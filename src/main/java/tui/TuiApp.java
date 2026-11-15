package tui;

import db.Database;
import dev.tamboui.text.Text;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.tui.event.TickEvent;
import model.QueryResult;
import requestHandler.RequestHandler;
import requestHandler.SqlParser;
import storage.FileStorageService;

import dev.tamboui.tui.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TuiApp {
    private Database db;
    private final FileStorageService storageService;
    private final RequestHandler requestHandler;
    private final UiState uiState;
    private final MainView mainView;

    public TuiApp(Database db, FileStorageService storageService, RequestHandler requestHandler) {
        this.db = db;
        this.storageService = storageService;
        this.requestHandler = requestHandler;
        List<UiState.PaletteCommand> paletteCommandList = new ArrayList<>();

        paletteCommandList.addAll(createPaletteCommandList());

        List<UiState.DirectoryCommand> directoryCommandList = new ArrayList<>();

        this.mainView = new MainView();
        this.uiState = new UiState(paletteCommandList, directoryCommandList);
    }
    public void run() throws IOException {
        TuiConfig config = TuiConfig.builder()
                .hideCursor(false)
                .build();
        try (TuiRunner runner = TuiRunner.create(config)) {
            while (uiState.isRunning()) {
                runner.draw(frame -> mainView.render(frame.area(), frame, uiState));

                var event = runner.pollEvent();
                switch (event) {
                    case null -> {
                        continue;
                    }
                    case KeyEvent keyEvent -> {
                        if (keyEvent.code() == KeyCode.ENTER && keyEvent.modifiers().alt()) {
                            String sql = uiState.getInputState().text();

                            if (!sql.isBlank()) {
                                try {
                                    QueryResult result = requestHandler.parse(List.of(sql));
                                    uiState.addCmd(sql, result);
                                    storageService.save(db);
                                } catch (Exception e) {
                                    QueryResult result = new QueryResult(false, e.getMessage(), null, null, 0);
                                    uiState.addCmd(sql, result);
                                }
                            }
                        } else {
                            uiState.onKeyEvent(keyEvent);
                        }
                    }
                    case TickEvent tickEvent -> uiState.getLogoWaveState().advance();
                    default -> {
                    }
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            storageService.save(db);
        }
    }


    private List<UiState.PaletteCommand> createPaletteCommandList() {
        return List.of(
                new UiState.PaletteCommand("Show tables", "\\dt", () -> {
                    QueryResult res = requestHandler.parse("SELECT * FROM db.getTableNames()");
                    uiState.addCmd("\\dt", res);
                }),
                new UiState.PaletteCommand("Show indexes", "\\di", () -> {
                    QueryResult res = requestHandler.parse("SELECT * FROM db.getTableIndexes()");
                    uiState.addCmd("\\di", res);
                }),
                new UiState.PaletteCommand("Working directory", "ctrl+d", () -> {
                    uiState.openDirectory();
                }),
                new UiState.PaletteCommand("Save database to disk", "ctrl+s", () -> {
                    try {
                        storageService.save(db);
                        uiState.addCmd("\\save", new QueryResult(true, "Database successfully saved to disk", null, null, 0));
                    } catch (Exception e) {
                        uiState.addCmd("\\save", new QueryResult(false, "Failed to save database:" + e.getMessage(), null, null, 0));
                    }
                }),
                new UiState.PaletteCommand("Load demo sample data", "\\demo", () -> {
                    try {
                        requestHandler.parse(List.of("DROP TABLE IF EXISTS rooms"));
                        requestHandler.parse(List.of("DROP TABLE IF EXISTS guests"));
                        requestHandler.parse(List.of("CREATE TABLE rooms (id INT, number INT, type STRING, price FLOAT, is_occupied BOOLEAN);"));
                        requestHandler.parse(List.of("CREATE TABLE guests (id INT, full_name STRING, room_id INT, check_in STRING, check_out STRING, is_paid BOOLEAN);"));

                        requestHandler.parse(List.of("INSERT INTO rooms VALUES (1, 101, 'Single', 2500.0, TRUE);"));
                        requestHandler.parse(List.of("INSERT INTO rooms VALUES (2, 102, 'Double', 4500.0, TRUE);"));
                        requestHandler.parse(List.of("INSERT INTO rooms VALUES (3, 201, 'Suite', 12000.0, FALSE);"));
                        requestHandler.parse(List.of("INSERT INTO rooms VALUES (4, 202, 'Double', 4800.0, FALSE);"));

                        requestHandler.parse(List.of("INSERT INTO guests VALUES (1, 'Иван Иванов', 1, '2023-10-01', '2023-10-05', TRUE);"));
                        requestHandler.parse(List.of("INSERT INTO guests VALUES (2, 'Анна Петрова', 2, '2023-10-03', '2023-10-10', FALSE);"));

                        storageService.save(db);
                        uiState.addCmd("\\demo", new QueryResult(true, "Demo tables 'rooms' and 'guests' created", null, null, 0));
                    } catch (Exception e) {
                        uiState.addCmd("\\demo", new QueryResult(false, "Demo load failed: " + e.getMessage(), null, null, 0));
                    }
                }),
                new UiState.PaletteCommand("Run benchmark: Room Type Search", "\\bench", () -> {
                    try {
                        long startScan = System.nanoTime();
                        requestHandler.parse(List.of("SELECT * FROM rooms WHERE type = 'Suite';"));
                        long scanTime = (System.nanoTime() - startScan) / 1_000_000;

                        requestHandler.parse(List.of("CREATE INDEX idx_rooms_type ON rooms (type);"));

                        long startIndex = System.nanoTime();
                        requestHandler.parse(List.of("SELECT * FROM rooms WHERE type = 'Suite';"));
                        long indexTime = (System.nanoTime() - startIndex) / 1_000_000;

                        String report = String.format("HOTEL BENCHMARK REPORT:\n- Full-Scan (No Index): %d ms\n- Indexed Search (B-Tree): %d ms\nSpeedup: %.1fx faster!",
                                scanTime, indexTime, (double) Math.max(1, scanTime) / Math.max(1, indexTime));

                        uiState.addCmd("\\bench", new QueryResult(true, report, null, null, scanTime + indexTime));
                    } catch (Exception e) {
                        uiState.addCmd("\\bench", new QueryResult(false, "Benchmark error: " + e.getMessage(), null, null, 0));
                    }
                }),
                new UiState.PaletteCommand("SQL syntax reference", "\\help", () -> {
                    String helpText = """
                        SQL CHEAT SHEET:
                        - SELECT * FROM rooms WHERE price < 5000;
                        - SELECT full_name, room_id FROM guests WHERE is_paid = FALSE;
                        - UPDATE rooms SET is_occupied = TRUE WHERE number = 201;
                        - INSERT INTO guests VALUES (3, 'Олег Смирнов', 3, '2023-10-12', '2023-10-15', TRUE);
                        - CREATE INDEX idx_guests_name ON guests (full_name);
                        - DROP TABLE guests;
                        """;
                    uiState.addCmd("\\help", new QueryResult(true, helpText, null, null, 0));
                }),

                new UiState.PaletteCommand("Clear screen", "ctrl+l", () -> {
                    uiState.clear();
                }),

                new UiState.PaletteCommand("Save & Exit", "ctrl+q", () -> {
                    try {
                        storageService.save(db);
                    } catch (Exception ignored) {}
                    uiState.exit();
                })
        );
    }
}
