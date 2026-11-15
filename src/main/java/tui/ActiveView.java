package tui;

import com.jakewharton.fliptables.FlipTable;
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
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class ActiveView {
    private final SqlHighlighter highlighter;
    private final static Paragraph footer = Paragraph.builder().text(MarkupParser.parse("[#EEEEEE]ctrl+d[/] [#3C3C3C]directory[/]  [#EEEEEE]ctrl+h[/] [#3C3C3C]history[/]  [#EEEEEE]ctrl+p[/] [#3C3C3C]commands[/]")).alignment(Alignment.RIGHT).build();
    private final static Paragraph session = Paragraph.from("Database 1");
    public ActiveView(SqlHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void render(Rect area, Frame frame, UiState uiState) {
        Rect rect = area.inner(new Margin(1, 2, 1, 2));
        if (uiState.getHistory().isEmpty()) {
            return;
        }
        Buffer buffer = frame.buffer();
        String text = uiState.getInputState().text();
        int rawCol = uiState.getInputState().cursorCol();
        int rawRow = uiState.getInputState().cursorRow();

        int maxWidth = Math.max(1, rect.width() - 2);

        List<String> visualLines = new ArrayList<>();
        int cursorVisualRow = 0;
        int cursorVisualCol = 0;

        String[] logicalLines = text.split("\n", -1);
        for (int i = 0; i < logicalLines.length; ++i) {
            String line = logicalLines[i];
            int start = 0;
            int lineCursor = (i == rawRow) ? rawCol : -1;

            if (line.isEmpty()) {
                visualLines.add("");
                if (lineCursor == 0) {
                    cursorVisualRow = visualLines.size() - 1;
                    cursorVisualCol = 0;
                }
                continue;
            }

            while (start < line.length()) {
                if (line.length() - start <= maxWidth) {
                    visualLines.add(line.substring(start));
                    if (lineCursor >= start) {
                        cursorVisualRow = visualLines.size() - 1;
                        cursorVisualCol = lineCursor - start;
                    }
                    break;
                }

                int breakAt = line.lastIndexOf(' ', start + maxWidth);
                if (breakAt <= start) {
                    breakAt = start + maxWidth;
                }

                visualLines.add(line.substring(start, breakAt));

                if (lineCursor >= start && lineCursor <= breakAt) {
                    cursorVisualRow = visualLines.size() - 1;
                    cursorVisualCol = lineCursor - start;
                }

                start = breakAt;
                if (start < line.length() && line.charAt(start) == ' ') {
                    if (lineCursor == start) {
                        cursorVisualRow = visualLines.size();
                        cursorVisualCol = 0;
                    }
                    start++;
                }
            }
        }

        int totalLines = Math.clamp(visualLines.size(), 1, 5);

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
        feedLines.add(Line.from(""));
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
                    String[][] stringData = new String[data.length][headers.length];

                    for (int i = 0; i < data.length; ++i) {
                        for (int j = 0; j < data[i].length; ++j) {
                            stringData[i][j] = String.valueOf(data[i][j]);
                        }
                    }
                    String table = FlipTable.of(headers, stringData);
                    String[] tableArr = table.split("\n");
                    for (String str : tableArr) {
                        feedLines.add(Line.from(str));
                    }
                } else {
                    feedLines.add(Line.styled("Empty set (0 rows)", Style.EMPTY.fg(Color.hex("#3C3C3C"))));
                }
            } else {
                feedLines.add(Line.styled("Query OK", Style.EMPTY.fg(Color.GREEN)));
            }
            feedLines.add(Line.from(""));
            feedLines.add(Line.styled("Execution time: " + s + " ms", Style.EMPTY.fg(Color.hex("#3C3C3C"))));
        }
        Paragraph feedParagraph = Paragraph.builder().text(Text.from(feedLines)).overflow(Overflow.CLIP).build();
        feedParagraph.render(feedRect, buffer);

        Rect inputBoxRect = frames.get(2);
        Block inputBox = Block.builder().borders(Borders.ALL).borderType(BorderType.ROUNDED).borderStyle(Style.EMPTY.fg(Color.hex("#1E1E1E"))).build();
        inputBox.render(inputBoxRect, buffer);
        Rect innerRect = inputBox.inner(inputBoxRect);

        int scrollOffset = Math.max(0, cursorVisualRow - totalLines + 1);

        if (text.isEmpty()) {
            Paragraph hint = Paragraph.builder().text("Enter command... (ctrl+enter to execute)").foreground(Color.hex("#5D5D5D")).build();
            hint.render(innerRect, buffer);
        } else {
            List<Line> lines = new ArrayList<>();
            for (int i = scrollOffset; i < min(visualLines.size(), scrollOffset + totalLines); ++i) {
                String vLine = visualLines.get(i);
                if (i == visualLines.size() - 1) {
                    lines.add(Line.from(highlighter.ghostHighlight(vLine, uiState.getCurrentSuffix())));
                } else {
                    lines.add(Line.from(highlighter.highlight(vLine)));
                }
            }
            Paragraph paragraph = Paragraph.builder().text(Text.from(lines)).build();
            paragraph.render(innerRect, buffer);
        }

        int x = innerRect.x() + cursorVisualCol;
        int y = innerRect.y() + (cursorVisualRow - scrollOffset);
        frame.setCursorPosition(x, y);
    }
}
