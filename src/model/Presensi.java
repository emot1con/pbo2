package model;

public class Presensi {
    private int id;
    private String tanggal;
    private String jamMasuk;
    private String jamKeluar;
    private String statusMasuk;
    private String statusPulang;
    private Pegawai pegawai;

    public static int totalPresensi = 0;

    public Presensi(int id, String tanggal, Pegawai pegawai) {
        this.id = id;
        this.tanggal = tanggal;
        this.pegawai = pegawai;
        totalPresensi++;
    }

    public int getId() {
        return id;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getJamMasuk() {
        return jamMasuk;
    }

    public void setJamMasuk(String jamMasuk) {
        this.jamMasuk = jamMasuk;
    }

    public String getJamKeluar() {
        return jamKeluar;
    }

    public void setJamKeluar(String jamKeluar) {
        this.jamKeluar = jamKeluar;
    }

    public String getStatusMasuk() {
        return statusMasuk;
    }

    public void setStatusMasuk(String statusMasuk) {
        this.statusMasuk = statusMasuk;
    }

    public String getStatusPulang() {
        return statusPulang;
    }

    public void setStatusPulang(String statusPulang) {
        this.statusPulang = statusPulang;
    }

    public Pegawai getPegawai() {
        return pegawai;
    }
}
