package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import sessions.SessionManager;

public class LoginHandler implements HttpHandler {
    private static final String FILE_PATH = "resources/users.json";
    private static final String HTML_PATH = "resources/login.html";
    private final SessionManager sessionManager;

    public LoginHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Возвращаем HTML-страницу входа
            String html = Files.readString(Paths.get(HTML_PATH), StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(html.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        } else if ("POST".equals(exchange.getRequestMethod())) {
            // Обработка данных входа
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject request = new JSONObject(body);

            String username = request.getString("username");
            String password = request.getString("password");

            JSONArray users = readUsersFromFile();
            for (Object obj : users) {
                JSONObject user = (JSONObject) obj;
                if (user.getString("username").equals(username) &&
                        user.getString("password").equals(password)) {

                    String sessionId = sessionManager.createSession(username);
                    exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=" + sessionId);
                    sendResponse(exchange, 200, new JSONObject().put("message", "Вход выполнен успешно."));
                    return;
                }
            }

            sendResponse(exchange, 401, new JSONObject().put("error", "Неверный логин или пароль."));
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private JSONArray readUsersFromFile() throws IOException {
        if (!Files.exists(Paths.get(FILE_PATH))) {
            return new JSONArray();
        }
        String content = Files.readString(Paths.get(FILE_PATH), StandardCharsets.UTF_8);
        return new JSONArray(content.isEmpty() ? "[]" : content);
    }

    private void sendResponse(HttpExchange exchange, int status, JSONObject response) throws IOException {
        byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
