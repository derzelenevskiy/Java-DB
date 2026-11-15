package tui;

import db.Database;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.tui.event.TickEvent;
import model.QueryResult;
import requestHandler.RequestHandler;
import storage.FileStorageService;

import dev.tamboui.tui.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TuiApp {
    private final Database db;
    private final FileStorageService storageService;
    private final RequestHandler requestHandler;
    private final UiState uiState;
    private final MainView mainView;
    private final DemoDataInitializer demoDataInitializer = new DemoDataInitializer();
    public TuiApp(FileStorageService storageService) {
        this.db = new Database();
        this.storageService = storageService;
        List<UiState.DirectoryCommand> directoryCommandList = new ArrayList<>();
        List<UiState.PaletteCommand> paletteCommandList = new ArrayList<>();
        this.uiState = new UiState(paletteCommandList, directoryCommandList);
        this.requestHandler = new RequestHandler(db);
        paletteCommandList.addAll(createPaletteCommandList());
        this.mainView = new MainView();
    }
    public void run() throws IOException {
        TuiConfig config = TuiConfig.builder()
                .hideCursor(false)
                .build();
        try (TuiRunner runner = TuiRunner.create(config)) {
            while (uiState.isRunning()) {
                runner.draw(frame -> mainView.render(frame.area(), frame, uiState));

                var event = runner.pollEvent();
                if (event == null) {
                    continue;
                }
                switch (event) {
                    case KeyEvent keyEvent -> {
                        if (keyEvent.modifiers().ctrl()) {
                            if (keyEvent.isChar('Q') || keyEvent.isChar('q')) {
                                uiState.getPaletteCommandList().getLast().action().run();
                            } else if (keyEvent.code() == KeyCode.ENTER) {
                                String sql = uiState.getInputState().text().trim();

                                if (!sql.isBlank()) {
                                    if (!sql.startsWith("\\")) {
                                        try {
                                            QueryResult result = requestHandler.parse(sql);
                                            uiState.addCmd(sql, result);
                                            storageService.save(db);
                                        } catch (Exception e) {
                                            QueryResult result = new QueryResult(false, e.getMessage(), null, null, 0);
                                            uiState.addCmd(sql, result);
                                        }
                                    } else {
                                        boolean found = false;
                                        for (UiState.PaletteCommand cmd : uiState.getPaletteCommandList()) {
                                            if (cmd.shortcut().equals(sql)) {
                                                cmd.action().run();
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            uiState.addCmd(sql, new QueryResult(false, "Unknown command", null, null, 0));
                                        }
                                    }
                                }
                            } else if (keyEvent.isChar('S') || keyEvent.isChar('s')) {
                                storageService.save(db);
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
        assert uiState != null;
        return List.of(
                new UiState.PaletteCommand("Show tables", "\\dt", () -> {
                    try {
                        List<String> tableNames = List.of(db.getTableNames());
                        if (tableNames.isEmpty()) {
                            uiState.addCmd("\\dt", new QueryResult(true, "No tables found.", null, null, 0));
                            return;
                        }
                        String[] headers = {"Table Name"};
                        Object[][] data = new Object[tableNames.size()][1];
                        for (int i = 0; i < tableNames.size(); i++) {
                            data[i][0] = tableNames.get(i);
                        }
                        uiState.addCmd("\\dt", new QueryResult(true, "", headers, data, 0));

                    } catch (Exception e) {
                        uiState.addCmd("\\dt", new QueryResult(false, "Error getting tables: " + e.getMessage(), null, null, 0));
                    }
                }),
                new UiState.PaletteCommand("Show indexes", "\\di", () -> {
                    try {
                        String[] headers = {"Index Name", "Table"};
                        Object[][] data = {};

                        uiState.addCmd("\\di", new QueryResult(true, "Indexes listed successfully", headers, data, 0));
                    } catch (Exception e) {
                        uiState.addCmd("\\di", new QueryResult(false, "Error getting indexes: " + e.getMessage(), null, null, 0));
                    }
                }),
                new UiState.PaletteCommand("Working directory", "ctrl+d", uiState::openDirectory),
                new UiState.PaletteCommand("Save database to disk", "ctrl+s", () -> {
                    try {
                        storageService.save(db);
                        uiState.addCmd("\\save", new QueryResult(true, "Database successfully saved to disk", null, null, 0));
                    } catch (Exception e) {
                        uiState.addCmd("\\save", new QueryResult(false, "Failed to save database:" + e.getMessage(), null, null, 0));
                    }
                }),
                new UiState.PaletteCommand("Load demo sample data", "\\demo", () -> {
                    QueryResult res = demoDataInitializer.initializeDemo(db, requestHandler, storageService);
                    uiState.addCmd("\\demo", res);
                    uiState.getInputState().clear();
                }),
                new UiState.PaletteCommand("Clear demo sample data", "\\demoсlear", () -> {
                    QueryResult res = demoDataInitializer.clean(db, requestHandler, storageService);
                    uiState.addCmd("\\democlear", res);
                    uiState.getInputState().clear();
                }),
                new UiState.PaletteCommand("Run benchmark: Room Type Search", "\\bench", uiState::clear),
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

                new UiState.PaletteCommand("Clear screen", "ctrl+l", uiState::clear),

                new UiState.PaletteCommand("Save & Exit", "ctrl+q", () -> {
                    try {
                        storageService.save(db);
                    } catch (Exception ignored) {}
                    uiState.exit();
                })
        );
    }
}
