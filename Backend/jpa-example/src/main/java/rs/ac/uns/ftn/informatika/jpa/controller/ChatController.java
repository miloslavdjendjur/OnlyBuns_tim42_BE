package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.CreateChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.MessageDTO;
import rs.ac.uns.ftn.informatika.jpa.service.ChatService;
import rs.ac.uns.ftn.informatika.jpa.service.MessageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    @PostMapping("/create/{userId}")
    public ResponseEntity<ChatDTO> createChat(@PathVariable Long userId, @RequestBody CreateChatDTO createChatDTO) {
        ChatDTO chat = chatService.createChat(createChatDTO, userId);
        return ResponseEntity.ok(chat);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatDTO>> getUserChats(@PathVariable Long userId) {
        List<ChatDTO> chats = chatService.getUserChats(userId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{chatId}/messages/{userId}")
    public ResponseEntity<List<MessageDTO>> getChatMessages(@PathVariable Long chatId, @PathVariable Long userId) {
        List<MessageDTO> messages = chatService.getChatMessages(chatId, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{chatId}/send/{senderId}")
    public ResponseEntity<MessageDTO> sendMessage(@PathVariable Long chatId, @PathVariable Long senderId, @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        MessageDTO message = messageService.sendMessage(chatId, senderId, content);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{chatId}/add-user/{adminId}")
    public ResponseEntity<Void> addUserToGroup(@PathVariable Long chatId, @PathVariable Long adminId, @RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        chatService.addUserToGroupChat(chatId, userId, adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{chatId}/remove-user/{adminId}")
    public ResponseEntity<Void> removeUserFromGroup(@PathVariable Long chatId, @PathVariable Long adminId, @RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        chatService.removeUserFromGroupChat(chatId, userId, adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{chatId}/mark-read/{userId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long chatId, @PathVariable Long userId) {
        messageService.markMessagesAsRead(chatId, userId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(@Payload Map<String, Object> payload) {
        Long chatId = Long.valueOf(payload.get("chatId").toString());
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String content = payload.get("content").toString();

        messageService.sendMessage(chatId, senderId, content);
    }
}
