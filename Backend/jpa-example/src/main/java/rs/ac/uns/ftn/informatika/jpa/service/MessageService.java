package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.dto.MessageDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Chat;
import rs.ac.uns.ftn.informatika.jpa.model.ChatParticipant;
import rs.ac.uns.ftn.informatika.jpa.model.Message;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.ChatRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.MessageRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageDTO sendMessage(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(user -> user.getId().equals(senderId));

        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant of this chat");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);
        MessageDTO messageDTO = convertToMessageDTO(savedMessage);

        // WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, messageDTO);

        return messageDTO;
    }

    @Transactional
    public void markMessagesAsRead(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));
        ChatParticipant participant = chat.getChatParticipants().stream()
                .filter(cp -> cp.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);

        List<Message> unreadBefore = new ArrayList<>();
        List<Message> unreadAfter = new ArrayList<>();
        if (participant != null){
            LocalDateTime joinedAt = participant.getJoinedAt();
            unreadBefore = messageRepository.find10UnreadMessagesBeforeJoining(chat.getId(), userId, joinedAt);
            unreadAfter = messageRepository.findUnreadMessagesAfterJoining(chat.getId(), userId, joinedAt);
        }
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(unreadBefore);
        allMessages.addAll(unreadAfter);
        allMessages.forEach(message -> message.setRead(true));

       // List<Message> unreadMessages = messageRepository.findUnreadMessages(chatId, userId);
        //unreadMessages.forEach(message -> message.setRead(true));
        messageRepository.saveAll(allMessages);
    }

    private MessageDTO convertToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setChatId(message.getChat().getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        return dto;
    }
}
