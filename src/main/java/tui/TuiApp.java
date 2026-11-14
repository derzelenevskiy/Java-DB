package tui;

import db.Database;
import dev.tamboui.terminal.*;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.tui.event.KeyCode;
import model.QueryResult;
import storage.FileStorageService;

import dev.tamboui.tui.event.KeyEvent;
import java.io.IOException;
import java.util.List;

public class TuiApp {
    private Database db;
    private FileStorageService storageService;
    private final RequestHandler requestHandler;
    private UiState uiState;
    private MainView mainView;

    public TuiApp(Database db, FileStorageService storageService, RequestHandler requestHandler) {
        this.db = db;
        this.storageService = storageService;
        this.requestHandler = requestHandler;
        this.mainView = new MainView();
        List<UiState.PaletteCommand> paletteCommandList = List.of(
                new UiState.PaletteCommand("Show tables", "\\dt", () -> {
                    try {
                        QueryResult result = requestHandler.parse("SELECT * FROM ...");
                        uiState.addCmd("SELECT * FROM ...", result);
                        storageService.save(db);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
        );

        this.uiState = new UiState(paletteCommandList);
    }
    public void run() throws IOException {
        try(TuiRunner runner = TuiRunner.create()) {
            while (uiState.isRunning()) {
                runner.draw(frame -> mainView.render(frame.area(), frame, uiState));

                KeyEvent event = (KeyEvent) runner.pollEvent();
                if (event == null) {
                    continue;
                }
                if (event.code() == KeyCode.ENTER && event.modifiers().ctrl()) {
                    String sql = uiState.getInputState().text();
                    if (!sql.isBlank()) {
                        QueryResult result = requestHandler.parse(sql);
                        uiState.addCmd(sql, result);
                        storageService.save(db);
                    }
                } else {
                    uiState.onKeyEvent(event);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            storageService.save(db);
        }
    }


}
