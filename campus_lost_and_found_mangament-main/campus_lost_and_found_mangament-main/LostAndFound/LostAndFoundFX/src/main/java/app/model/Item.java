package app.model;

public abstract class Item {

    private int id;
    private String title;
    private String category;
    private String location;
    private String description;
    private String status;
    private String imagePath;

    // Contact info
    private String reporterName;
    private String phone;
    private String email;

    // Relational Foreign Key referencing users(id)
    private Integer userId;

    public Item(String title, String category, String location,
                String description, String imagePath,
                String reporterName, String phone, String email) {
        this(0, title, category, location, description, imagePath, reporterName, phone, email, "OPEN");
    }

    public Item(int id, String title, String category, String location,
                String description, String imagePath,
                String reporterName, String phone, String email, String status) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.location = location;
        this.description = description;
        this.imagePath = imagePath;
        this.reporterName = reporterName;
        this.phone = phone;
        this.email = email;
        this.status = (status != null && !status.isEmpty()) ? status : "OPEN";
    }

    public abstract String getType();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImagePath() { return imagePath; }

    // Contact getters (Admin only)
    public String getReporterName() { return reporterName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    // User relationship
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
