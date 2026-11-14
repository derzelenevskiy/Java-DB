package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.*;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import dev.tamboui.text.Line;
import dev.tamboui.text.Text;
import dev.tamboui.widgets.Clear;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.style.Color;
import dev.tamboui.widgets.block.Title;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.util.ArrayList;
import java.util.List;

public class CommandPaletteView {

    public void render(Rect rect, Frame frame, UiState uiState) {
        Buffer buffer = frame.buffer();
        Layout verticalLayout = Layout.vertical().constraints(
                Constraint.fill(),
                Constraint.length(10),
                Constraint.fill()
        );
        Layout horizontalLayout = Layout.horizontal().constraints(
                Constraint.fill(),
                Constraint.length(50),
                Constraint.fill()
        );
        Rect modalRect = horizontalLayout.split(verticalLayout.split(rect).get(1)).get(1);
        Clear.INSTANCE.render(modalRect, buffer);
        Block block = Block.builder().title(Title.from(" Commands ").alignment(Alignment.LEFT)).title(Title.from("esc").alignment(Alignment.RIGHT)).borders(Borders.ALL).borderType(BorderType.ROUNDED).style(Style.EMPTY.fg(Color.GRAY)).build();
        block.render(modalRect, buffer);
        Rect innerRect = block.inner(modalRect);
        List<Line> lines = new ArrayList<>();
        int index = 0;
        for (UiState.PaletteCommand i : uiState.getPaletteCommandList()) {
            boolean isSelected = (index == uiState.getSelectedPaletteIndex());
            String text = i.title() + "   " + i.shortcut();
            if (isSelected) {
                Line line = Line.styled(text, Style.EMPTY.bg(Color.GREEN).fg(Color.LIGHT_YELLOW).bold());
                lines.add(line);
            } else {
                Line line = Line.from(text);
                lines.add(line);
            }
            ++index;
        }
        Paragraph menuParagraph = Paragraph.builder().text(Text.from(lines)).build();
        menuParagraph.render(innerRect, buffer);
    }
}