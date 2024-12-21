package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {
    private final String baseDirectory;

    public StaticFileHandler(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Если путь пустой или "/"
        if (path.equals("/")) {
            path = "/welcome.html";
        }

        // Не обрабатываем /register.html и /login.html
        if (path.equals("/register.html") || path.equals("/login.html")) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String filePath = baseDirectory + path;

        if (Files.exists(Paths.get(filePath))) {
            String contentType = getContentType(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            exchange.sendResponseHeaders(200, fileBytes.length);

            exchange.getResponseBody().write(fileBytes);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
        exchange.close();
    }

    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        return "application/octet-stream";
    }
}
