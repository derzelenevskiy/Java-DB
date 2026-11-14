package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.*;
import dev.tamboui.style.Color;
import dev.tamboui.style.Overflow;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import dev.tamboui.text.Line;
import dev.tamboui.text.MarkupParser;
import dev.tamboui.text.Text;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class WelcomeView {
    private final SqlHighlighter highlighter;
    private final static Paragraph footer = Paragraph.builder().text(MarkupParser.parse("[#EEEEEE]ctrl+h[/] [#3C3C3C]history[/]  [#EEEEEE]ctrl+p[/] [#3C3C3C]commands[/]")).alignment(Alignment.RIGHT).build();
    private final static Paragraph logo = Paragraph.builder().text("Добро пожаловать в Java-DB!").foreground(Color.RED).centered().build();

    public WelcomeView(SqlHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void render (Rect rect, Frame frame, UiState uiState) {
        Buffer buffer = frame.buffer();
        int totalLines = uiState.getLineCount() > 0 ? uiState.getLineCount() : 1;
        totalLines = totalLines < 6 ? totalLines : 5;
        Layout verticalLayout = Layout.vertical()
                .constraints(
                        Constraint.fill(),
                        Constraint.length(5),
                        Constraint.length(1),
                        Constraint.length(totalLines + 2),
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
        int scrollOffset = uiState.getCursorCol() < 5 ? 0 : uiState.getCursorRow() - 5 + 1;
        if (text.isEmpty()) {
            Paragraph hint = Paragraph.builder().text("Ask anything...").foreground(Color.LIGHT_YELLOW).build();
            hint.render(innerRect, buffer);
        } else {
            List<Line> lines = new ArrayList<>();
            String[] textArr = text.split("\n", -1);
            for (int i = scrollOffset; i < min(textArr.length, scrollOffset + 5); ++i) {
                lines.add(Line.from(highlighter.highlight(textArr[i])));
                if (i == textArr.length - 1) {
                    lines.add(Line.from(highlighter.ghostHighlight(textArr[i], uiState.getCurrentSuffix())));
                }
            }
            Paragraph paragraph = Paragraph.builder().text(Text.from(lines)).overflow(Overflow.WRAP_WORD).build();
            paragraph.render(innerRect, buffer);
        }
        int x = innerRect.x() + uiState.getInputState().cursorCol();
        int y = innerRect.y() + uiState.getInputState().cursorRow() - scrollOffset;
        frame.setCursorPosition(x, y);
    }
}