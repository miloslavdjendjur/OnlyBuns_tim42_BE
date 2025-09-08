package rs.ac.uns.ftn.informatika.jpa.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.dto.*;
import rs.ac.uns.ftn.informatika.jpa.mapper.CommentDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.mapper.PostMapper;
import rs.ac.uns.ftn.informatika.jpa.model.*;
import rs.ac.uns.ftn.informatika.jpa.repository.CommentRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final LocationService locationService;
    private final CommentService commentService;
    private final CommentDTOMapper commentDTOMapper;

    @Autowired
    private AdvertisingService advertisingService;

    @Autowired
    public PostService(PostRepository postRepository, UserService userService, LocationService locationService,
                       CommentDTOMapper commentDTOMapper, ImageService imageService, CommentService commentService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.locationService = locationService;
        this.commentDTOMapper = commentDTOMapper;
        this.imageService = imageService;
        this.commentService = commentService;
    }

    @org.springframework.transaction.annotation.Transactional
    public PostDTO createPost(PostDTO postDTO, MultipartFile file, Double latitude, Double longitude, String address) throws IOException {
        Long userId = postDTO.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Image image = imageService.saveImage(file);

        // Create or fetch location
        Location location = locationService.createLocation(latitude, longitude, address);

        // Create post
        Post post = new Post();
        post.setDescription(postDTO.getDescription());
        post.setCreatedTime(LocalDateTime.now());
        post.setUser(user);
        post.setImage(image);
        post.setLocation(location); // Associate the location with the post

        Post savedPost = postRepository.save(post);

        postDTO.setId(savedPost.getId());
        postDTO.setImageId(image.getId());
        return postDTO;
    }

    @Transactional
    public Optional<Post> getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        post.ifPresent(p -> Hibernate.initialize(p.getLikes())); // Ensure likes are initialized
        return post;
    }

    @Transactional
    public List<PostViewDTO> getAllPosts(Long userId) {
        List<Post> posts = postRepository.findAll();
        User currentUser = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        return posts.stream()
                .map(post -> new PostViewDTO(
                        post.getId(),
                        post.getDescription(),
                        post.getImage() != null ? post.getImage().getPath() : "assets/default.jpg", // Default image
                        post.getUser() != null ? post.getUser().getId() : null, // Handle null user
                        post.getUser() != null ? post.getUser().getUsername() : "Unknown", // Provide default username
                        post.getLikes().size(),
                        post.getCreatedTime(),
                        post.getLikes().contains(currentUser) // Check if the user liked this post
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<CommentDTO> getPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Hibernate.initialize(post.getComments());

        return post.getComments().stream()
                .sorted((c1, c2) -> c2.getCreatedTime().compareTo(c1.getCreatedTime())) // Sort by newest
                .map(comment -> new CommentDTO(
                        comment.getId(),
                        comment.getText(),
                        comment.getCreatedTime(),
                        comment.getUser().getId(),
                        comment.getPost().getId(),
                        comment.getUser().getUsername()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Fetch and delete the associated location
        Location location = post.getLocation();
        if (location != null) {
            locationService.deleteLocation(location.getId()); // Trigger location deletion and cache eviction
        }

        // Delete the post itself
        postRepository.deleteById(postId);
    }

    @Transactional
    public void addCommentToPost(WriteCommentDTO commentDTO) {
        commentService.addComment(commentDTO.getPostId(), commentDTO.getUserId(), commentDTO.getText());
    }

    @Transactional
    public ResponseEntity<Map<String, String>> toggleLike(Long postId, Long userId) {
        Map<String, String> response = new HashMap<>();

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        synchronized (this) {
            boolean alreadyLiked = post.getLikes().contains(user);

            if (alreadyLiked) {
                post.getLikes().remove(user); // Remove the user from likes
                postRepository.decrementLikes(postId); // Persist the change
                response.put("message", "Post unliked");
            } else {
                post.getLikes().add(user); // Add the user to likes
                postRepository.incrementLikes(postId); // Persist the change
                response.put("message", "Post liked");
            }
            response.put("likesCount", String.valueOf(post.getLikes().size()));
        }

        return ResponseEntity.ok(response);
    }
    @Transactional
    public PostDTO updatePost(Long postId, PostDTO postDTO, MultipartFile file, Double latitude, Double longitude, String address) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(postDTO.getUserId())) {
            throw new RuntimeException("Unauthorized: You can only edit your own posts");
        }

        post.setDescription(postDTO.getDescription());

        if (file != null && !file.isEmpty()) {
            Long prevImageId = post.getImage().getId();
            Image newImage = imageService.saveImage(file);
            post.setImage(newImage);
            imageService.deleteImage(prevImageId);
        }

        // Location update
        Location location = locationService.createLocation(latitude, longitude, address);
        Long locationId = post.getLocation().getId();
        post.setLocation(location);
        locationService.deleteLocation(locationId);


        Post updatedPost = postRepository.save(post);
        
        postDTO.setId(updatedPost.getId());
        if (updatedPost.getImage() != null) {
            postDTO.setImageId(updatedPost.getImage().getId());
        }

        return postDTO;
    }

    @Transactional
    public PostDetailDTO getPostDetails(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Hibernate.initialize(post.getUser());
        Hibernate.initialize(post.getLocation());
        Hibernate.initialize(post.getImage());
        Hibernate.initialize(post.getLikes());

        return new PostDetailDTO(
                post.getId(),
                post.getDescription(),
                post.getImage() != null ? post.getImage().getPath() : null,
                post.getUser() != null ? post.getUser().getId() : null,
                post.getUser() != null ? post.getUser().getUsername() : "Unknown",
                post.getLocation() != null ? post.getLocation().getLatitude() : null,
                post.getLocation() != null ? post.getLocation().getLongitude() : null,
                post.getLocation() != null ? post.getLocation().getAddress() : null,
                post.getCreatedTime(),
                post.getLikes().size()
        );
    }
    public List<UserLikeCountDTO> getTopLikersInLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> results = postRepository.findTopLikersInLast7Days(sevenDaysAgo);
        return results.stream()
                .map(row -> new UserLikeCountDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<PostMapDTO> findNearbyPosts(double lat, double lng, double radiusKm) {
        List<Post> allPosts = postRepository.findAllWithLocation();
        return allPosts.stream()
                .filter(p -> p.getLocation() != null)
                .filter(p -> distance(
                        lat, lng,
                        p.getLocation().getLatitude(),
                        p.getLocation().getLongitude()
                ) <= radiusKm)
                .map(post -> {
                    return new PostMapDTO(
                            post.getId(),
                            post.getDescription(),
                            post.getUser().getUsername(),
                            post.getLocation().getLatitude(),
                            post.getLocation().getLongitude()
                    );
                })
                .collect(Collectors.toList());
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    @Cacheable("topPostsAllTime")
    public List<PostDetailDTO> getTopPostsAllTimeDto() {
        return postRepository.findTop10ByOrderByLikesCountDesc()
                .stream()
                .map(PostMapper::toPostDetailDTO)
                .collect(Collectors.toList());
    }

    @Cacheable("topPostsLastWeek")
    public List<PostDetailDTO> getTopPostsLastWeekDto() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        return postRepository.findTop5ByCreatedTimeAfterOrderByLikesCountDesc(oneWeekAgo)
                .stream()
                .sorted(Comparator.comparingInt(Post::getLikesCount).reversed())
                .limit(5)
                .map(PostMapper::toPostDetailDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Post setAdEligible(Long postId, boolean adEligible) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setAdEligible(adEligible);
        Post saved = postRepository.save(post);

        if (adEligible) {
            AdPostMessage msg = new AdPostMessage(
                    saved.getDescription(),       // prilagodi nazivu tvog polja
                    saved.getCreatedTime(),       // prilagodi nazivu tvog timestamp polja
                    saved.getUser().getUsername() // pretpostavljam da User ima username
            );
            advertisingService.publish(msg);
        }

        return saved;
    }
}
