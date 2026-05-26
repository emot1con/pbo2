package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.AuthService;
import service.PresensiService;
import util.JsonHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PresensiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Authentication & Authorization Check
        String token = getBearerToken(exchange);
        Integer userId = AuthService.getUserIdFromSession(token);

        if (userId == null) {
            sendResponse(exchange, 401, "{\"error\":\"Unauthorized. Token invalid atau kedaluwarsa.\"}");
            return;
        }

        try {
            User user = AuthService.getUserById(userId);
            if (user == null || !"PEGAWAI".equals(user.getRoleValue())) {
                sendResponse(exchange, 403, "{\"error\":\"Hanya Pegawai yang dapat mengakses endpoint presensi.\"}");
                return;
            }

            // Get client IP
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

            if ("/api/presensi/datang".equals(path) && "POST".equalsIgnoreCase(method)) {
                try {
                    Map<String, Object> result = PresensiService.absenDatang(userId, clientIp);
                    sendResponse(exchange, 200, JsonHelper.toJson(result));
                } catch (IllegalArgumentException | IllegalStateException e) {
                    sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else if ("/api/presensi/pulang".equals(path) && "POST".equalsIgnoreCase(method)) {
                try {
                    Map<String, Object> result = PresensiService.absenPulang(userId, clientIp);
                    sendResponse(exchange, 200, JsonHelper.toJson(result));
                } catch (IllegalArgumentException | IllegalStateException e) {
                    sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else if ("/api/presensi/status".equals(path) && "GET".equalsIgnoreCase(method)) {
                Map<String, Object> result = PresensiService.getStatusHariIni(userId);
                sendResponse(exchange, 200, JsonHelper.toJson(result));
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private String getBearerToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader;
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
