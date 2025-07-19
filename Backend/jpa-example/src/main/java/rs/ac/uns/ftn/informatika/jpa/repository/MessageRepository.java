package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.model.Message;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.isRead = false")
    List<Message> findUnreadMessages(Long chatId, Long userId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.timestamp >= :joinedAt ORDER BY m.timestamp DESC")
    List<Message> findMessagesAfterDate(@Param("chatId") Long chatId, @Param("joinedAt") LocalDateTime joinedAt);

    @Query(value = "SELECT * FROM messages WHERE chat_id = :chatId AND timestamp < :joinedAt ORDER BY timestamp DESC LIMIT 10", nativeQuery = true)
    List<Message> find10MessagesBeforeDate(@Param("chatId") Long chatId, @Param("joinedAt") LocalDateTime joinedAt);

   @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.isRead = false AND m.timestamp >= :joinedAt")
   List<Message> findUnreadMessagesAfterJoining(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("joinedAt") LocalDateTime joinedAt);

    @Query(value = "SELECT * FROM messages WHERE chat_id = :chatId AND sender_id != :userId AND is_read = false AND timestamp < :joinedAt ORDER BY timestamp DESC LIMIT 10", nativeQuery = true)
    List<Message> find10UnreadMessagesBeforeJoining(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("joinedAt") LocalDateTime joinedAt);
}
