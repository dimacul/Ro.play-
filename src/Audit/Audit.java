package Audit;
import java.io.FileWriter;
import java.io.IOException;

public class Audit {
    private static final String FILE_NAME = "audit.csv";

    public static void logInAudit(String actionName) {
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            long timestamp = System.currentTimeMillis();
            writer.write(actionName + "," + timestamp + "\n");
        } catch (IOException e) {
            e.printStackTrace();

        }

    }
}
