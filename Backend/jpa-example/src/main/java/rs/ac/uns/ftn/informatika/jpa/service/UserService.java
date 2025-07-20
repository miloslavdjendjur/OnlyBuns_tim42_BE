package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.dto.PageDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.ShowUserDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.model.UserFollower;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserFollowerRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import javax.transaction.Transactional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserFollowerRepository userFollowerRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserDTOMapper userDTOMapper;
    private static final int FOLLOW_LIMIT = 50;
    private static final long ONE_MINUTE = 60 * 1000L;

    private final ConcurrentHashMap<Long, UserFollowTracker> followTracker = new ConcurrentHashMap<>();

    private static class UserFollowTracker {
        AtomicInteger count = new AtomicInteger(0);
        long timestamp = System.currentTimeMillis();
    }

    @Autowired
    public UserService(UserRepository userRepository, UserFollowerRepository userFollowerRepository,PostRepository postRepository, PasswordEncoder passwordEncoder, EmailService emailService,UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.userFollowerRepository = userFollowerRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userDTOMapper = userDTOMapper;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setFullName(userDetails.getFullName());
            user.setAddress(userDetails.getAddress());
            user.setRole(userDetails.getRole());
            return userRepository.save(user);
        });
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User registerUser(User user) {
        // Encrypt the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate verification token and set user as inactive
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setActive(false);

        // Set default values
        user.setLastLogin(LocalDateTime.now()); // Postavi trenutni datum i vreme
        user.setRole(User.Role.REGISTERED);

        // Save the user
        User savedUser = userRepository.save(user);

        // Send the activation email
        emailService.sendActivationEmail(user.getEmail(), token);

        return savedUser;
    }


    public int activateUser(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.getActive()) {
                user.setActive(true);
                user.setVerificationToken(null); // Clear token to prevent reuse
                userRepository.save(user);
                return 1; // Successfully activated
            } else {
                return -1; // User already active
            }
        } else {
            return 0; // Invalid token
        }
    }
    @Transactional
    public List<ShowUserDTO> getAllUsers(Long adminId) {
        List<User> users = userRepository.findAll();
        List<ShowUserDTO> showUserDTOs = new ArrayList<>();
        for (User user : users) {
            if (!user.getId().equals(adminId)) {
                long postCount = postRepository.countByUserId(user.getId());
                long followersCount = userFollowerRepository.countFollowers(user.getId());
                ShowUserDTO showUserDTO = userDTOMapper.fromUserToDTO(user, postCount, followersCount);
                showUserDTO.setFollowerIds(userFollowerRepository.getAllFollowerIds(user.getId()));
                showUserDTO.setFollowsPeople((int) userFollowerRepository.countHowManyPeopleFollowing(user.getId()));
                showUserDTOs.add(showUserDTO);
            }
        }
        return showUserDTOs;
    }

    public PageDTO<ShowUserDTO> filterUsersWithPagination(Long adminId, Optional<String> name,
                                                          Optional<String> surname, Optional<String> email,
                                                          Optional<Integer> minPosts, Optional<Integer> maxPosts,
                                                          Optional<String> sortField, Optional<String> sortOrder,
                                                          int page, int size) {
        List<ShowUserDTO> allUsers = getAllUsers(adminId);

        List<ShowUserDTO> filteredUsers = allUsers.stream()
                .filter(user -> name.map(n -> user.getFullName().toLowerCase().contains(n.toLowerCase())).orElse(true))
                .filter(user -> surname.map(s -> user.getFullName().toLowerCase().contains(s.toLowerCase())).orElse(true))
                .filter(user -> email.map(e -> user.getEmail().toLowerCase().contains(e.toLowerCase())).orElse(true))
                .filter(user -> minPosts.map(min -> user.getPostNumber() >= min).orElse(true))
                .filter(user -> maxPosts.map(max -> user.getPostNumber() <= max).orElse(true))
                .sorted((u1, u2) -> {
                    if (!sortField.isPresent()) return 0;
                    int direction = sortOrder.orElse("asc").equalsIgnoreCase("asc") ? 1 : -1;

                    switch (sortField.get().toLowerCase()) {
                        case "followers":
                            return direction * Long.compare(u1.getFollowsPeople(), u2.getFollowsPeople());
                        case "email":
                            return direction * u1.getEmail().compareToIgnoreCase(u2.getEmail());
                        default:
                            return 0;
                    }
                })
                .collect(Collectors.toList());

        // Paginacija
        int totalElements = filteredUsers.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ShowUserDTO> paginatedUsers = filteredUsers.subList(fromIndex, toIndex);

        return new PageDTO<>(paginatedUsers, page, size, totalElements);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public Optional<ShowUserDTO> getShowUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        long postCount = postRepository.countByUserId(user.getId());
        long followersCount = userFollowerRepository.countFollowers(user.getId());
        ShowUserDTO userToReturn = userDTOMapper.fromUserToDTO(user, postCount, followersCount);
        return Optional.of(userToReturn);
    }


    // FOLLOW
    @Transactional
    public Optional<ShowUserDTO> followUser(Long userToFollow, Long userThatIsFollowing) {
        if (!canFollow(userThatIsFollowing)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Follow limit of 50 per minute reached. Please try again later.");
        }

        Optional<User> followUserOpt = userRepository.findById(userToFollow);
        Optional<User> userWhoFollowsOpt = userRepository.findById(userThatIsFollowing);

        if (followUserOpt.isPresent() && userWhoFollowsOpt.isPresent()) {
            User followUser = followUserOpt.get();
            User userWhoFollows = userWhoFollowsOpt.get();

            boolean alreadyFollowing = userFollowerRepository.existsByUserIdAndFollowerId(userToFollow, userThatIsFollowing);

            if (alreadyFollowing) {
                userFollowerRepository.deleteByUserIdAndFollowerId(userToFollow, userThatIsFollowing);
            } else {
                UserFollower userFollower = new UserFollower();
                userFollower.setUser(followUser);
                userFollower.setFollower(userWhoFollows);
                userFollower.setFollowedSince(LocalDateTime.now());
                userFollowerRepository.save(userFollower);
            }

            long postCount = postRepository.countByUserId(followUser.getId());
            long followersCount = userFollowerRepository.countFollowers(followUser.getId());
            ShowUserDTO userToReturn = userDTOMapper.fromUserToDTO(followUser, postCount, followersCount);
            userToReturn.setFollowerIds(userFollowerRepository.getAllFollowerIds(userToFollow));
            return Optional.of(userToReturn);
        }

        throw new RuntimeException("User not found");
    }

    @Transactional
    public Optional<ShowUserDTO> followUserById(Long userToFollow, Long userThatIsFollowing) {
        if (userToFollow.equals(userThatIsFollowing)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself.");
        }

        if (!canFollow(userThatIsFollowing)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only follow 50 times per minute.");
        }

        Optional<User> followUserOpt = userRepository.findById(userToFollow);
        Optional<User> userWhoFollowsOpt = userRepository.findById(userThatIsFollowing);

        if (followUserOpt.isPresent() && userWhoFollowsOpt.isPresent()) {
            User followUser = followUserOpt.get();
            User userWhoFollows = userWhoFollowsOpt.get();

            // Check if the user already follows the target
            boolean alreadyFollowing = userFollowerRepository.existsByUserIdAndFollowerId(userToFollow, userThatIsFollowing);

            if (alreadyFollowing) {
                // Unfollow logic
                userFollowerRepository.deleteByUserIdAndFollowerId(userToFollow, userThatIsFollowing);
            } else {
                // Follow logic
                UserFollower userFollower = new UserFollower();
                userFollower.setUser(followUser);
                userFollower.setFollower(userWhoFollows);
                userFollower.setFollowedSince(LocalDateTime.now());
                userFollowerRepository.save(userFollower);
            }

            long postCount = postRepository.countByUserId(followUser.getId());
            long followersCount = userFollowerRepository.countFollowers(followUser.getId());
            ShowUserDTO userToReturn = userDTOMapper.fromUserToDTO(followUser, postCount, followersCount);
            return Optional.of(userToReturn);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
    }


    private boolean canFollow(Long userId) {
        UserFollowTracker tracker = followTracker.computeIfAbsent(userId, id -> new UserFollowTracker());

        synchronized (tracker) {
            long currentTime = System.currentTimeMillis();

            // Resetujte broj zahteva ako je proslo vise od jednog minuta
            if (currentTime - tracker.timestamp > ONE_MINUTE) {
                tracker.timestamp = currentTime;
                tracker.count.set(0);
            }

            // Proverite da li je korisnik premasio limit
            if (tracker.count.incrementAndGet() > FOLLOW_LIMIT) {
                return false;
            }

            return true;
        }
    }
    @Scheduled(fixedRate = 60000) // 60 000 milisekundi
    public void cleanUpFollowTracker() {
        long currentTime = System.currentTimeMillis();
        followTracker.entrySet().removeIf(entry -> currentTime - entry.getValue().timestamp > ONE_MINUTE);
    }

    @Scheduled(cron = "0 0 8 * * ?") // Svakog dana u 8:00
    @Transactional
    public void sendWeeklySummaries() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<User> inactiveUsers = userRepository.findAll().stream()
                .filter(user -> user.getLastLogin() == null || user.getLastLogin().isBefore(sevenDaysAgo))
                .collect(Collectors.toList());

        for (User user : inactiveUsers) {
            String summary = generateWeeklySummary(user);
            emailService.sendWeeklySummary(user.getEmail(), summary);
        }
    }

    private String generateWeeklySummary(User user) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Count new followers in the last 7 days
        long newFollowers = userFollowerRepository.countNewFollowersSince(user.getId(), sevenDaysAgo);

        // Count new likes in the last 7 days
        long newLikes = postRepository.countLikesInLast7Days(user.getId(), sevenDaysAgo);

        // Count new posts in the last 7 days
        long newPosts = postRepository.countByUserIdAndCreatedTimeAfter(user.getId(), sevenDaysAgo);

        return String.format(
                "Dear %s,\n\nHere is your weekly summary:\n- New followers: %d\n- New likes: %d\n- New posts: %d\n\nLog in to check more updates!",
                user.getFullName(), newFollowers, newLikes, newPosts
        );
    }
    //Brisanje svih neaktivnih usera na kraju meseca
    // 0 h / 0 min / 0 sec / od 28-31 u mesecu / * - svaki mesec / ? - bilo koji dan u nedelji
    @Scheduled(cron = "0 0 0 28-31 * ?")
    @Transactional
    public void deleteInactiveUsers() {
        // Da li je poslednji dan u mesecu
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (today != lastDay) {
            return; // Nije poslednji dan
        }

        // Neaktivirani korisnici
        List<User> inactiveUsers = userRepository.findByIsActiveFalse();

        // Brisanje svih neaktivnih
        for (User user : inactiveUsers) {
            userRepository.delete(user);
            System.out.println("Deleted inactive user: " + user.getEmail());
        }

        if (!inactiveUsers.isEmpty()) {
            System.out.println("Total deleted inactive users: " + inactiveUsers.size());
        }
    }


}
