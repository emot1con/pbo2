import com.sun.net.httpserver.HttpServer;
import handler.AdminHandler;
import handler.AuthHandler;
import handler.PresensiHandler;
import handler.StaticFileHandler;
import service.AuthService;
import util.EnvLoader;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        // 1. Load configuration from .env file
        EnvLoader.load(".env");
        System.out.println("Configuration loaded.");

        // 2. Seed default admin user if not exists
        AuthService.seedAdmin();

        // 3. Setup and start HTTP Server
        int port = 8080;
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Register handlers
            server.createContext("/api/auth", new AuthHandler());
            server.createContext("/api/presensi", new PresensiHandler());
            server.createContext("/api/admin", new AdminHandler());
            server.createContext("/", new StaticFileHandler());

            // Set executor to default (single-threaded / standard)
            server.setExecutor(null);
            
            server.start();
            System.out.println("====================================================");
            System.out.println("Employee Attendance System server started successfully!");
            System.out.println("Listening on http://localhost:" + port);
            System.out.println("====================================================");

        } catch (IOException e) {
            System.err.println("Fatal Error starting HTTP Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
