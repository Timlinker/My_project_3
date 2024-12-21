package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RegisterHandler implements HttpHandler {
    private static final String FILE_PATH = "resources/users.json";
    private static final String HTML_PATH = "resources/register.html";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Возвращаем HTML-страницу регистрации
            String html = Files.readString(Paths.get(HTML_PATH), StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(html.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        } else if ("POST".equals(exchange.getRequestMethod())) {
            // Обработка данных регистрации
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject request = new JSONObject(body);

            String username = request.getString("username");
            String password = request.getString("password");
            String name = request.getString("name");
            String email = request.getString("email");

            // Чтение существующих пользователей
            JSONArray users = readUsersFromFile();

            // Проверяем, существует ли пользователь с таким логином
            for (Object obj : users) {
                JSONObject user = (JSONObject) obj;
                if (user.getString("username").equals(username)) {
                    sendResponse(exchange, 400, new JSONObject().put("error", "Пользователь с таким логином уже существует."));
                    return;
                }
            }

            // Добавление нового пользователя
            JSONObject newUser = new JSONObject();
            newUser.put("username", username);
            newUser.put("password", password);
            newUser.put("name", name);
            newUser.put("email", email);
            users.put(newUser);

            // Сохранение в файл
            saveUsersToFile(users);

            sendResponse(exchange, 201, new JSONObject().put("message", "Регистрация прошла успешно."));
        } else {
            exchange.sendResponseHeaders(405, -1); // Метод не разрешён
        }
    }

    private JSONArray readUsersFromFile() throws IOException {
        if (!Files.exists(Paths.get(FILE_PATH))) {
            return new JSONArray();
        }
        String content = Files.readString(Paths.get(FILE_PATH), StandardCharsets.UTF_8);
        return new JSONArray(content.isEmpty() ? "[]" : content);
    }

    private void saveUsersToFile(JSONArray users) throws IOException {
        Files.writeString(Paths.get(FILE_PATH), users.toString(4), StandardCharsets.UTF_8);
    }

    private void sendResponse(HttpExchange exchange, int status, JSONObject response) throws IOException {
        byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
