package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Additional query methods can be added here, if needed
    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :sevenDaysAgo")
    List<User> findInactiveUsers(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
    List<User> findByIsActiveFalse();

}

