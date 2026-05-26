package service;

import model.Admin;
import model.Pegawai;
import model.User;
import util.DatabaseConnection;
import util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    // Session memory storage: Token -> User ID
    private static final Map<String, Integer> sessions = new HashMap<>();

    public static synchronized void seedAdmin() {
        String adminEmail = "admin@presensi.com";
        try (Connection conn = DatabaseConnection.getInstance()) {
            // Check if admin exists
            String selectSql = "SELECT id FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, adminEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Admin already seeded
                        return;
                    }
                }
            }

            // Seed default admin
            String insertSql = "INSERT INTO users (email, password_hash, role, nama) VALUES (?, ?, 'ADMIN', 'Administrator')";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, adminEmail);
                stmt.setString(2, PasswordHasher.hash("admin123"));
                stmt.executeUpdate();
                System.out.println("Default admin seeded: " + adminEmail + " / admin123");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding admin: " + e.getMessage());
        }
    }

    public static boolean register(String email, String password, String nama, String jabatan, String divisi) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance()) {
            // Check if email already exists
            String checkSql = "SELECT id FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return false; // Email already registered
                    }
                }
            }

            // Insert new user
            String insertSql = "INSERT INTO users (email, password_hash, role, nama, jabatan, divisi) VALUES (?, ?, 'PEGAWAI', ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, email);
                stmt.setString(2, PasswordHasher.hash(password));
                stmt.setString(3, nama);
                stmt.setString(4, jabatan);
                stmt.setString(5, divisi);
                stmt.executeUpdate();
                return true;
            }
        }
    }

    public static String login(String email, String password) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance()) {
            String sql = "SELECT id, password_hash FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String hash = rs.getString("password_hash");
                        if (PasswordHasher.verify(password, hash)) {
                            String token = UUID.randomUUID().toString();
                            sessions.put(token, id);
                            return token;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void logout(String token) {
        sessions.remove(token);
    }

    public static Integer getUserIdFromSession(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    public static User getUserById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance()) {
            String sql = "SELECT * FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String email = rs.getString("email");
                        String hash = rs.getString("password_hash");
                        String role = rs.getString("role");
                        String nama = rs.getString("nama");
                        if ("ADMIN".equals(role)) {
                            return new Admin(id, email, hash, nama);
                        } else {
                            String jabatan = rs.getString("jabatan");
                            String divisi = rs.getString("divisi");
                            return new Pegawai(id, email, hash, nama, jabatan, divisi);
                        }
                    }
                }
            }
        }
        return null;
    }
}
