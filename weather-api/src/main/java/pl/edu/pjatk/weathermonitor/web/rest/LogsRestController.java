package pl.edu.pjatk.weathermonitor.web.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@RestController
public class LogsRestController {

    @Value("${logging.file.name:logs/weather-monitor.log}")
    private String logFile;

    @GetMapping(value = "/api/logs", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLogs(@RequestParam(defaultValue = "200") int lines) throws IOException {
        int safeLines = Math.max(1, Math.min(lines, 2000));
        Path path = Path.of(logFile).toAbsolutePath();

        if (!Files.exists(path)) {
            return "Log file not found: " + path;
        }

        List<String> all = Files.readAllLines(path, StandardCharsets.UTF_8);
        int from = Math.max(0, all.size() - safeLines);

        return String.join("\n", all.subList(from, all.size()));
    }

    @PostMapping(value = "/api/logs/clear", produces = MediaType.TEXT_PLAIN_VALUE)
    public String clearLogs() throws IOException {
        Path path = Path.of(logFile).toAbsolutePath();

        if (!Files.exists(path)) {
            return "Log file not found: " + path;
        }

        Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING
        ).close();

        return "OK (cleared): " + path;
    }
}
