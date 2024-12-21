package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import sessions.SessionManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginHandler implements HttpHandler {
    private static final String FILE_PATH = "resources/users.json";
    private final SessionManager sessionManager;

    public LoginHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            // Чтение данных из тела запроса
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject request = new JSONObject(body);

            String username = request.getString("username");
            String password = request.getString("password");

            // Проверка данных пользователя
            JSONArray users = readUsersFromFile();
            for (Object obj : users) {
                JSONObject user = (JSONObject) obj;
                if (user.getString("username").equals(username) &&
                    user.getString("password").equals(password)) {

                    // Создаём сессию
                    String sessionId = sessionManager.createSession(username);
                    exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=" + sessionId);

                    sendResponse(exchange, 200, new JSONObject().put("message", "Вход выполнен успешно."));
                    return;
                }
            }

            // Неверные логин или пароль
            sendResponse(exchange, 401, new JSONObject().put("error", "Неверный логин или пароль."));
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

    private void sendResponse(HttpExchange exchange, int status, JSONObject response) throws IOException {
        byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
