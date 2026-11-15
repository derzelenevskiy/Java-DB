package requestHandler;
import java.util.ArrayList;
import java.util.List;

public class SqlParser {

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
                return parseCreate(tokens);
        }

        else if (FirstToken.equalsIgnoreCase("DROP")) {
            return parseDrop(tokens);
        }

        throw new IllegalArgumentException("Неизвестная команда");
    }


    private Command parseInsert(List<String> tokens) {
        int position = 1;
        InsertCommand command = new InsertCommand();

        if (tokens.get(position).equalsIgnoreCase("INTO")) {
            command.setTableName(tokens.get(position + 1));

            if (tokens.get(position + 2).equalsIgnoreCase("VALUES")) {
                position = position + 3;
            } else if (tokens.get(position + 2).equals("(")) {
                position = position + 3;

                while (!tokens.get(position).equals(")")) {
                    String current = tokens.get(position);
                    if (!current.equals(",")) {
                        command.addColumn(current);
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

                command.addRow(row);
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


    private Command parseSelect(List<String> tokens) {
        int position = 1;
        SelectCommand command = new SelectCommand();

        if (tokens.get(position).equals("*")) {
            command.addColumn("*");
            position++;
        } else {
            while (!tokens.get(position).equalsIgnoreCase("FROM")) {
                String current = tokens.get(position);
                if (!current.equals(",")) {
                    command.addColumn(current);
                }
                position++;
            }
        }

        if (tokens.get(position).equalsIgnoreCase("FROM")) {
            command.setTableName(tokens.get(position + 1));

            if (tokens.get(position + 2).equalsIgnoreCase("WHERE")) {
                command.addCondition(new Condition(
                        tokens.get(position + 3),
                        tokens.get(position + 4),
                        tokens.get(position + 5)
                ));
            }
        }

        return command;
    }

    private Command parseDelete(List<String> tokens) {
        int position = 1;
        DeleteCommand command = new DeleteCommand();

        if (tokens.get(position).equalsIgnoreCase("FROM")) {
            command.setTableName(tokens.get(position + 1));
            if (tokens.get(position + 2).equalsIgnoreCase("WHERE")) {
                command.addCondition(new Condition(
                        tokens.get(position + 3),
                        tokens.get(position + 4),
                        tokens.get(position + 5)
                ));
            }
        }

        return command;
    }

    private Command parseUpdate(List<String> tokens) {
        int position = 1;
        UpdateCommand command = new UpdateCommand();

        command.setTableName(tokens.get(position));

        if (tokens.get(position + 1).equalsIgnoreCase("SET")) {
            position = position + 2;

            while (!tokens.get(position).equalsIgnoreCase("WHERE")) {

                if (tokens.get(position).equals(",")) {
                    position++;
                } else if (tokens.get(position).equals(";")) {
                    return command;
                } else {
                    command.addUpdate(tokens.get(position), tokens.get(position + 2));

                    position = position + 3;
                }
            }

            if (tokens.get(position).equalsIgnoreCase("WHERE")) {
                command.addCondition(new Condition(
                        tokens.get(position + 1),
                        tokens.get(position + 2),
                        tokens.get(position + 3)
                ));
            }
        }

        return command;
    }

    private Command parseDrop(List<String> tokens) {
        DropTableCommand command = new DropTableCommand();

        if (tokens.get(1).equalsIgnoreCase("TABLE")) {
            command.setTableName(tokens.get(2));
        }

        return command;
    }


    private Command parseCreate(List<String> tokens) {
        int position = 2;
        CreateTableCommand command = new CreateTableCommand();

        command.setTableName(tokens.get(position));
        position++;

        if (tokens.get(position).equals("(")) {
            position++;

            while (!tokens.get(position).equals(")")) {
                String columnName = tokens.get(position);
                position++;

                String columnType = tokens.get(position);
                position++;

                command.addColumn(columnName, columnType);

                while (!tokens.get(position).equals(",") && !tokens.get(position).equals(")")) {
                    if (tokens.get(position).equalsIgnoreCase("PRIMARY")
                            && tokens.get(position + 1).equalsIgnoreCase("KEY")) {
                        command.setPrimaryKey(columnName);
                        position = position + 2;
                    } else if (tokens.get(position).equalsIgnoreCase("REFERENCES")) {
                        String refTable = tokens.get(position + 1);
                        command.addReference(columnName, refTable);
                        position = position + 2;
                    } else {
                        throw new IllegalArgumentException("Неизвестный параметр столбца: " + tokens.get(position));
                    }
                }

                if (tokens.get(position).equals(",")) {
                    position++;
                }
            }
        }

        return command;
    }
}