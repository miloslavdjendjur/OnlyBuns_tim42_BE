package rs.ac.uns.ftn.informatika.jpa.dto;

public class PostMapDTO {
    private Long id;
    private String description;
    private String username;
    private double latitude;
    private double longitude;

    public PostMapDTO() {}

    public PostMapDTO(Long id, String description, String username, double latitude, double longitude) {
        this.id = id;
        this.description = description;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
