package tui;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class TerminalController {
    private final Terminal terminal;
    private final LineReader reader;
    private volatile boolean isRunning;
    public TerminalController() throws IOException {
        this.terminal = TerminalBuilder.builder().system(true).provider("ffm").build();
        this.reader = LineReaderBuilder.builder().terminal(terminal).history(new DefaultHistory()).variable(LineReader.HISTORY_FILE, Path.of("history.txt")).highlighter(new SqlHighlighter()).completer(new SqlCompleter()).build();
        ResultPresenter presenter = new ResultPresenter();
        MetaCommandHandler metaCommandHandler = new MetaCommandHandler();
    }
    public void run() throws IOException {
        terminal.writer().println("Welcome to JavaDB! Type \\help for help, \\exit to quit");
        terminal.flush();
        terminal.writer().println("Type commands and use up/down arrows to navigate history");
        terminal.flush();
        isRunning = true;
        while (isRunning) {
            try {
                String line = reader.readLine("db> ");
                if (line.trim().isEmpty()) {
                    continue;
                }

            } catch (UserInterruptException e) {
                terminal.writer().println("Input is canceled");
                terminal.flush();
            } catch (EndOfFileException e) {
                isRunning = false;
            }
        }
        close();
    }
    public void close() throws IOException {
        terminal.writer().println("Goodbye");
        terminal.flush();
        terminal.close();
    }
    public Terminal getTerminal(Terminal terminal) {
        return terminal;
    }
}
