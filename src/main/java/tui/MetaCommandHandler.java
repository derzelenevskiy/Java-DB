package tui;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class MetaCommandHandler {
    Terminal terminal;
    public MetaCommandHandler(Terminal terminal) throws IOException {
        this.terminal = terminal;
    }
    public boolean check(String line) {
        return line.startsWith("\\");
    }
    public void execute(String line) {
        line = line.trim();
        String[] words = line.split(" ");

    }
}
