package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.AnalyticsDTO;
import rs.ac.uns.ftn.informatika.jpa.repository.CommentRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AnalyticsService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AnalyticsService(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AnalyticsDTO getAnalytics() {
        AnalyticsDTO analytics = new AnalyticsDTO();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime oneYearAgo = now.minusYears(1);
        LocalDateTime oneDayAgo = now.minusDays(1);

        // Objave
        analytics.setWeeklyPosts(postRepository.countByCreatedTimeAfter(oneWeekAgo));
        analytics.setMonthlyPosts(postRepository.countByCreatedTimeAfter(oneMonthAgo));
        analytics.setYearlyPosts(postRepository.countByCreatedTimeAfter(oneYearAgo));

        // Komentari
        analytics.setWeeklyComments(commentRepository.countByCreatedTimeAfter(oneWeekAgo));
        analytics.setMonthlyComments(commentRepository.countByCreatedTimeAfter(oneMonthAgo));
        analytics.setYearlyComments(commentRepository.countByCreatedTimeAfter(oneYearAgo));

        calculateUserActivityPercentages(analytics);

        // Dodatni podaci
        analytics.setTotalUsers(userRepository.count());
        analytics.setDailyActiveUsers(userRepository.countByLastLoginAfter(oneDayAgo));
        analytics.setNewPostsToday(postRepository.countByCreatedTimeAfter(oneDayAgo));

        // Prosecno vreme sesije ( za sada placeholder - ako budem imao vremena odradicu sa logovima)
        analytics.setAvgSessionTime(24.5);

        // Engagement rate (odnos aktivnih korisnika prema ukupnom broju)
        double engagementRate = (analytics.getDailyActiveUsers() * 100.0) / analytics.getTotalUsers();
        analytics.setEngagementRate(Math.round(engagementRate * 10.0) / 10.0);

        return analytics;
    }

    private void calculateUserActivityPercentages(AnalyticsDTO analytics) {
        long totalUsers = userRepository.count();
        if (totalUsers == 0) {
            analytics.setPercentUsersWithPosts(0);
            analytics.setPercentUsersWithCommentsOnly(0);
            analytics.setPercentInactiveUsers(0);
            return;
        }

        // Korisnici koji su napravili bar jedan post
        Set<Long> usersWithPosts = new HashSet<>(postRepository.findDistinctUserIds());

        // Korisnici sa bar jednim komentarom
        Set<Long> usersWithComments = new HashSet<>(commentRepository.findDistinctUserIds());

        // Korisnici koji su napravili komentar (a ne post)
        Set<Long> usersWithCommentsOnly = new HashSet<>(usersWithComments);
        usersWithCommentsOnly.removeAll(usersWithPosts);

        // Korisnici koji nisu napravili ni post ni komentar
        long inactiveUsers = totalUsers - usersWithPosts.size() - usersWithCommentsOnly.size();

        // Procenti za grafik
        double percentWithPosts = (usersWithPosts.size() * 100.0) / totalUsers;
        double percentWithCommentsOnly = (usersWithCommentsOnly.size() * 100.0) / totalUsers;
        double percentInactive = (inactiveUsers * 100.0) / totalUsers;

        // Zaokruzivanje na jednu decimalu
        analytics.setPercentUsersWithPosts(Math.round(percentWithPosts * 10.0) / 10.0);
        analytics.setPercentUsersWithCommentsOnly(Math.round(percentWithCommentsOnly * 10.0) / 10.0);
        analytics.setPercentInactiveUsers(Math.round(percentInactive * 10.0) / 10.0);
    }
}