package handlers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import handlers.RegisterHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

// Обработчик для статических файлов
public class StaticFileHandler implements HttpHandler {
    private final String baseDirectory;

    public StaticFileHandler(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Если путь пустой или "/", перенаправляем на register.html
        if (path.equals("/") || path.isEmpty()) {
            path = "/welcome.html";
        }

        // Полный путь к файлу
        String filePath = baseDirectory + path;

        // Проверяем, существует ли файл
        if (Files.exists(Paths.get(filePath))) {
            // Устанавливаем Content-Type на основе расширения файла
            String contentType = getContentType(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);

            // Читаем файл и отправляем содержимое
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            exchange.sendResponseHeaders(200, fileBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        } else {
            // Если файл не найден, возвращаем 404
            String response = "404 File Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}