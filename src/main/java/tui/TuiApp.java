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
    public TuiApp(Database db, FileStorageService storageService, RequestHandler requestHandler) {
        this.db = db;
        this.storageService = storageService;
        List<UiState.DirectoryCommand> directoryCommandList = new ArrayList<>();
        List<UiState.PaletteCommand> paletteCommandList = new ArrayList<>();
        this.uiState = new UiState(paletteCommandList, directoryCommandList);
        this.requestHandler = requestHandler;
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
                        if (keyEvent.code() == KeyCode.ENTER && keyEvent.modifiers().ctrl()) {
                            String sql = uiState.getInputState().text();

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
                                    switch (sql) {
                                        case "\\dt" -> {
                                            uiState.getPaletteCommandList().getFirst().action().run();
                                        }
                                        case "\\di" -> {
                                            uiState.getPaletteCommandList().get(1).action().run();
                                        }
                                        case "\\save" -> {
                                            uiState.getPaletteCommandList().get(3).action().run();
                                        }
                                        case "\\demo" -> {
                                            uiState.getPaletteCommandList().get(4).action().run();
                                        }
                                        case "\\democlear" -> {
                                            uiState.getPaletteCommandList().get(5).action().run();
                                        }
                                        case "\\bench" -> {
                                            uiState.getPaletteCommandList().get(6).action().run();
                                        }
                                        case "\\help" -> {
                                            uiState.getPaletteCommandList().get(7).action().run();
                                        }
                                        default -> {
                                            uiState.addCmd(sql, new QueryResult(false, "Unknown command" ,null, null, 0));
                                        }
                                    }
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
        assert uiState != null;
        return List.of(
                new UiState.PaletteCommand("Show tables", "\\dt", () -> {
                    QueryResult res = requestHandler.parse("SELECT * FROM db.getTableNames()");
                    uiState.addCmd("\\dt", res);
                }),
                new UiState.PaletteCommand("Show indexes", "\\di", () -> {
                    QueryResult res = requestHandler.parse("SELECT * FROM db.getTableIndexes()");
                    uiState.addCmd("\\di", res);
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
                new UiState.PaletteCommand("Run benchmark: Room Type Search", "\\bench", () -> {
                    uiState.clear();
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
