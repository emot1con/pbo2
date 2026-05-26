package model;

public class Pegawai extends User {
    private String jabatan;
    private String divisi;

    public static int totalPegawai = 0;

    public Pegawai(int id, String email, String passwordHash, String nama, String jabatan, String divisi) {
        super(id, email, passwordHash, "PEGAWAI", nama);
        this.jabatan = jabatan;
        this.divisi = divisi;
        totalPegawai++;
    }

    @Override
    public String getRole() {
        return "PEGAWAI";
    }

    public String getJabatan() {
        return jabatan;
    }

    public String getDivisi() {
        return divisi;
    }
}
