package rs.ac.uns.ftn.informatika.jpa.dto;

import java.time.LocalDateTime;

public class PostDetailDTO {
    private Long id;
    private String description;
    private String imagePath;
    private Long userId;
    private String username;
    private Double latitude;
    private Double longitude;
    private String address;
    private LocalDateTime createdDate;
    private int likes;

    public PostDetailDTO() {}

    public PostDetailDTO(Long id, String description, String imagePath, Long userId,
                         String username, Double latitude, Double longitude,
                         String address, LocalDateTime createdDate, int likes) {
        this.id = id;
        this.description = description;
        this.imagePath = imagePath;
        this.userId = userId;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.createdDate = createdDate;
        this.likes = likes;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}