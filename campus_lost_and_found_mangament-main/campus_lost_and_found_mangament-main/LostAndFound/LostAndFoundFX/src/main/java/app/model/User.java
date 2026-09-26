package app.model;

public class User {

    private int id;
    private String username;
    private String password;
    private String role;

    public User(String username, String password) {
        this(0, username, password, "USER");
    }

    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = (role != null) ? role : "USER";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean login(String enteredPassword) {
        return password != null && password.equals(enteredPassword);
    }
}
