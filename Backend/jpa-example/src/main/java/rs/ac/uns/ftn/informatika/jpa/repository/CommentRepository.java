package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByUserIdAndCreatedTimeAfter(Long userId, LocalDateTime time);

    long countByCreatedTimeAfter(LocalDateTime date);

    @Query("SELECT DISTINCT c.user.id FROM Comment c")
    List<Long> findDistinctUserIds();
}
