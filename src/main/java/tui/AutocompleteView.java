package tui;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.*;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import dev.tamboui.widgets.Clear;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.block.Title;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.util.List;

public class AutocompleteView {

    private static final int MAX_VISIBLE_ROWS = 8;

    public void render(Rect rect, Frame frame, UiState uiState) {
        Buffer buffer = frame.buffer();

        List<String> suggestions = uiState.getSuggestions();
        if (suggestions == null || suggestions.isEmpty()) {
            return;
        }
        int actualRows = Math.min(suggestions.size(), MAX_VISIBLE_ROWS);
        int modalHeight = actualRows + 4;

        Layout verticalLayout = Layout.vertical().constraints(
                Constraint.fill(),
                Constraint.length(modalHeight),
                Constraint.fill()
        );
        Layout horizontalLayout = Layout.horizontal().constraints(
                Constraint.fill(),
                Constraint.length(50),
                Constraint.fill()
        );
        Rect modalRect = horizontalLayout.split(verticalLayout.split(rect).get(1)).get(1);

        Clear.INSTANCE.render(modalRect, buffer);
        Block block = Block.builder()
                .title(Title.from(" Suggestions ").alignment(Alignment.LEFT))
                .title(Title.from("esc ").alignment(Alignment.RIGHT))
                .borders(Borders.ALL)
                .borderType(BorderType.ROUNDED)
                .style(Style.EMPTY.fg(Color.GRAY))
                .build();
        block.render(modalRect, buffer);

        Rect innerRect = block.inner(modalRect);
        Rect listRect = new Rect(
                innerRect.x() + 2,
                innerRect.y() + 1,
                innerRect.width() - 4,
                innerRect.height() - 2
        );

        int selectedIndex = uiState.getSelectedSuggestionIndex();

        int scrollOffset = Math.max(0, selectedIndex - actualRows + 1);
        int endIndex = Math.min(scrollOffset + actualRows, suggestions.size());

        for (int i = scrollOffset; i < endIndex; i++) {
            String suggestion = suggestions.get(i);
            boolean isSelected = (i == selectedIndex);

            Style rowStyle = isSelected
                    ? Style.EMPTY.bg(Color.hex("#282b30")).fg(Color.WHITE).bold()
                    : Style.EMPTY.fg(Color.WHITE);

            int visualRow = i - scrollOffset;
            Rect rowRect = new Rect(listRect.x(), listRect.y() + visualRow, listRect.width(), 1);

            buffer.setStyle(rowRect, rowStyle);

            Paragraph.builder()
                    .text(suggestion)
                    .style(rowStyle)
                    .build()
                    .render(rowRect, buffer);
        }
    }
}