package requestHandler;
import java.util.Scanner;

public class CommandReader {
    public String readCommand(Scanner scanner) {
        StringBuilder command = new StringBuilder();

        while (true) {
            String line = scanner.nextLine();
            command.append(line).append("\n");

            if (line.trim().endsWith(";")) {
                break;
            }
        }

        return command.toString();
    }
}