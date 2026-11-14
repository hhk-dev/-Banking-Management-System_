package BankSystem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class StatementWriter {
    private StatementWriter() {}

    public static void write(Path file, List<String> lines) throws IOException {
        if (file == null) {
            throw new InvalidInputException("File path is required");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}