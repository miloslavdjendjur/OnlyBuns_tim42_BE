package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.model.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp DESC")
    List<Message> findByChatIdOrderByTimestampDesc(Long chatId);

    @Query(value = "SELECT * FROM messages WHERE chat_id = :chatId ORDER BY timestamp DESC LIMIT 10", nativeQuery = true)
    List<Message> findLast10MessagesByChatId(Long chatId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.isRead = false")
    List<Message> findUnreadMessages(Long chatId, Long userId);
}
