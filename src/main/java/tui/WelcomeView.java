package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.Alignment;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Layout;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.text.Line;
import dev.tamboui.text.MarkupParser;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.util.List;

public class WelcomeView {
    private final AutoSuggestionEngine suggestionEngine;
    private final SqlHighlighter highlighter;
    private final static Paragraph footer = Paragraph.builder().text(MarkupParser.parse("[#EEEEEE]ctrl+h[/] [#3C3C3C]history[/]  [#EEEEEE]ctrl+p[/] [#3C3C3C]commands[/]")).alignment(Alignment.RIGHT).build();
    private final static Paragraph logo = Paragraph.builder().text("Добро пожаловать в Java-DB!").foreground(Color.RED).centered().build();

    public WelcomeView(AutoSuggestionEngine suggestionEngine, SqlHighlighter highlighter) {
        this.suggestionEngine = suggestionEngine;
        this.highlighter = highlighter;
    }

    public void render (Rect rect, Buffer buffer, UiState uiState) {
        Layout verticalLayout = Layout.vertical()
                .constraints(
                        Constraint.fill(),
                        Constraint.length(5),
                        Constraint.length(1),
                        Constraint.length(3),
                        Constraint.length(1),
                        Constraint.fill()
                );
        Layout horizontalLayout = Layout.horizontal()
                .constraints(
                        Constraint.fill(),
                        Constraint.length(64),
                        Constraint.fill()
                );
        List<Rect> verticalFrames = verticalLayout.split(rect);
        logo.render(verticalFrames.get(1), buffer);
        footer.render(horizontalLayout.split(verticalFrames.get(4)).get(1), buffer);
        Rect centralRect = horizontalLayout.split(verticalFrames.get(3)).get(1);
        Block inputBox = Block.builder().borders(Borders.ALL).borderStyle(Style.EMPTY.fg(Color.DARK_GRAY)).build();
        inputBox.render(centralRect, buffer);
        Rect innerRect = inputBox.inner(centralRect);
        String text = uiState.getInputState().text();
        if (text.isEmpty()) {
            Paragraph hint = Paragraph.builder().text("Ask anything...").foreground(Color.LIGHT_YELLOW).build();
            hint.render(innerRect, buffer);
        } else {
            Line line = Line.from(highlighter.ghostHighlight(text, uiState.getCurrentSuffix()));
            Paragraph paragraph = Paragraph.from(line);
            paragraph.render(innerRect, buffer);
        }
        int x = innerRect.x() + uiState.getInputState().cursorPosition();
        int y = innerRect.y();

    }
}