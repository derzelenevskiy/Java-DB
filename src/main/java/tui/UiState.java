package tui;

import dev.tamboui.tui.event.KeyEvent;
import dev.tamboui.widgets.input.TextAreaState;
import dev.tamboui.widgets.wavetext.WaveTextState;
import model.QueryResult;

import java.util.ArrayList;
import java.util.List;


public class UiState {
    public UiState(List<PaletteCommand> paletteCommandList, List<DirectoryCommand> directoryCommandList) {
        this.directoryCommandList = directoryCommandList;
        this.inputState = new TextAreaState();
        this.suggestions = new ArrayList<>();
        this.successfulHistory = new ArrayList<>();
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
    public List<DirectoryCommand> getDirectoryCommandList() { return directoryCommandList; }
    public boolean isActiveMode() {
        return isActiveMode;
    }

    private int feedScrollOffset = 0;
    private int feedHScrollOffset = 0;
    private boolean autoScrollToBottom = true;

    public int getFeedScrollOffset() { return feedScrollOffset; }
    public int getFeedHScrollOffset() { return feedHScrollOffset; }
    public boolean isAutoScrollToBottom() { return autoScrollToBottom; }

    public void scrollFeedUp(int lines) {
        this.feedScrollOffset = Math.max(0, this.feedScrollOffset - lines);
        this.autoScrollToBottom = false;
    }

    public void scrollFeedDown(int lines) {
        this.feedScrollOffset += lines;
        this.autoScrollToBottom = true;
    }

    public void scrollFeedLeft(int chars) {
        this.feedHScrollOffset = Math.max(0, this.feedHScrollOffset - chars);
    }

    public void scrollFeedRight(int chars) {
        this.feedHScrollOffset += chars;
    }

    public void setFeedScrollOffset(int offset) {
        this.feedScrollOffset = offset;
    }



    public record HistoryEntry(String query, QueryResult queryResult) {}
    public record PaletteCommand(String title, String shortcut, Runnable action) {}
    public record DirectoryCommand(String title, Runnable action) {}

    private final List<HistoryEntry> successfulHistory;
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
    private boolean isDirectoryOpen = false;
    private int selectedDirectoryIndex = 0;
    private final List<DirectoryCommand> directoryCommandList;
    private final WaveTextState logoWaveState = new WaveTextState();

    public WaveTextState getLogoWaveState() {
        return logoWaveState;
    }
    public void openDirectory() {
        isDirectoryOpen = true;
        selectedDirectoryIndex = 0;
    }
    public void closeDirectory() {
        isDirectoryOpen = false;
        selectedDirectoryIndex = 0;
    }
    private void incrementDirectoryIndex() {
        if (!directoryCommandList.isEmpty()) {
            selectedDirectoryIndex = (selectedDirectoryIndex + 1) % directoryCommandList.size();
        }
    }
    private void decrementDirectoryIndex() {
        if (!directoryCommandList.isEmpty()) {
            selectedDirectoryIndex = (selectedDirectoryIndex - 1 + directoryCommandList.size()) % directoryCommandList.size();
        }
    }

    public static boolean isCursorAtEnd(TextAreaState state) {
        int lastRow = state.lineCount() - 1;
        if (state.cursorRow() != lastRow) {
            return false;
        }
        int lastLineLength = state.getLine(lastRow).length();
        return state.cursorCol() == lastLineLength;
    }

    public void onKeyEvent(KeyEvent event) {
        if(isPaletteOpen) {
            switch (event.code()) {
                case UP -> {
                    decrementPaletteIndex();
                }
                case DOWN -> {
                    incrementPaletteIndex();
                }
                case ENTER -> {
                    getCommand(selectedPaletteIndex).action().run();
                    closePalette();
                }
                case ESCAPE -> {
                    closePalette();
                }
            }
        } else if (isAutocompleteOpen) {
            switch (event.code()) {
                case UP -> {
                    decrementAutocompleteIndex();
                }
                case DOWN -> {
                    incrementAutocompleteIndex();
                }
                case TAB, ENTER -> {
                    if (!suggestions.isEmpty() && selectedSuggestionIndex < suggestions.size()) {
                        inputState.setText(suggestions.get(selectedSuggestionIndex));
                        currentSuffix = "";
                        closeAutocomplete();
                    }
                }
                case ESCAPE -> {
                    closeAutocomplete();
                }
            }
        } else if (isDirectoryOpen) {
            switch (event.code()) {

                case UP -> {
                    decrementDirectoryIndex();
                }
                case DOWN -> {
                    incrementDirectoryIndex();
                }
                case TAB, ENTER -> {
                    getCommand(selectedDirectoryIndex).action().run();
                    closeDirectory();
                }
                case ESCAPE -> {
                    closeDirectory();
                }
            }
        } else {
            switch (event.code()) {
                case CHAR -> {
                    if (event.modifiers().ctrl() && (event.isChar('P') || event.isChar('p'))) {
                        openPalette();
                    }
                    else if (event.modifiers().ctrl() && (event.isChar('H') || event.isChar('h'))) {
                        openAutocomplete();
                    }
                    else if (event.modifiers().ctrl() && (event.isChar('D') || event.isChar('d'))) {
                        openDirectory();
                    } else if (event.modifiers().ctrl() && (event.isChar('L') || event.isChar('l'))) {
                        clear();
                    } else {
                        inputState.insert(event.string());
                        this.currentSuffix = suggestionEngine.getSuggestionSuffix(inputState.text(), successfulHistory);
                    }
                }
                case PAGE_UP -> scrollFeedUp(5);
                case PAGE_DOWN -> scrollFeedDown(5);
                case UP -> {
                    if (event.modifiers().ctrl()) {
                        scrollFeedUp(2);
                    } else {
                        inputState.moveCursorUp();
                    }
                }
                case DOWN -> {
                    if (event.modifiers().ctrl()) {
                        scrollFeedDown(2);
                    } else {
                        inputState.moveCursorDown();
                    }
                }
                case BACKSPACE -> {
                    inputState.deleteBackward();
                    this.currentSuffix = suggestionEngine.getSuggestionSuffix(inputState.text(), successfulHistory);
                }
                case DELETE -> {
                    inputState.deleteForward();
                    this.currentSuffix = suggestionEngine.getSuggestionSuffix(inputState.text(), successfulHistory);
                }
                case LEFT -> {
                    if (event.modifiers().alt()) {
                        scrollFeedLeft(10);
                    } else {
                        inputState.moveCursorLeft();
                    }
                }
                case RIGHT -> {
                    if (event.modifiers().alt()) {
                        scrollFeedRight(10);
                    } else {
                        if (!currentSuffix.isEmpty() && isCursorAtEnd(inputState)) {
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
                    }
                }
                case ENTER -> {
                    inputState.insert('\n');
                }
            }
        }
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

        suggestions.clear();

        for (int i = successfulHistory.size() - 1; i >= 0; i--) {
            suggestions.add(successfulHistory.get(i).query());
        }
    }
    public void closeAutocomplete() {
        isAutocompleteOpen = false;
        selectedSuggestionIndex = 0;
    }
    public void addCmd(String cmd, QueryResult result) {
        if (result.isSuccess()) {
            successfulHistory.add(new HistoryEntry(cmd, result));
        }
        history.add(new HistoryEntry(cmd, result));
        isActiveMode = true;
        currentSuffix = "";
        inputState.clear();
        resetFeedScroll();
    }
    public void resetFeedScroll() {
        this.feedScrollOffset = 0;
        this.feedHScrollOffset = 0;
        this.autoScrollToBottom = true;
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
    public boolean isDirectoryOpen() {
        return isDirectoryOpen;
    }
    public int getSelectedDirectoryIndex() {
        return selectedDirectoryIndex;
    }
}

