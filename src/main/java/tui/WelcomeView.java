package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Layout;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.util.List;

public class WelcomeView {
    private final Rect rect;
    private final AutoSuggestionEngine suggestionEngine;
    private final SqlHighlighter highlighter;
    private final List<UiState.PaletteCommand> paletteCommandList;

    public WelcomeView(Rect rect, List<UiState.PaletteCommand> paletteCommandList) {
        this.paletteCommandList = paletteCommandList;
        this.suggestionEngine = new AutoSuggestionEngine();
        this.highlighter = new SqlHighlighter();
        this.rect = rect;
    }

    public void render (Rect rect, Buffer buffer, UiState uiState) {
        Layout layout = Layout.vertical()
                .constraints(
                        Constraint.fill(),
                        Constraint.length(5),
                        Constraint.length(1),
                        Constraint.length(3),
                        Constraint.length(1),
                        Constraint.fill()
                );
        List<Rect> frames = layout.split(rect);
        Layout layout1 = Layout.horizontal()
                .constraints(
                        Constraint.fill(),
                        Constraint.length(64),
                        Constraint.fill()
                );
        List<Rect> frames1 = layout1.split(frames.get(1));
        Rect centerRect = frames1.get(1);
        Paragraph logo = Paragraph.builder().text("Добро пожаловать в Java-DB!").foreground(Color.RED).centered().build();
        logo.render(centerRect, buffer);
    }
}