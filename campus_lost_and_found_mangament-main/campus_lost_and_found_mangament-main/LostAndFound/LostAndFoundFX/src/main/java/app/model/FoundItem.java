package app.model;

public class FoundItem extends Item {

    public FoundItem(String title, String category, String location,
                     String description, String imagePath,
                     String reporterName, String phone, String email) {
        super(title, category, location, description,
                imagePath, reporterName, phone, email);
    }

    public FoundItem(int id, String title, String category, String location,
                     String description, String imagePath,
                     String reporterName, String phone, String email, String status) {
        super(id, title, category, location, description,
                imagePath, reporterName, phone, email, status);
    }

    @Override
    public String getType() {
        return "FOUND";
    }
}
