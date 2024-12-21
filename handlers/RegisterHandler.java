package handlers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;
import handlers.StaticFileHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RegisterHandler implements HttpHandler {
    private static final String FILE_PATH = "resources/users.json";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            // Чтение данных из тела запроса
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject request = new JSONObject(body);

            String username = request.getString("username");
            String password = request.getString("password");
            String name = request.getString("name");
            String email = request.getString("email");

            // Загрузка существующих пользователей
            JSONArray users = readUsersFromFile();

            // Проверка, существует ли пользователь с таким логином
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

            // Сохранение пользователей в файл
            saveUsersToFile(users);

            sendResponse(exchange, 201, new JSONObject().put("message", "Регистрация прошла успешно."));
        } else {
            exchange.sendResponseHeaders(405, -1); // Метод не разрешён
        }
    }

    private JSONArray readUsersFromFile() throws IOException {
        if (!Files.exists(Paths.get(FILE_PATH))) {
            // Если файл отсутствует, возвращаем пустой массив
            return new JSONArray();
        }
        String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)), StandardCharsets.UTF_8);
        return new JSONArray(content.isEmpty() ? "[]" : content);
    }

    private void saveUsersToFile(JSONArray users) throws IOException {
        Files.write(Paths.get(FILE_PATH), users.toString(4).getBytes(StandardCharsets.UTF_8));
    }

    private void sendResponse(HttpExchange exchange, int status, JSONObject response) throws IOException {
        byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}