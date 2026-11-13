import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RequestHandler {

    public List<String> TokenMaker(Scanner scanner) {
        StringBuilder command = new StringBuilder();
        List<String> tokens = new ArrayList<>();

        while (true) {
            String line = scanner.nextLine();
            command.append(line).append("\n");

            if (line.trim().endsWith(";")) {
                break;
            }
        }

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

    public class Command {
        String TableName;
        List<String> Row = new ArrayList<>();
        List<String> Columns = new ArrayList<>();
        List<String> Conditions = new ArrayList<>();
        List<List<String>> Rows = new ArrayList<>();
        List<String> Types = new ArrayList<>();
        String IndexName;
    }

    public class Parse {

        public Command parse(List<String> tokens) {
            String FirstToken = tokens.get(0);

            if (FirstToken.equalsIgnoreCase("INSERT")) {
                return parseInsert(tokens);
            }

            else if (FirstToken.equalsIgnoreCase("SELECT")) {
                return parseSelect(tokens);
            }

            else if (FirstToken.equalsIgnoreCase("UPDATE")) {
                return parseUpdate(tokens);
            }

            else if (FirstToken.equalsIgnoreCase("DELETE")) {
                return parseDelete(tokens);
            }

            else if (FirstToken.equalsIgnoreCase("CREATE")) {
                if (tokens.get(1).equalsIgnoreCase("TABLE")) {
                    return parseCreate(tokens);
                } else if (tokens.get(1).equalsIgnoreCase("INDEX")) {
                    return parseCreateIndex(tokens);
                }
            }

            else if (FirstToken.equalsIgnoreCase("DROP")) {
                return parseDrop(tokens);
            }

            throw new IllegalArgumentException("Неизвестная команда");
        }


        public Command parseInsert(List<String> tokens) {
            int position = 1;
            Command command = new Command();

            if (tokens.get(position).equalsIgnoreCase("INTO")) {
                command.TableName = tokens.get(position + 1);

                if (tokens.get(position + 2).equalsIgnoreCase("VALUES")) {
                    position = position + 3;
                } else if (tokens.get(position + 2).equals("(")) {
                    position = position + 3;

                    while (!tokens.get(position).equals(")")) {
                        String current = tokens.get(position);
                        if (!current.equals(",")) {
                            command.Columns.add(current);
                        }
                        position++;
                    }

                    position = position + 2;
                }

                while (tokens.get(position).equals("(")) {
                    position++;

                    List<String> row = new ArrayList<>();

                    while (!tokens.get(position).equals(")")) {
                        String current = tokens.get(position);
                        if (!current.equals(",")) {
                            row.add(current);
                        }
                        position++;
                    }

                    command.Rows.add(row);
                    position++;

                    if (tokens.get(position).equals(",")) {
                        position++;
                    } else {
                        break;
                    }
                }
            }

            return command;
        }


        public Command parseSelect(List<String> tokens) {
            int position = 1;
            Command command = new Command();

            if (tokens.get(position).equals("*")) {
                command.Columns.add("*");
                position++;
            } else {
                while (!tokens.get(position).equalsIgnoreCase("FROM")) {
                    String current = tokens.get(position);
                    if (!current.equals(",")) {
                        command.Columns.add(current);
                    }
                    position++;
                }
            }

            if (tokens.get(position).equalsIgnoreCase("FROM")) {
                command.TableName = tokens.get(position + 1);

                if (tokens.get(position + 2).equalsIgnoreCase("WHERE")) {
                    command.Conditions.add(tokens.get(position + 3));
                    command.Conditions.add(tokens.get(position + 4));
                    command.Conditions.add(tokens.get(position + 5));
                }
            }

            return command;
        }

        public Command parseDelete(List<String> tokens) {
            int position = 1;
            Command command = new Command();

            if (tokens.get(position).equalsIgnoreCase("FROM")) {
                command.TableName = tokens.get(position + 1);
                if (tokens.get(position + 2).equalsIgnoreCase("WHERE")) {
                    command.Conditions.add(tokens.get(position + 3));
                    command.Conditions.add(tokens.get(position + 4));
                    command.Conditions.add(tokens.get(position + 5));
                }
            }

            return command;
        }

        public Command parseUpdate(List<String> tokens) {
            int position = 1;
            Command command = new Command();

            command.TableName = tokens.get(position);

            if (tokens.get(position + 1).equalsIgnoreCase("SET")) {
                position = position + 2;

                while (!tokens.get(position).equalsIgnoreCase("WHERE")) {

                    if (tokens.get(position).equals(",")) {
                        position++;
                    } else if (tokens.get(position).equals(";")) {
                        return command;
                    } else {
                        command.Columns.add(tokens.get(position));
                        command.Row.add(tokens.get(position + 2));

                        position = position + 3;
                    }
                }

                if (tokens.get(position).equalsIgnoreCase("WHERE")) {
                    command.Conditions.add(tokens.get(position + 1));
                    command.Conditions.add(tokens.get(position + 2));
                    command.Conditions.add(tokens.get(position + 3));
                }
            }

            return command;
        }

        public Command parseDrop(List<String> tokens) {
            Command command = new Command();

            if (tokens.get(1).equalsIgnoreCase("TABLE")) {
                command.TableName = tokens.get(2);
            }

            return command;
        }


        public Command parseCreate(List<String> tokens) {
            int position = 2;
            Command command = new Command();

            command.TableName = tokens.get(position);
            position++;

            if (tokens.get(position).equals("(")) {
                position++;

                while (!tokens.get(position).equals(")")) {
                    String columnName = tokens.get(position);
                    position++;

                    String columnType = tokens.get(position);
                    position++;

                    command.Columns.add(columnName);
                    command.Types.add(columnType);

                    if (tokens.get(position).equals(",")) {
                        position++;
                    }
                }
            }

            return command;
        }

        public Command parseCreateIndex(List<String> tokens) {
            int position = 2;
            Command command = new Command();

            command.IndexName = tokens.get(position);
            position++;

            if (tokens.get(position).equalsIgnoreCase("ON")) {
                command.TableName = tokens.get(position + 1);
                position = position + 2;

                if (tokens.get(position).equals("(")) {
                    command.Columns.add(tokens.get(position + 1));
                }
            }

            return command;
        }
    }
}
