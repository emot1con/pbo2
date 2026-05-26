package service;

import model.Pegawai;
import util.DatabaseConnection;
import util.EnvLoader;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresensiService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static boolean isValidIp(String clientIp) {
        String allowedIp = EnvLoader.get("ALLOWED_IP", "127.0.0.1");
        // Normalize IPv6 loopback to IPv4 loopback
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }
        return allowedIp.equals(clientIp);
    }

    public static Map<String, Object> absenDatang(int userId, String clientIp) throws Exception {
        if (!isValidIp(clientIp)) {
            throw new IllegalArgumentException("Anda tidak berada di area kantor (IP Anda: " + clientIp + ")");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String todayStr = today.toString();
        String nowStr = now.format(TIME_FORMATTER);

        try (Connection conn = DatabaseConnection.getInstance()) {
            // Check if already checked in today
            String checkSql = "SELECT id FROM presensi WHERE user_id = ? AND tanggal = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, java.sql.Date.valueOf(today));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException("Anda sudah melakukan absen datang hari ini.");
                    }
                }
            }

            // Determine status
            LocalTime limit = LocalTime.of(12, 0); // 08:00
            String status = now.isAfter(limit) ? "TERLAMBAT" : "TEPAT_WAKTU";

            // Insert attendance
            String insertSql = "INSERT INTO presensi (user_id, tanggal, jam_masuk, status_masuk) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, java.sql.Date.valueOf(today));
                stmt.setTime(3, Time.valueOf(now));
                stmt.setString(4, status);
                stmt.executeUpdate();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("jam_masuk", nowStr);
            result.put("status_masuk", status);
            return result;
        }
    }

    public static Map<String, Object> absenPulang(int userId, String clientIp) throws Exception {
        if (!isValidIp(clientIp)) {
            throw new IllegalArgumentException("Anda tidak berada di area kantor (IP Anda: " + clientIp + ")");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String todayStr = today.toString();
        String nowStr = now.format(TIME_FORMATTER);

        // Check if now is before 15:00
        LocalTime pulangLimit = LocalTime.of(15, 0); // 15:00
        if (now.isBefore(pulangLimit)) {
            throw new IllegalStateException("Belum waktunya pulang. Absen pulang baru bisa dilakukan mulai jam 15:00.");
        }

        try (Connection conn = DatabaseConnection.getInstance()) {
            // Check if checked in today
            String checkSql = "SELECT id, jam_keluar FROM presensi WHERE user_id = ? AND tanggal = ?";
            boolean hasRecord = false;
            String existingJamKeluar = null;

            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, java.sql.Date.valueOf(today));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        hasRecord = true;
                        Time t = rs.getTime("jam_keluar");
                        if (t != null) {
                            existingJamKeluar = t.toString();
                        }
                    }
                }
            }

            if (!hasRecord) {
                throw new IllegalStateException("Anda harus melakukan absen datang terlebih dahulu.");
            }

            if (existingJamKeluar != null) {
                throw new IllegalStateException("Anda sudah melakukan absen pulang hari ini.");
            }

            // Update attendance
            String updateSql = "UPDATE presensi SET jam_keluar = ?, status_pulang = 'TEPAT_WAKTU' WHERE user_id = ? AND tanggal = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setTime(1, Time.valueOf(now));
                stmt.setInt(2, userId);
                stmt.setDate(3, java.sql.Date.valueOf(today));
                stmt.executeUpdate();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("jam_keluar", nowStr);
            result.put("status_pulang", "TEPAT_WAKTU");
            return result;
        }
    }

    public static Map<String, Object> getStatusHariIni(int userId) throws SQLException {
        LocalDate today = LocalDate.now();
        Map<String, Object> result = new HashMap<>();
        result.put("sudah_datang", false);
        result.put("sudah_pulang", false);
        result.put("jam_masuk", null);
        result.put("jam_keluar", null);
        result.put("status_masuk", null);
        result.put("status_pulang", null);

        try (Connection conn = DatabaseConnection.getInstance()) {
            String sql = "SELECT jam_masuk, jam_keluar, status_masuk, status_pulang FROM presensi WHERE user_id = ? AND tanggal = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, java.sql.Date.valueOf(today));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result.put("sudah_datang", true);
                        Time m = rs.getTime("jam_masuk");
                        if (m != null) {
                            result.put("jam_masuk", m.toString());
                        }
                        result.put("status_masuk", rs.getString("status_masuk"));

                        Time k = rs.getTime("jam_keluar");
                        if (k != null) {
                            result.put("sudah_pulang", true);
                            result.put("jam_keluar", k.toString());
                            result.put("status_pulang", rs.getString("status_pulang"));
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Map<String, Object>> getSemuaPresensiHariIni() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        try (Connection conn = DatabaseConnection.getInstance()) {
            String sql = "SELECT u.nama, u.jabatan, u.divisi, p.jam_masuk, p.jam_keluar, p.status_masuk, p.status_pulang "
                    +
                    "FROM users u " +
                    "JOIN presensi p ON u.id = p.user_id " +
                    "WHERE p.tanggal = ? " +
                    "ORDER BY p.jam_masuk DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, java.sql.Date.valueOf(today));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("nama", rs.getString("nama"));
                        map.put("jabatan", rs.getString("jabatan"));
                        map.put("divisi", rs.getString("divisi"));

                        Time m = rs.getTime("jam_masuk");
                        map.put("jam_masuk", m != null ? m.toString() : "-");
                        map.put("status_masuk", rs.getString("status_masuk"));

                        Time k = rs.getTime("jam_keluar");
                        map.put("jam_keluar", k != null ? k.toString() : "-");
                        map.put("status_pulang",
                                rs.getString("status_pulang") != null ? rs.getString("status_pulang") : "-");

                        list.add(map);
                    }
                }
            }
        }
        return list;
    }
}
