package model;

public class Admin extends User {
    public Admin(int id, String email, String passwordHash, String nama) {
        super(id, email, passwordHash, "ADMIN", nama);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
