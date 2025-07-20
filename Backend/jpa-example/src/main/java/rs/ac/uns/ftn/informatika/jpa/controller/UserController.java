package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.dto.*;
import rs.ac.uns.ftn.informatika.jpa.mapper.PostMapper;
import rs.ac.uns.ftn.informatika.jpa.model.CustomUserDetails;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.service.BloomFilterService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    @Autowired
    private BloomFilterService bloomFilterService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userService.updateUser(id, userDetails)
                .map(updatedUser -> new ResponseEntity<>(updatedUser, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/all/{id}")
    public ResponseEntity<List<ShowUserDTO>> getAllUsers(@PathVariable Long id) {
        List<ShowUserDTO> showUserDTOs = userService.getAllUsers(id);
        return ResponseEntity.ok(showUserDTOs);
    }
    @PostMapping("/filter/{id}")
    public ResponseEntity<List<ShowUserDTO>> filterUsers(@PathVariable Long id, @RequestBody FilterCriteriaDTO criteria) {

        List<ShowUserDTO> filteredUsers = userService.filterUsers
        (id,Optional.ofNullable(criteria.getName()),
                Optional.ofNullable(criteria.getSurname()),
                Optional.ofNullable(criteria.getEmail()),
                Optional.ofNullable(criteria.getMinPosts()),
                Optional.ofNullable(criteria.getMaxPosts()),
                Optional.ofNullable(criteria.getSortField()),
                Optional.ofNullable(criteria.getSortOrder()));
        return ResponseEntity.ok(filteredUsers);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<ShowUserDTO> getShowUserById(@PathVariable Long id) {
        return userService.getShowUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/followUser/{id}")
    public ResponseEntity<ShowUserDTO> followUser(@PathVariable Long id,@RequestBody ShowUserDTO userToFollow) {
        return userService.followUser(userToFollow.getId(),id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/followUserId/{userThatFollows}")
    public ResponseEntity<ShowUserDTO> followUserById(
            @PathVariable Long userThatFollows,
            @RequestBody Map<String, Long> payload // Expect userToFollow as a key in the payload
    ) {
        Long userToFollow = payload.get("userToFollow");
        if (userToFollow == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User to follow is required.");
        }

        return userService.followUserById(userToFollow, userThatFollows)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/sendWeeklySummaries")
    public ResponseEntity<String> sendWeeklySummaries() {
        System.out.println("sendWeeklySummaries called");
        userService.sendWeeklySummaries();
        return ResponseEntity.ok("Weekly summaries sent successfully.");
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileFullDTO> getMyProfile(@AuthenticationPrincipal Object principal) {
        if (!(principal instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        User user = userDetails.getUser();
        return ResponseEntity.ok(new UserProfileFullDTO(user, new ArrayList<>()));
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean maybeExists = bloomFilterService.maybeUsernameExists(username);
        return ResponseEntity.ok(maybeExists);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfileFullDTO> getProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal) {

        if (!(principal instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long loggedInUserId = userDetails.getUser().getId();

        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<PostDetailDTO> posts = postRepository.findByUserId(id)
                .stream()
                .map(PostMapper::toPostDetailDTO)
                .collect(Collectors.toList());

        UserProfileFullDTO dto = new UserProfileFullDTO(user, posts);
        dto.setEditable(loggedInUserId.equals(id));

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordDTO dto, @AuthenticationPrincipal Object principal) {
        if (!(principal instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        userService.changePassword(userDetails.getUser(), dto.getNewPassword());
        return ResponseEntity.ok().build();
    }





}
