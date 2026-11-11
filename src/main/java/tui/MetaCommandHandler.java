package tui;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class MetaCommandHandler {
    Terminal terminal;
    public MetaCommandHandler() throws IOException {
        this.terminal = TerminalBuilder.builder().system(true).provider("ffm").build();
    }
    public boolean check(String line) {
        return line.startsWith("\\");
    }
    public void execute(String line) {
        line = line.trim();
        String[] words = line.split(" ");

    }
}
