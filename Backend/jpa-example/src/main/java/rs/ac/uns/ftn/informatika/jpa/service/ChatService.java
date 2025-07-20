package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.CreateChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.MessageDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserRemovedDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Chat;
import rs.ac.uns.ftn.informatika.jpa.model.ChatParticipant;
import rs.ac.uns.ftn.informatika.jpa.model.Message;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.ChatRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.MessageRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatDTO createChat(CreateChatDTO createChatDTO, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));

        Chat chat = new Chat();
        chat.setName(createChatDTO.getName());
        chat.setAdmin(creator);

        Set<User> participants = new HashSet<>();
        Set<ChatParticipant> chatParticipants = new HashSet<>();
        participants.add(creator);
        chatParticipants.add(new ChatParticipant(chat,creator));

        for (Long participantId : createChatDTO.getParticipantIds()) {
            if (!participantId.equals(creatorId)) {
                User participant = userRepository.findById(participantId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
                participants.add(participant);
                chatParticipants.add(new ChatParticipant(chat,participant));
            }
        }

        chat.setParticipants(participants);
        chat.setChatParticipants(chatParticipants);

        Chat savedChat = chatRepository.save(chat);
        return convertToChatDTO(savedChat, creatorId);
    }

    @Transactional
    public void addUserToGroupChat(Long chatId, Long userId, Long adminId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        if (!chat.getAdmin().getId().equals(adminId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can add users");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        chat.getParticipants().add(userToAdd);
        chat.getChatParticipants().add(new ChatParticipant(chat,userToAdd));
        chatRepository.save(chat);
    }

    @Transactional
    public void removeUserFromGroupChat(Long chatId, Long userId, Long adminId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        if (!chat.getAdmin().getId().equals(adminId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can remove users");
        }

        chat.getParticipants().removeIf(user -> user.getId().equals(userId));
        chat.getChatParticipants().removeIf(cp -> cp.getUser().getId().equals(userId));
        chatRepository.save(chat);

        UserRemovedDTO notification = new UserRemovedDTO(chatId, userId, "You have been removed from the chat");
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/removed", notification);
    }
    @Transactional
    public List<ChatDTO> getUserChats(Long userId) {
        List<Chat> chats = chatRepository.findChatsByUserId(userId);
        return chats.stream()
                .map(chat -> convertToChatDTO(chat, userId))
                .collect(Collectors.toList());
    }
    @Transactional
    public List<MessageDTO> getChatMessages(Long chatId, Long userId) {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));


        ChatParticipant participant = chat.getChatParticipants().stream()
                .filter(cp -> cp.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant"));

        LocalDateTime joinedAt = participant.getJoinedAt();

        // 10 poruka pre nego sto je usao, i ostale poruke
        List<Message> messagesBefore = messageRepository.find10MessagesBeforeDate(chatId, joinedAt);
        List<Message> messagesAfter = messageRepository.findMessagesAfterDate(chatId, joinedAt);

        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(messagesBefore);
        allMessages.addAll(messagesAfter);

        allMessages.sort(Comparator.comparing(Message::getTimestamp));

        return allMessages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }

    private ChatDTO convertToChatDTO(Chat chat, Long currentUserId) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setName(chat.getName());
        dto.setAdminId(chat.getAdmin() != null ? chat.getAdmin().getId() : null);
        dto.setCreatedAt(chat.getCreatedAt());

        dto.setParticipantIds(chat.getParticipants().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));

        List<Message> messages = new ArrayList<>(chat.getMessages());
        if (!messages.isEmpty()) {
            messages.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
            dto.setLastMessage(convertToMessageDTO(messages.get(0)));
        }

       // int unreadCount = messageRepository.findUnreadMessages(chat.getId(), currentUserId).size();
       // dto.setUnreadCount(unreadCount);
        ChatParticipant participant = chat.getChatParticipants().stream()
                .filter(cp -> cp.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        if (participant != null) {
            LocalDateTime joinedAt = participant.getJoinedAt();

            List<Message> unreadBefore = messageRepository.find10UnreadMessagesBeforeJoining(chat.getId(), currentUserId, joinedAt);
            List<Message> unreadAfter = messageRepository.findUnreadMessagesAfterJoining(chat.getId(), currentUserId, joinedAt);

            int unreadCount = unreadBefore.size() + unreadAfter.size();
            dto.setUnreadCount(unreadCount);
        } else {
            dto.setUnreadCount(0);
        }

        return dto;
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