package tui;

import dev.tamboui.tui.event.KeyEvent;
import dev.tamboui.widgets.input.TextAreaState;
import model.QueryResult;

import java.util.ArrayList;
import java.util.List;

public class UiState {
    public UiState(List<PaletteCommand> paletteCommandList) {
        this.inputState = new TextAreaState();
        this.suggestions = new ArrayList<>();
        this.history = new ArrayList<>();
        this.paletteCommandList = paletteCommandList;
    }

    public TextAreaState getInputState() {
        return inputState;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public List<PaletteCommand> getPaletteCommandList() {
        return paletteCommandList;
    }

    public boolean isActiveMode() {
        return isActiveMode;
    }

    public record HistoryEntry(String query, QueryResult queryResult) {}
    public record PaletteCommand(String title, String shortcut, Runnable action) {}

    private final TextAreaState inputState;
    private boolean isActiveMode = false;
    private boolean isRunning = true;
    private final List<HistoryEntry> history;
    private boolean isPaletteOpen = false;
    private final List<PaletteCommand> paletteCommandList;
    private int selectedPaletteIndex = 0;
    private boolean isAutocompleteOpen = false;
    private final List<String> suggestions;
    private int selectedSuggestionIndex = 0;
    private final AutoSuggestionEngine suggestionEngine = new AutoSuggestionEngine();
    private String currentSuffix = "";

    public static boolean isCursorAtEnd(TextAreaState state) {
        int lastRow = state.lineCount() - 1;
        if (state.cursorRow() != lastRow) {
            return false;
        }
        int lastLineLength = state.getLine(lastRow).length();
        return state.cursorCol() == lastLineLength;
    }

    public boolean onKeyEvent(KeyEvent event) {
        if(isPaletteOpen) {
            switch (event.code()) {
                case UP -> {
                    decrementPaletteIndex();
                    return true;
                }
                case DOWN -> {
                    incrementPaletteIndex();
                    return true;
                }
                case ENTER -> {
                    getCommand(selectedPaletteIndex).action.run();
                    closePalette();
                    return true;
                }
                case ESCAPE -> {
                    closePalette();
                    return true;
                }
            }
        } else if (isAutocompleteOpen) {
            switch (event.code()) {
                case UP -> {
                    decrementAutocompleteIndex();
                    return true;
                }
                case DOWN -> {
                    incrementAutocompleteIndex();
                    return true;
                }
                case TAB, ENTER -> {
                    inputState.setText(suggestions.get(selectedSuggestionIndex));
                    currentSuffix = "";
                    return true;
                }
                case ESCAPE -> {
                    closeAutocomplete();
                    return true;
                }
            }
        } else {
            switch (event.code()) {
                case CHAR -> {
                    if (event.modifiers().ctrl() && (event.isChar('P') || event.isChar('p'))) {
                        openPalette();
                        return true;
                    }
                    else if (event.modifiers().ctrl() && (event.isChar('H') || event.isChar('h'))) {
                        openAutocomplete();
                        return true;
                    }
                    else {
                        inputState.insert(event.string());
                        this.currentSuffix = suggestionEngine.getSuggestionSuffix(inputState.text(), history);
                        return true;
                    }
                }
                case BACKSPACE -> {
                    inputState.deleteBackward();
                    this.currentSuffix = suggestionEngine.getSuggestionSuffix(inputState.text(), history);
                    return true;
                }
                case DELETE -> {
                    inputState.deleteForward();
                    this.currentSuffix = suggestionEngine.getSuggestionSuffix(inputState.text(), history);
                    return true;
                }
                case LEFT -> {
                    inputState.moveCursorLeft();
                    return true;
                }
                case RIGHT -> {
                    if (!currentSuffix.isEmpty() && isCursorAtEnd(inputState))
                    {
                        if (event.modifiers().ctrl()) {
                            String word = suggestionEngine.getNextWord(currentSuffix);
                            inputState.insert(word);
                            currentSuffix = currentSuffix.substring(word.length());
                        } else {
                            inputState.insert(currentSuffix);
                            currentSuffix = "";
                        }
                    } else {
                        inputState.moveCursorRight();
                    }
                    return true;
                }
                case ENTER -> {
                    if (event.modifiers().ctrl()) {
                        //
                        return true;
                    }
                    inputState.insert('\n');
                    return true;
                }
            }
        }
        return false;
    }

    public int getSelectedPaletteIndex() {
        return selectedPaletteIndex;
    }

    private void incrementPaletteIndex() {
        selectedPaletteIndex = (selectedPaletteIndex + 1) % paletteCommandList.size();
    }
    private void decrementPaletteIndex() {
        selectedPaletteIndex = (selectedPaletteIndex - 1 + paletteCommandList.size()) % paletteCommandList.size();
    }

    private void incrementAutocompleteIndex() {
        if (!suggestions.isEmpty()) {
            selectedSuggestionIndex = (selectedSuggestionIndex + 1) % suggestions.size();
        }
    }
    private void decrementAutocompleteIndex() {
        if (!suggestions.isEmpty()) {
            selectedSuggestionIndex = (selectedSuggestionIndex - 1 + suggestions.size()) % suggestions.size();
        }
    }
    public PaletteCommand getCommand(int index) {
        return paletteCommandList.get(index);
    }
    public void openPalette() {
        isPaletteOpen = true;
        selectedPaletteIndex = 0;
    }
    public void closePalette() {
        isPaletteOpen = false;
        selectedPaletteIndex = 0;
    }
    public void openAutocomplete() {
        isAutocompleteOpen = true;
        selectedSuggestionIndex = 0;
    }
    public void closeAutocomplete() {
        isAutocompleteOpen = false;
        selectedSuggestionIndex = 0;
    }
    public void addCmd(String cmd, QueryResult result) {
        history.add(new HistoryEntry(cmd, result));
        isActiveMode = true;
        currentSuffix = "";
        inputState.clear();
    }
    public void clear() {
        history.clear();
        isActiveMode = false;
    }
    public String getCurrentSuffix() {
        return currentSuffix;
    }
    public boolean isPaletteOpen() {
        return isPaletteOpen;
    }
    public boolean isAutocompleteOpen() {
        return isAutocompleteOpen;
    }

    public int getSelectedSuggestionIndex() {
        return selectedSuggestionIndex;
    }
    public List<String> getSuggestions() {
        return suggestions;
    }
    public boolean isRunning() {
        return isRunning;
    }
    public void exit() {
        this.isRunning = false;
    }
    public int getLineCount() {
        return inputState.lineCount();
    }
    public int getCursorRow() {
        return inputState.cursorRow();
    }
    public int getCursorCol() {
        return inputState.cursorCol();
    }
}

