package model;

public abstract class User {
    protected int id;
    protected String email;
    protected String passwordHash;
    protected String role;
    protected String nama;

    public static int jumlahUser = 0;

    public User(int id, String email, String passwordHash, String role, String nama) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.nama = nama;
        jumlahUser++;
    }

    public abstract String getRole();

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNama() {
        return nama;
    }

    public String getRoleValue() {
        return role;
    }
}
