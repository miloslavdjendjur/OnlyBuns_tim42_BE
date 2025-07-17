package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.CreateChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.MessageDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Chat;
import rs.ac.uns.ftn.informatika.jpa.model.Message;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.ChatRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.MessageRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;

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

    @Transactional
    public ChatDTO createChat(CreateChatDTO createChatDTO, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));

        Chat chat = new Chat();
        chat.setName(createChatDTO.getName());
        chat.setAdmin(creator);

        Set<User> participants = new HashSet<>();
        participants.add(creator);

        for (Long participantId : createChatDTO.getParticipantIds()) {
            if (!participantId.equals(creatorId)) {
                User participant = userRepository.findById(participantId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
                participants.add(participant);
            }
        }

        chat.setParticipants(participants);

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
        chatRepository.save(chat);
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

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(user -> user.getId().equals(userId));

        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant of this chat");
        }

        List<Message> messages = messageRepository.findByChatIdOrderByTimestampDesc(chatId);
        return messages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public List<MessageDTO> getLast10Messages(Long chatId) {
        List<Message> messages = messageRepository.findLast10MessagesByChatId(chatId);
        return messages.stream()
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

        int unreadCount = messageRepository.findUnreadMessages(chat.getId(), currentUserId).size();
        dto.setUnreadCount(unreadCount);

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