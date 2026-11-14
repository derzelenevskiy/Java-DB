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
import dev.tamboui.text.Line;
import dev.tamboui.text.MarkupParser;
import dev.tamboui.text.Text;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.util.ArrayList;
import java.util.List;

public class ActiveView {
    private final AutoSuggestionEngine suggestionEngine;
    private final SqlHighlighter highlighter;
    private final static Paragraph footer = Paragraph.builder().text(MarkupParser.parse("[#EEEEEE]ctrl+h[/] [#3C3C3C]history[/]  [#EEEEEE]ctrl+p[/] [#3C3C3C]commands[/]")).alignment(Alignment.RIGHT).build();
    private final static Paragraph session = Paragraph.from("Database 1");
    public ActiveView(AutoSuggestionEngine suggestionEngine, SqlHighlighter highlighter) {
        this.suggestionEngine = suggestionEngine;
        this.highlighter = highlighter;
    }

    public void render(Rect rect, Buffer buffer, UiState uiState) {
        Layout verticalLayout = Layout.vertical()
                .constraints(
                        Constraint.length(1),
                        Constraint.fill(),
                        Constraint.length(3),
                        Constraint.length(1)
                );
        List<Rect> frames = verticalLayout.split(rect);
        footer.render(frames.get(3), buffer);
        session.render(frames.get(0), buffer);

        Rect feedRect = frames.get(1);
        List<Line> lines = new ArrayList<>();
        UiState.HistoryEntry lastRecord = uiState.getHistory().getLast();
        lines.add(Line.from(highlighter.highlight("> " + lastRecord.query())));
        lines.add(Line.from(""));
        if(!lastRecord.queryResult().isSuccess()) {
            Line line = Line.styled(lastRecord.queryResult().errorMessage(), Style.EMPTY.fg(Color.RED));
            lines.add(line);
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
                        lines.add(Line.from(str));
                    }
                }
                String time = s;
                lines.add(Line.from(""));
                lines.add(Line.styled("Execution time:" + time + "ms", Style.EMPTY.fg(Color.GRAY)));
            } else {
                lines.add(Line.styled("Query OK", Style.EMPTY.fg(Color.GREEN)));
                String time = s;
                lines.add(Line.from(""));
                lines.add(Line.styled("Execution time:" + time + "ms", Style.EMPTY.fg(Color.GRAY)));
            }
        }
        Paragraph feedParagraph = Paragraph.builder().text(Text.from(lines)).overflow(Overflow.WRAP_WORD).build();
        feedParagraph.render(feedRect, buffer);

        Block inputBox = Block.builder().borders(Borders.ALL).borderStyle(Style.EMPTY.fg(Color.DARK_GRAY)).build();
        inputBox.render(frames.get(2), buffer);
        Rect innerRect = inputBox.inner(frames.get(2));
        String text = uiState.getInputState().text();
        if (text.isEmpty()) {
            Paragraph hint = Paragraph.builder().text("Ask anything...").foreground(Color.LIGHT_YELLOW).build();
            hint.render(innerRect, buffer);
        } else {
            String suffix = suggestionEngine.getSuggestionSuffix(text, uiState.getHistory());
            Line line = Line.from(highlighter.ghostHighlight(text, suffix));
            Paragraph inputParagraph = Paragraph.from(line);
            inputParagraph.render(innerRect, buffer);
        }
    }
}
