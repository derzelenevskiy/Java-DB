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
        List<UiState.PaletteCommand> commands = uiState.getPaletteCommandList();
        Layout verticalLayout = Layout.vertical().constraints(
                Constraint.fill(),
                Constraint.length(14),
                Constraint.fill()
        );
        Layout horizontalLayout = Layout.horizontal().constraints(
                Constraint.fill(),
                Constraint.length(60),
                Constraint.fill()
        );
        Rect modalRect = horizontalLayout.split(verticalLayout.split(rect).get(1)).get(1);
        Clear.INSTANCE.render(modalRect, buffer);
        Block block = Block.builder().title(Title.from(" Commands ").alignment(Alignment.LEFT)).title(Title.from("esc ").alignment(Alignment.RIGHT)).borders(Borders.ALL).borderType(BorderType.ROUNDED).style(Style.EMPTY.fg(Color.GRAY)).build();
        block.render(modalRect, buffer);
        Rect innerRect = block.inner(modalRect);

        Rect listRect = new Rect(
                innerRect.x() + 2,
                innerRect.y() + 1,
                innerRect.width() - 4,
                innerRect.height() - 2
        );

        for (int i = 0; i < commands.size(); i++) {
            UiState.PaletteCommand cmd = commands.get(i);
            boolean isSelected = (i == uiState.getSelectedPaletteIndex());

            Style rowStyle = isSelected ? Style.EMPTY.bg(Color.hex("#FAB283")).fg(Color.BLACK).bold() : Style.EMPTY.fg(Color.WHITE);

            Rect rowRect = new Rect(listRect.x(), listRect.y() + i, listRect.width(), 1);

            buffer.setStyle(rowRect, rowStyle);

            Layout rowLayout = Layout.horizontal().constraints(
                    Constraint.fill(),
                    Constraint.length(15)
            );
            List<Rect> rowFrames = rowLayout.split(rowRect);

            Paragraph.builder()
                    .text(cmd.title())
                    .style(rowStyle)
                    .build()
                    .render(rowFrames.get(0), buffer);

            Paragraph.builder()
                    .text(cmd.shortcut())
                    .style(rowStyle)
                    .alignment(Alignment.RIGHT)
                    .build()
                    .render(rowFrames.get(1), buffer);
        }
    }
}