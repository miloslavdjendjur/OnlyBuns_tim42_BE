package rs.ac.uns.ftn.informatika.jpa.dto;

public class UserRemovedDTO {
    private Long chatId;
    private Long userId;
    private String reason;

    public UserRemovedDTO(Long chatId, Long userId, String reason) {
        this.chatId = chatId;
        this.userId = userId;
        this.reason = reason;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
