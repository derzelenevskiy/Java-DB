package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;

public class MainView {
    private final ActiveView activeView;
    private final WelcomeView welcomeView;
    private final CommandPaletteView paletteView;
    private final AutocompleteView autocompleteView;
    private final DirectoryView directoryView;

    public MainView() {
        this.autocompleteView = new AutocompleteView();
        this.directoryView = new DirectoryView();
        SqlHighlighter highlighter = new SqlHighlighter();
        this.welcomeView = new WelcomeView(highlighter);
        this.activeView = new ActiveView(highlighter);
        this.paletteView = new CommandPaletteView();
    }
    public void render(Rect rect, Frame frame, UiState uiState) {
        frame.buffer().setStyle(frame.area(), Style.EMPTY.bg(Color.hex("#000000")));
        if (uiState.isActiveMode()) {
            activeView.render(rect, frame, uiState);
        } else {
            welcomeView.render(rect, frame, uiState);
        }
        if (uiState.isPaletteOpen()) {
            paletteView.render(rect, frame, uiState);
        } else if (uiState.isAutocompleteOpen()) {
            autocompleteView.render(rect, frame, uiState);
        } else if (uiState.isDirectoryOpen()) {
            directoryView.render(rect, frame, uiState);
        }
    }
}
