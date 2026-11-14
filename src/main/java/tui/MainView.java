package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.Rect;

public class MainView {
    private final ActiveView activeView;
    private final WelcomeView welcomeView;
    private final CommandPaletteView paletteView;
    public MainView() {
        AutoSuggestionEngine suggestionEngine = new AutoSuggestionEngine();
        SqlHighlighter highlighter = new SqlHighlighter();
        this.welcomeView = new WelcomeView(suggestionEngine, highlighter);
        this.activeView = new ActiveView(suggestionEngine, highlighter);
        this.paletteView = new CommandPaletteView();
    }
    public void render(Rect rect, Buffer buffer, UiState uiState) {
        if (uiState.isActiveMode()) {
            activeView.render(rect, buffer, uiState);
        } else {
            welcomeView.render(rect, buffer, uiState);
        }
        if (uiState.isPaletteOpen()) {
            paletteView.render(rect, buffer, uiState);
        }
    }
}
