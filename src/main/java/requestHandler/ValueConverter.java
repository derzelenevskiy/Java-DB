package requestHandler;
import db.util.Comparator;
import java.time.LocalDate;


public class ValueConverter {

    public static Class<?> parseType(String typeName) {
        if (typeName.equalsIgnoreCase("INT") || typeName.equalsIgnoreCase("INTEGER")) {
            return Integer.class;
        } else if (typeName.equalsIgnoreCase("STRING")) {
            return String.class;
        }
        else if (typeName.equalsIgnoreCase("DOUBLE") || typeName.equalsIgnoreCase("FLOAT")) {
            return Double.class;
        }
        else if (typeName.equalsIgnoreCase("DATE")) {
            return LocalDate.class;
        }

        else if (typeName.equalsIgnoreCase("BOOLEAN") || typeName.equalsIgnoreCase("BOOL")) {
            return Boolean.class;
        }

        throw new IllegalArgumentException("Неизвестный тип: " + typeName);
    }

    private static String remove(String value) {
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        throw new IllegalArgumentException("Строка или дата должна быть в одинарных кавычках");
    }

    public static Object parseValue(String value, Class<?> type) {

        if (value == null || value.equalsIgnoreCase("NULL")) {
            return null;
        }

        if (type.equals(String.class)) {
            return remove(value);
        } else if (type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(LocalDate.class)) {
            return LocalDate.parse(remove(value));
        } else if (type.equals(Boolean.class)) {
            if (value.equalsIgnoreCase("true")) {
                return true;
            } else if (value.equalsIgnoreCase("false")) {
                return false;
            }
            throw new IllegalArgumentException("Ожидалось TRUE или FALSE");
        }

        throw new IllegalArgumentException("Неподдерживаемый тип");
    }

    public static Comparator parseComparator(String operator) {
        if (operator.equals("=")) {
            return Comparator.EQUAL;
        } else if (operator.equals("<")) {
            return Comparator.LESS;
        } else if (operator.equals(">")) {
            return Comparator.GREATER;
        } else if (operator.equals("<=")) {
            return Comparator.LESSorEQUAL;
        } else if (operator.equals(">=")) {
            return Comparator.GREATERorEQUAL;
        } else if (operator.equals("!=")) {
            return Comparator.NEQUAL;
        }

        throw new IllegalArgumentException("Неизвестный оператор: " + operator);
    }
}
