package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.ShowUserDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;

import java.util.*;
import javax.transaction.Transactional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
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
    public UserService(UserRepository userRepository,PostRepository postRepository, PasswordEncoder passwordEncoder, EmailService emailService,UserDTOMapper userDTOMapper) {

        this.userRepository = userRepository;
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
    public List<ShowUserDTO> getAllUsers(Long adminId){
        List<User> users = userRepository.findAll();
        List<ShowUserDTO> showUserDTOs = new ArrayList<>();
        for (User user : users) {
            if(!user.getId().equals(adminId)) {
                long postCount = postRepository.countByUserId(user.getId());
                showUserDTOs.add(userDTOMapper.fromUserToDTO(user,postCount));
            }
        }
        return showUserDTOs;
    }
    public List<ShowUserDTO> filterUsers(Long adminId, Optional<String> name, Optional<String> surname, Optional<String> email,
                                         Optional<Integer> minPosts, Optional<Integer> maxPosts,
                                         Optional<String> sortField, Optional<String> sortOrder) {
        List<ShowUserDTO> showUserDTOs = getAllUsers(adminId);

        return showUserDTOs.stream()
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
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public Optional<ShowUserDTO> getShowUserById(Long id) {
        User user = userRepository.findById(id).get();
        long postCount = postRepository.countByUserId(user.getId());
        ShowUserDTO userToReturn = userDTOMapper.fromUserToDTO(user,postCount);
        return Optional.of(userToReturn);
    }
    //FOLLOWING LOGIC
    @Transactional
    public Optional<ShowUserDTO> followUser(Long userToFollow, Long userThatIsFollowing) {
        if (!canFollow(userThatIsFollowing)) {
            throw new IllegalStateException("Premasili ste limit od 50 pracenja po minuti. Pokusajte ponovo kasnije.");
        }
        Optional<User> followUserOpt = userRepository.findById(userToFollow);
        Optional<User> userWhoFollowsOpt = userRepository.findById(userThatIsFollowing);

        if (followUserOpt.isPresent() && userWhoFollowsOpt.isPresent()) {
            User followUser = followUserOpt.get();
            User userWhoFollows = userWhoFollowsOpt.get();

            if (followUser.getFollowers().contains(userWhoFollows)) {
                // Otprati korisnika
                followUser.getFollowers().remove(userWhoFollows);
                followUser.setFollowersCount(followUser.getFollowersCount() - 1);

                userWhoFollows.setNumberOfPeopleFollowing(userWhoFollows.getNumberOfPeopleFollowing() - 1);
            } else {
                // Zaprati korisnika
                followUser.getFollowers().add(userWhoFollows);
                followUser.setFollowersCount(followUser.getFollowersCount() + 1);

                userWhoFollows.setNumberOfPeopleFollowing(userWhoFollows.getNumberOfPeopleFollowing() + 1);
            }

            // Sacuvaj izmene za oba korisnika
            userRepository.save(followUser);
            userRepository.save(userWhoFollows);
            long postCount = postRepository.countByUserId(followUser.getId());
            ShowUserDTO userToReturn = userDTOMapper.fromUserToDTO(followUser,postCount);
            return Optional.of(userToReturn);
        }

        return Optional.empty();
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
