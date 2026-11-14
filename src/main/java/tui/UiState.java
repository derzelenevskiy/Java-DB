package tui;

import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.tui.event.KeyEvent;
import dev.tamboui.widgets.input.TextInputState;
import model.QueryResult;

import java.util.ArrayList;
import java.util.List;

public class UiState {
    public UiState(List<PaletteCommand> paletteCommandList) {
        this.inputState = new TextInputState();
        this.suggestions = new ArrayList<>();
        this.history = new ArrayList<>();
        this.paletteCommandList = paletteCommandList;
    }

    public record HistoryEntry(String query, QueryResult queryResult) {}
    public record PaletteCommand(String title, String shortcut, Runnable action) {}

    private final TextInputState inputState;
    private boolean isActiveMode = false;
    private boolean isRunning = true;
    private List<HistoryEntry> history;
    private boolean isPaletteOpen = false;
    private final List<PaletteCommand> paletteCommandList;
    private int selectedPaletteIndex = 0;
    private boolean isAutocompleteOpen = false;
    private List<String> suggestions;
    private int selectedSuggestionIndex = 0;
    private final AutoSuggestionEngine suggestionEngine = new AutoSuggestionEngine();
    private String currentSuffix = "";

    public boolean onKeyEvent(KeyEvent event) {
        if(!isPaletteOpen && !isAutocompleteOpen) {
            switch (event.code()) {
                case CHAR -> {
                    if (event.modifiers().ctrl() && (event.isChar('P') || event.isChar('p'))) {
                        openPalette();
                    }
                    else if (event.modifiers().ctrl() && (event.isChar('H') || event.isChar('h'))) {
                        openAutocomplete();
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
                    if (event.modifiers().ctrl()) {
                        String word = suggestionEngine.getNextWord(currentSuffix);
                        inputState.insert(word);
                        currentSuffix = currentSuffix.substring(word.length());
                    } else {
                        inputState.insert(currentSuffix);
                        currentSuffix = "";
                    }
                    return true;
                }
            }
        } else if (!isAutocompleteOpen) {
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
                    execute(getCommand(selectedPaletteIndex));
                    return true;
                }
                case ESCAPE -> {
                    closePalette();
                    return true;
                }
            }
        } else {
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
                    //
                    return true;
                }
            }
        }
        return false;
    }

    private void incrementPaletteIndex() {
        selectedPaletteIndex = (selectedPaletteIndex + 1) % paletteCommandList.size();
    }
    private void decrementPaletteIndex() {
        selectedPaletteIndex = (selectedPaletteIndex - 1 + paletteCommandList.size()) % paletteCommandList.size();
    }

    private void incrementAutocompleteIndex() {
        selectedSuggestionIndex = (selectedSuggestionIndex + 1) % suggestions.size();
    }
    private void decrementAutocompleteIndex() {
        selectedSuggestionIndex = (selectedSuggestionIndex - 1 + suggestions.size()) % suggestions.size();
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
    }
    public void clear() {
        history.clear();
        isActiveMode = false;
    }
}

