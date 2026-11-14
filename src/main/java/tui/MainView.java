package tui;

import dev.tamboui.layout.Rect;
import dev.tamboui.terminal.Frame;

public class MainView {
    private final ActiveView activeView;
    private final WelcomeView welcomeView;
    private final CommandPaletteView paletteView;
    public MainView() {
        AutoSuggestionEngine suggestionEngine = new AutoSuggestionEngine();
        SqlHighlighter highlighter = new SqlHighlighter();
        this.welcomeView = new WelcomeView(highlighter);
        this.activeView = new ActiveView(highlighter);
        this.paletteView = new CommandPaletteView();
    }
    public void render(Rect rect, Frame frame, UiState uiState) {
        if (uiState.isActiveMode()) {
            activeView.render(rect, frame, uiState);
        } else {
            welcomeView.render(rect, frame, uiState);
        }
        if (uiState.isPaletteOpen()) {
            paletteView.render(rect, frame, uiState);
        }
    }
}
