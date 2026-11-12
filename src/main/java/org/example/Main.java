package org.example;

import tui.TerminalController;

import java.io.IOException;

public class Main {
    public void main(String[] args) throws IOException {
        TerminalController terminalController = new TerminalController();
        terminalController.run();
    }
}
