package org.example;

import db.Database;
import requestHandler.RequestHandler;
import requestHandler.SqlParser;
import storage.FileStorageService;
import tui.TuiApp;

import java.io.IOException;

public class Main {
    TuiApp app;
    Database db;
    public void main(String[] args) throws IOException {
        this.app = new TuiApp(new Database(), new FileStorageService(), new RequestHandler(db));
        app.run();
    }
}
