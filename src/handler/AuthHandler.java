package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.AuthService;
import util.JsonHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable simple CORS if accessed from elsewhere (though they are same origin)
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("/api/auth/register".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleRegister(exchange);
            } else if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleLogin(exchange);
            } else if ("/api/auth/logout".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleLogout(exchange);
            } else if ("/api/auth/me".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleMe(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> data = JsonHelper.parseJson(body);

        String email = data.get("email");
        String password = data.get("password");
        String nama = data.get("nama");
        String jabatan = data.get("jabatan");
        String divisi = data.get("divisi");

        if (email == null || password == null || nama == null) {
            sendResponse(exchange, 400, "{\"error\":\"Email, password, and nama are required\"}");
            return;
        }

        try {
            boolean success = AuthService.register(email, password, nama, jabatan, divisi);
            if (success) {
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Pegawai registered successfully\"}");
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Email is already registered\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> data = JsonHelper.parseJson(body);

        String email = data.get("email");
        String password = data.get("password");

        if (email == null || password == null) {
            sendResponse(exchange, 400, "{\"error\":\"Email and password are required\"}");
            return;
        }

        try {
            String token = AuthService.login(email, password);
            if (token != null) {
                Integer userId = AuthService.getUserIdFromSession(token);
                User user = AuthService.getUserById(userId);
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("token", token);
                resp.put("role", user.getRoleValue());
                resp.put("nama", user.getNama());
                sendResponse(exchange, 200, JsonHelper.toJson(resp));
            } else {
                sendResponse(exchange, 401, "{\"error\":\"Invalid email or password\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = getBearerToken(exchange);
        if (token != null) {
            AuthService.logout(token);
        }
        sendResponse(exchange, 200, "{\"success\":true}");
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        String token = getBearerToken(exchange);
        Integer userId = AuthService.getUserIdFromSession(token);

        if (userId == null) {
            sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        try {
            User user = AuthService.getUserById(userId);
            if (user != null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("id", user.getId());
                resp.put("email", user.getEmail());
                resp.put("nama", user.getNama());
                resp.put("role", user.getRoleValue());
                if (user instanceof model.Pegawai) {
                    model.Pegawai p = (model.Pegawai) user;
                    resp.put("jabatan", p.getJabatan());
                    resp.put("divisi", p.getDivisi());
                }
                sendResponse(exchange, 200, JsonHelper.toJson(resp));
            } else {
                sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private String getBearerToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader; // fallback to raw
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
