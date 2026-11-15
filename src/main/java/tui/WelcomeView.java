package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.*;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import dev.tamboui.text.Line;
import dev.tamboui.text.MarkupParser;
import dev.tamboui.text.Text;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.paragraph.Paragraph;
import dev.tamboui.widgets.wavetext.WaveText;
import dev.tamboui.widgets.wavetext.WaveTextState;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class WelcomeView {
    private final SqlHighlighter highlighter;
    private final static Paragraph footer = Paragraph.builder().text(MarkupParser.parse("[#EEEEEE]ctrl+d[/] [#3C3C3C]directory[/]  [#EEEEEE]ctrl+h[/] [#3C3C3C]history[/]  [#EEEEEE]ctrl+p[/] [#3C3C3C]commands[/]")).alignment(Alignment.RIGHT).build();
    private final WaveTextState logoWaveState = new WaveTextState();

    public WelcomeView(SqlHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void render (Rect area, Frame frame, UiState uiState) {
        String[] logoLines = {
                "  ██╗ █████╗ ██╗   ██╗ █████╗     ██████╗ ██████╗ ",
                " ██╔╝██╔══██╗██║   ██║██╔══██╗    ██╔══██╗██╔══██╗",
                "██╔╝ ███████║██║   ██║███████║    ██║  ██║██████╔╝",
                "╚██╗ ██╔══██║╚██╗ ██╔╝██╔══██║    ██║  ██║██╔══██╗",
                " ╚██╗██║  ██║ ╚████╔╝ ██║  ██║    ██████╔╝██████╔╝",
                "  ╚═╝╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝    ╚═════╝ ╚═════╝ "
        };

        Rect rect = area.inner(new Margin(1, 2, 1, 2));
        Buffer buffer = frame.buffer();

        String text = uiState.getInputState().text();
        int rawCol = uiState.getInputState().cursorCol();
        int rawRow = uiState.getInputState().cursorRow();

        int maxWidth = 62;

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
                        Constraint.fill(),
                        Constraint.length(6),
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
        Layout logoHorizontalLayout = Layout.horizontal()
                .constraints(
                        Constraint.fill(),
                        Constraint.length(logoLines[1].length()),
                        Constraint.fill()
                );
        List<Rect> verticalFrames = verticalLayout.split(rect);

        WaveTextState waveState = uiState.getLogoWaveState();
        Rect logoRect = logoHorizontalLayout.split(verticalFrames.get(1)).get(1);

        for (int i = 0; i < logoLines.length; ++i) {
            WaveText lineWave = WaveText.builder()
                    .text(logoLines[i])
                    .color(Color.CYAN)
                    .inverted(true)
                    .peakWidth(4)
                    .dimFactor(0.4)
                    .speed(2.5)
                    .build();
            Rect lineRect = new Rect(logoRect.x(), logoRect.y() + i, logoRect.width(), 1);
            lineWave.render(lineRect, frame.buffer(), waveState);
        }

        footer.render(horizontalLayout.split(verticalFrames.get(4)).get(1), buffer);
        Rect centralRect = horizontalLayout.split(verticalFrames.get(3)).get(1);
        Block inputBox = Block.builder().borders(Borders.ALL).borderType(BorderType.ROUNDED).borderStyle(Style.EMPTY.fg(Color.hex("#1E1E1E"))).build();
        inputBox.render(centralRect, buffer);
        Rect innerRect = inputBox.inner(centralRect);

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