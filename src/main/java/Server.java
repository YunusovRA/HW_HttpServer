import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executorService;
    private final Map<String, Handler> handlers;

    public Server() {
        this.executorService = Executors.newFixedThreadPool(64);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.put(method + " " + path, handler);
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.execute(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        try (socket;
             final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // Parse request
            final var requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }

            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }

            final var method = parts[0];
            final var path = parts[1];

            // Read headers
            final var headers = new HashMap<String, String>();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                final var i = line.indexOf(":");
                if (i != -1) {
                    headers.put(line.substring(0, i).trim(), line.substring(i + 1).trim());
                }
            }

            // Read body if present
            final var contentLength = headers.get("Content-Length");
            final String body;
            if (contentLength != null) {
                final var length = Integer.parseInt(contentLength);
                final var bodyChars = new char[length];
                in.read(bodyChars, 0, length);
                body = new String(bodyChars);
            } else {
                body = "";
            }

            // Create request object
            final var request = new Request(method, path, headers, body);

            // Find and execute handler
            final var handlerKey = method + " " + request.getPath();
            if (handlers.containsKey(handlerKey)) {
                handlers.get(handlerKey).handle(request, out);
            } else {
                // Default 404 response
                out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
                ).getBytes());
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        executorService.shutdown();
    }
}
