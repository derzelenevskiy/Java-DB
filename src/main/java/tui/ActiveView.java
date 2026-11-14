package tui;

import com.jakewharton.fliptables.FlipTable;
import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.Alignment;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Layout;
import dev.tamboui.layout.Rect;
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

public class ActiveView {
    private final SqlHighlighter highlighter;
    private final static Paragraph footer = Paragraph.builder().text(MarkupParser.parse("[#EEEEEE]ctrl+h[/] [#3C3C3C]history[/]  [#EEEEEE]ctrl+p[/] [#3C3C3C]commands[/]")).alignment(Alignment.RIGHT).build();
    private final static Paragraph session = Paragraph.from("Database 1");
    public ActiveView(SqlHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void render(Rect rect, Frame frame, UiState uiState) {
        Buffer buffer = frame.buffer();
        int totalLines = uiState.getLineCount() > 0 ? uiState.getLineCount() : 1;
        totalLines = totalLines < 6 ? totalLines : 5;
        Layout verticalLayout = Layout.vertical()
                .constraints(
                        Constraint.length(1),
                        Constraint.fill(),
                        Constraint.length(totalLines + 2),
                        Constraint.length(1)
                );
        List<Rect> frames = verticalLayout.split(rect);
        footer.render(frames.get(3), buffer);
        session.render(frames.get(0), buffer);

        Rect feedRect = frames.get(1);
        List<Line> feedLines = new ArrayList<>();
        UiState.HistoryEntry lastRecord = uiState.getHistory().getLast();
        feedLines.add(Line.from(highlighter.highlight("> " + lastRecord.query())));
        feedLines.add(Line.from(""));
        if(!lastRecord.queryResult().isSuccess()) {
            Line line = Line.styled(lastRecord.queryResult().errorMessage(), Style.EMPTY.fg(Color.RED));
            feedLines.add(line);
        } else {
            String s = String.valueOf(lastRecord.queryResult().execution());
            if (lastRecord.queryResult().headers() != null && lastRecord.queryResult().headers().length > 0) {
                String[] headers = lastRecord.queryResult().headers();
                Object[][] data = lastRecord.queryResult().data();
                if (data.length > 0) {
                    String[][] stringData = new String[data.length][data[0].length];

                    for (int i = 0; i < data.length; i++) {
                        for (int j = 0; j < data[i].length; j++) {
                            stringData[i][j] = String.valueOf(data[i][j]);
                        }
                    }
                    String table = FlipTable.of(headers, stringData);
                    String[] tableArr = table.split("\n");
                    for (String str : tableArr) {
                        feedLines.add(Line.from(str));
                    }
                }
                feedLines.add(Line.from(""));
                feedLines.add(Line.styled("Execution time:" + s + "ms", Style.EMPTY.fg(Color.GRAY)));
            } else {
                feedLines.add(Line.styled("Query OK", Style.EMPTY.fg(Color.GREEN)));
                feedLines.add(Line.from(""));
                feedLines.add(Line.styled("Execution time:" + s + "ms", Style.EMPTY.fg(Color.GRAY)));
            }
        }
        Paragraph feedParagraph = Paragraph.builder().text(Text.from(feedLines)).overflow(Overflow.WRAP_WORD).build();
        feedParagraph.render(feedRect, buffer);

        Block inputBox = Block.builder().borders(Borders.ALL).borderStyle(Style.EMPTY.fg(Color.DARK_GRAY)).build();
        inputBox.render(frames.get(2), buffer);
        Rect innerRect = inputBox.inner(frames.get(2));
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
