package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.model.UserFollower;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserFollowerRepository extends JpaRepository<UserFollower, Long> {

    @Query("SELECT COUNT(uf) > 0 FROM UserFollower uf WHERE uf.user.id = :userId AND uf.follower.id = :followerId")
    boolean existsByUserIdAndFollowerId(@Param("userId") Long userId, @Param("followerId") Long followerId);

    void deleteByUserIdAndFollowerId(Long userId, Long followerId);

    @Query("SELECT COUNT(uf) FROM UserFollower uf WHERE uf.user.id = :userId")
    long countFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(uf) FROM UserFollower uf WHERE uf.follower.id = :userId")
    long countFollowing(@Param("userId") Long userId);

    @Query("SELECT COUNT(uf) FROM UserFollower uf WHERE uf.user.id = :userId AND uf.followedSince > :since")
    long countNewFollowersSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT uf.follower.id FROM UserFollower uf WHERE uf.user.id = :userId")
    List<Long> getAllFollowerIds(@Param("userId") Long userId);

    @Query("SELECT COUNT(uf) FROM UserFollower uf WHERE uf.follower.id = :userId")
    long countHowManyPeopleFollowing(@Param("userId") Long userId);
}