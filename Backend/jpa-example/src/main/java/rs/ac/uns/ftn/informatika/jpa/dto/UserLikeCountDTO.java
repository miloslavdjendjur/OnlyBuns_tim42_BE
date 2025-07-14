package rs.ac.uns.ftn.informatika.jpa.dto;

public class UserLikeCountDTO {
    private Long userId;
    private String username;
    private Long likeCount;

    public UserLikeCountDTO(Long userId, String username, Long likeCount) {
        this.userId = userId;
        this.username = username;
        this.likeCount = likeCount;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getLikeCount() { return likeCount; }
}