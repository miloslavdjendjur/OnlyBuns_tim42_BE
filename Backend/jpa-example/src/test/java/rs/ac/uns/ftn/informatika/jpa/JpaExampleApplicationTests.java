package rs.ac.uns.ftn.informatika.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.service.LocationService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaExampleApplicationTests {

	@Autowired
	private PostService postService;
	@Autowired
	private UserService userService;

	@Test
	public void contextLoads() {
		// Ensure that the context loads
	}

	@Test
	public void testConcurrentLikes() throws InterruptedException {
		Long postId = 1L;
		Long userId1 = 1L;
		Long userId2 = 2L;
		// Ovi ajdijevi moraju da postoje

		ExecutorService executorService = Executors.newFixedThreadPool(2);

		Runnable task1 = () -> postService.toggleLike(postId, userId1);
		Runnable task2 = () -> postService.toggleLike(postId, userId2);

		executorService.submit(task1);
		executorService.submit(task2);

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		Post post = postService.getPostById(postId)
				.orElseThrow(() -> new RuntimeException("Post with ID " + postId + " not found"));

		assert post.getLikes().size() == 2; // Adjust if toggling removes likes
	}

	@Test
	public void testConcurrentRegistration() throws Exception {
		Long unique = System.currentTimeMillis(); // za jedinstveni email
		String username = "konfliktniKorisnik";

		User user1 = new User();
		user1.setUsername(username);
		user1.setPassword("lozinka123");
		user1.setEmail("user" + unique + "@example.com");
		user1.setFullName("Test");
		user1.setAddress("Adresa");

		User user2 = new User();
		user2.setUsername(username);
		user2.setPassword("lozinka123");
		user2.setEmail("user" + (unique + 1) + "@example.com");
		user2.setFullName("Test");
		user2.setAddress("Adresa");

		ExecutorService executor = Executors.newFixedThreadPool(2);
		Callable<String> task1 = () -> {
			try {
				userService.registerUser(user1);
				return "SUCCESS";
			} catch (Exception e) {
				return "FAIL";
			}
		};
		Callable<String> task2 = () -> {
			try {
				userService.registerUser(user2);
				return "SUCCESS";
			} catch (Exception e) {
				return "FAIL";
			}
		};

		Future<String> result1 = executor.submit(task1);
		Future<String> result2 = executor.submit(task2);

		String r1 = result1.get();
		String r2 = result2.get();

		executor.shutdown();

		System.out.println("Rezultat 1: " + r1);
		System.out.println("Rezultat 2: " + r2);

		assert (r1.equals("SUCCESS") && r2.equals("FAIL")) || (r1.equals("FAIL") && r2.equals("SUCCESS"));
	}
}
