package rs.ac.uns.ftn.informatika.jpa.dto;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderUsername;
    private Long chatId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    public MessageDTO() {}

    public MessageDTO(Long id, Long senderId, String senderName, String senderUsername,
                      Long chatId, String content, LocalDateTime timestamp, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderUsername = senderUsername;
        this.chatId = chatId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}