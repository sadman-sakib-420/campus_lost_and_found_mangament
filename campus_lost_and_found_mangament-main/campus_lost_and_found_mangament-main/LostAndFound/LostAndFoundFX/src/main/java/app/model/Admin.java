package app.model;

public class Admin extends User {

    public Admin(String username, String password) {
        super(0, username, password, "ADMIN");
    }

    public Admin(int id, String username, String password) {
        super(id, username, password, "ADMIN");
    }

    public void markItemReturned(Item item) {
        item.setStatus("RETURNED");
    }
}
