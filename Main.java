import com.sun.net.httpserver.HttpServer;
import handlers.RegisterHandler;
import handlers.StaticFileHandler;
import handlers.LoginHandler;
import sessions.SessionManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        SessionManager sessionManager = new SessionManager();

        // Путь к папке ресурсов
        String resourcesPath = "resources";

        // Обработчик для регистрации
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler(sessionManager));
        
        // Обработчик для статических файлов
        server.createContext("/", new StaticFileHandler(resourcesPath));



        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен на http://localhost:8081");
    }
}
