package tui;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoSuggestionEngine {
    private static final Pattern NEXT_WORD = Pattern.compile("^(\\s*\\S+)");

    public String getSuggestionSuffix(String currentInput, List<UiState.HistoryEntry> successfulHistory) {
        if (currentInput == null || currentInput.isEmpty() || successfulHistory == null || successfulHistory.isEmpty()) {
            return "";
        }
        String inputLower = currentInput.toLowerCase();

        for (int i = successfulHistory.size() - 1; i >= 0; --i) {
            UiState.HistoryEntry entry = successfulHistory.get(i);
            if (entry == null || entry.query() == null) {
                continue;
            }
            String pastQuery = entry.query();
            if (pastQuery.length() > currentInput.length() && pastQuery.toLowerCase().startsWith(inputLower)) {
                return pastQuery.substring(currentInput.length());
            }
        }
        return "";
    }
    public String getNextWord (String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return "";
        }
        Matcher matcher = NEXT_WORD.matcher(suffix);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return suffix;
    }
}
