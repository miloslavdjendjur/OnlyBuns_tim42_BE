package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.dto.*;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.service.*;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final ImageService imageService;

    @Autowired
    public PostController(PostService postService, ImageService imageService) {
        this.postService = postService;
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("address") String address) {
        try {
            PostDTO postDTO = new PostDTO();
            postDTO.setUserId(userId);
            postDTO.setDescription(description);

            PostDTO createdPost = postService.createPost(postDTO, file, latitude, longitude, address);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @Value("${app.image.upload-dir}")
    private String uploadDir;

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

   //ovde bi trebalo da vraca PostDTO a ne post RELJJAAA
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = postService.getPostById(id);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Add this method for GET /api/posts to fetch all posts
    @GetMapping("/all")
    public ResponseEntity<List<PostViewDTO>> getAllPosts(@RequestParam Long userId) {
        List<PostViewDTO> posts = postService.getAllPosts(userId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/all-comments/{id}")
    public ResponseEntity<List<CommentDTO>> getAllComments(@PathVariable Long id){
        List<CommentDTO> comments = postService.getPostComments(id);
        return ResponseEntity.ok(comments);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/add-comment")
    public ResponseEntity<Post> addComment(@RequestBody WriteCommentDTO comment) {
        postService.addCommentToPost(comment);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/like/{postId}")
    public ResponseEntity<Map<String, String>> toggleLike(@PathVariable Long postId, @RequestParam Long userId) {
        //String response = postService.toggleLike(postId, userId);
        return postService.toggleLike(postId, userId);
    }
    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Long id,
            @RequestParam("description") String description,
            @RequestParam("userId") Long userId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("address") String address,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            PostDTO postDTO = new PostDTO();
            postDTO.setId(id);
            postDTO.setDescription(description);
            postDTO.setUserId(userId);

            PostDTO updatedPost = postService.updatePost(id, postDTO, file, latitude, longitude, address);
            return ResponseEntity.ok(updatedPost);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/getPost/{id}")
    public ResponseEntity<PostDetailDTO> getPostByPostId(@PathVariable Long id) {
        try {
            PostDetailDTO post = postService.getPostDetails(id);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
