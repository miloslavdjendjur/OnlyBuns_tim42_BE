package rs.ac.uns.ftn.informatika.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.service.LocationService;
import rs.ac.uns.ftn.informatika.jpa.repository.UserFollowerRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.util.concurrent.*;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaExampleApplicationTests {

	@Autowired
	private PostService postService;
	@Autowired
	private UserService userService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserFollowerRepository userFollowerRepository;


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
	@Test
	public void testConcurrentFollowSameUser() throws InterruptedException {
		User targetUser = new User();
		targetUser.setEmail("target@test.com");
		targetUser.setUsername("targetuser");
		targetUser.setPassword("password");
		targetUser.setFullName("Target User");
		targetUser.setActive(true);
		targetUser.setRole(User.Role.REGISTERED);
		targetUser.setLastLogin(LocalDateTime.now());
		targetUser = userRepository.save(targetUser);
		Long targetUserId = targetUser.getId();

		int numberOfFollowers = 10;
		Long[] followerIds = new Long[numberOfFollowers];

		for (int i = 0; i < numberOfFollowers; i++) {
			User follower = new User();
			follower.setEmail("follower" + i + "@test.com");
			follower.setUsername("follower" + i);
			follower.setPassword("password");
			follower.setFullName("Follower " + i);
			follower.setActive(true);
			follower.setRole(User.Role.REGISTERED);
			follower.setLastLogin(LocalDateTime.now());
			followerIds[i] = userRepository.save(follower).getId();
		}

		AtomicInteger successCount = new AtomicInteger(0);
		ExecutorService executor = Executors.newFixedThreadPool(numberOfFollowers);
		CountDownLatch latch = new CountDownLatch(numberOfFollowers);

		for (int i = 0; i < numberOfFollowers; i++) {
			final Long followerId = followerIds[i];
			executor.submit(() -> {
				try {
					Thread.sleep((long) (Math.random() * 100));
					userService.followUserById(targetUserId, followerId);
					successCount.incrementAndGet();
				} catch (Exception e) {
					System.out.println("Follow failed: " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();

		assertEquals(numberOfFollowers, successCount.get());
		assertEquals(numberOfFollowers, userFollowerRepository.countFollowers(targetUserId));
	}

	@Test
	public void testRateLimitExceeded() {
		User follower = new User();
		follower.setEmail("ratelimit@test.com");
		follower.setUsername("ratelimituser");
		follower.setPassword("password");
		follower.setFullName("Rate Limit User");
		follower.setActive(true);
		follower.setRole(User.Role.REGISTERED);
		follower.setLastLogin(LocalDateTime.now());
		follower = userRepository.save(follower);
		Long followerId = follower.getId();

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger rateLimitCount = new AtomicInteger(0);

		for (int i = 0; i < 60; i++) {
			User targetUser = new User();
			targetUser.setEmail("target" + i + "@test.com");
			targetUser.setUsername("target" + i);
			targetUser.setPassword("password");
			targetUser.setFullName("Target " + i);
			targetUser.setActive(true);
			targetUser.setRole(User.Role.REGISTERED);
			targetUser.setLastLogin(LocalDateTime.now());
			targetUser = userRepository.save(targetUser);

			try {
				userService.followUserById(targetUser.getId(), followerId);
				successCount.incrementAndGet();
			} catch (ResponseStatusException e) {
				if (e.getStatus() == HttpStatus.FORBIDDEN) {
					rateLimitCount.incrementAndGet();
				}
			}
		}

		assertEquals(50, successCount.get());
		assertTrue(rateLimitCount.get() >= 10);
	}
}
