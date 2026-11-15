package requestHandler;
import java.util.ArrayList;
import java.util.List;

public class TokenMaker {

    public List<String> tokenMaker(String command) {
        List<String> tokens = new ArrayList<>();

        StringBuilder word = new StringBuilder();

        for (int i = 0; i < command.length(); i++) {
            char current = command.charAt(i);

            if (current != ' ' && current != '\n') {
                if (current == '(' || current == ',' || current == ')' || current == ';' || current == '*') {
                    if (word.length() > 0) {
                        tokens.add(word.toString());
                        word.setLength(0);
                    }

                    tokens.add(String.valueOf(current));
                } else if (current == '\'') {
                    word.append(current);
                    i = i + 1;

                    while (i < command.length() && command.charAt(i) != '\'') {
                        word.append(command.charAt(i));
                        i = i + 1;
                    }

                    if (i >= command.length()) {
                        throw new IllegalArgumentException("Незакрытая строка");
                    }

                    word.append(command.charAt(i));
                    tokens.add(word.toString());
                    word.setLength(0);

                } else if (i + 1 < command.length() &&
                        (current == '>' && command.charAt(i + 1) == '=' ||
                                current == '<' && command.charAt(i + 1) == '=' ||
                                current == '!' && command.charAt(i + 1) == '=')) {

                    if (word.length() > 0) {
                        tokens.add(word.toString());
                    }

                    word.setLength(0);
                    word.append(command.charAt(i));
                    word.append(command.charAt(i + 1));
                    i = i + 1;
                    tokens.add(word.toString());
                    word.setLength(0);

                } else if (current == '>' || current == '<' || current == '=') {

                    if (word.length() > 0) {
                        tokens.add(word.toString());
                    }

                    word.setLength(0);
                    tokens.add(String.valueOf(current));
                    word.setLength(0);

                } else {
                    word.append(current);
                }
            } else {
                if (word.length() > 0) {
                    tokens.add(word.toString());
                }
                word.setLength(0);
            }
        }
        if (word.length() > 0) {
            tokens.add(word.toString());
        }

        return tokens;
    }
}