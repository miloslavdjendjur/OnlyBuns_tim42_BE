package rs.ac.uns.ftn.informatika.jpa.dto;
import java.time.LocalDateTime;

public class AdPostMessage {
    private String description;
    private LocalDateTime publishedAt;
    private String username;

    public AdPostMessage() { }

    public AdPostMessage(String description, LocalDateTime publishedAt, String username) {
        this.description = description;
        this.publishedAt = publishedAt;
        this.username = username;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
