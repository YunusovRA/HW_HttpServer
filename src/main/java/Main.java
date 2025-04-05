import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        // Add handler for serving static files
        server.addHandler("GET", "/index.html", (request, out) -> serveFile(request, out, "/index.html"));
        server.addHandler("GET", "/spring.svg", (request, out) -> serveFile(request, out, "/spring.svg"));
        server.addHandler("GET", "/spring.png", (request, out) -> serveFile(request, out, "/spring.png"));
        server.addHandler("GET", "/resources.html", (request, out) -> serveFile(request, out, "/resources.html"));
        server.addHandler("GET", "/styles.css", (request, out) -> serveFile(request, out, "/styles.css"));
        server.addHandler("GET", "/app.js", (request, out) -> serveFile(request, out, "/app.js"));
        server.addHandler("GET", "/links.html", (request, out) -> serveFile(request, out, "/links.html"));
        server.addHandler("GET", "/forms.html", (request, out) -> serveFile(request, out, "/forms.html"));
        server.addHandler("GET", "/events.html", (request, out) -> serveFile(request, out, "/events.html"));
        server.addHandler("GET", "/events.js", (request, out) -> serveFile(request, out, "/events.js"));

        // Add handler for classic.html with time replacement
        server.addHandler("GET", "/classic.html", (request, out) -> {
            final var filePath = Path.of(".", "public", "/classic.html");
            final var mimeType = Files.probeContentType(filePath);
            final var template = Files.readString(filePath);
            final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n"
            ).getBytes());
            out.write(content);
        });

        // Добавляем новый хендлер для тестирования Query String
        server.addHandler("GET", "/messages", (request, out) -> {
            final var paramValue = request.getQueryParam("last");
            final var response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + paramValue.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    paramValue;
            out.write(response.getBytes());
        });

        server.listen(9999);
    }

    private static void serveFile(Request request, BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);
        
        out.write((
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: " + mimeType + "\r\n" +
            "Content-Length: " + length + "\r\n" +
            "Connection: close\r\n" +
            "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
    }
}
