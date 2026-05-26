package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Routing paths to specific HTML files
        if (path.equals("/") || path.equals("/login")) {
            path = "/login.html";
        } else if (path.equals("/register")) {
            path = "/register.html";
        } else if (path.equals("/pegawai")) {
            path = "/pegawai.html";
        } else if (path.equals("/admin")) {
            path = "/admin.html";
        }

        // File location under "web" directory
        File file = new File("web" + path);

        if (!file.exists() || file.isDirectory()) {
            String errorMsg = "404 Not Found";
            exchange.sendResponseHeaders(404, errorMsg.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorMsg.getBytes());
            }
            return;
        }

        // Determine content type
        String contentType = "text/plain";
        String name = file.getName().toLowerCase();
        if (name.endsWith(".html")) {
            contentType = "text/html";
        } else if (name.endsWith(".css")) {
            contentType = "text/css";
        } else if (name.endsWith(".js")) {
            contentType = "application/javascript";
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}
