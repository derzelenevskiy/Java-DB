package tui;

import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.text.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlHighlighter {
    private static final Style KEYWORD_STYLE = Style.EMPTY.bold().fg(Color.hex("#9d67b7"));
    private static final Style STRING_STYLE = Style.EMPTY.fg(Color.hex("#6aab73"));
    private static final Style NUMBER_STYLE = Style.EMPTY.fg(Color.hex("#26aab7"));
    private static final Style BOOLEAN_STYLE = Style.EMPTY.fg(Color.hex("#ce8d6b"));
    private static final Style GHOST_STYLE = Style.EMPTY.fg(Color.DARK_GRAY);
    private static final Style DEFAULT_STYLE = Style.EMPTY;

    private static final Set<String> KEYWORDS = Set.of("SELECT", "FROM", "WHERE", "INSERT",
            "INTO", "VALUES", "UPDATE", "SET", "DELETE", "CREATE", "TABLE", "DROP", "INDEX",
            "ON", "AND", "OR", "NOT", "PRIMARY", "KEY", "INTEGER", "INT", "FLOAT", "DOUBLE",
            "STRING", "VARCHAR", "DATE", "BOOLEAN", "AS", "IS", "BY", "ORDER", "GROUP", "LIMIT");
    private static final Set<String> BOOLEANS = Set.of("TRUE", "FALSE", "NULL");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("('[^'\\r\\n]*(?:'|$))" +
            "|(\\b\\d+(?:\\.\\d+)?\\b)" + "|(\\b[a-zA-Z_][a-zA-Z0-9_]*\\b)" + "|(\\s+)" + "|(!=|<=|>=|[=><,;()])" + "|(.)");
    public List<Span> highlight (String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        List<Span> res = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text);
        while(matcher.find()) {
            String token = matcher.group();
            if (token.startsWith("'")) {
                res.add(Span.styled(token, STRING_STYLE));
            } else if (BOOLEANS.contains(token.toUpperCase())) {
                res.add(Span.styled(token, BOOLEAN_STYLE));
            } else if (KEYWORDS.contains(token.toUpperCase())) {
                res.add(Span.styled(token, KEYWORD_STYLE));
            } else if (Character.isDigit(token.charAt(0))) {
                res.add(Span.styled(token, NUMBER_STYLE));
            } else {
                res.add(Span.styled(token, DEFAULT_STYLE));
            }
        }
        return res;
    }

    public List<Span> ghostHighlight (String text, String suffix) {
        List<Span> res = highlight(text);
        if (suffix != null && !suffix.isEmpty()) {
            res.add(Span.styled(suffix, GHOST_STYLE));
        }
        return res;
    }
}
