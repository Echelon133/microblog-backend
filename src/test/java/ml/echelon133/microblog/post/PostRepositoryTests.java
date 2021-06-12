package ml.echelon133.microblog.post;

import ml.echelon133.microblog.post.model.*;
import ml.echelon133.microblog.post.repository.PostRepository;
import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class PostRepositoryTests {

    private UserRepository userRepository;

    private PostRepository postRepository;

    @Autowired
    public PostRepositoryTests(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    private User createTestUser(String username) {
        User u = new User(username, "", "", "");
        userRepository.save(u);
        // each user should follow themselves
        userRepository.followUserWithUuid(u.getUuid(), u.getUuid());
        return u;
    }

    private Post createTestPost(User user, String content, Long minutesAgo) {
        /*
            Sometimes when createTestPost is called from a loop,
            two posts using Date.from(...) get the same date object
            and this creates a chance of them being retrieved from the database in the
            wrong order when sorted by date, even though we clearly intended to create
            one object before another. When two posts are swapped, all tests
            that depend on posts order fail.

            To avoid this we can sleep 1ms before getting a Date object,
            which is not a clear solution, but it works.
         */
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {}

        Date ago = Date.from(Instant.now().minus(minutesAgo, MINUTES));
        Post post = new Post(user, content);
        post.setCreationDate(ago);
        return postRepository.save(post);
    }

    private Date getDateHoursAgo(int numberOfHours) {
        return Date.from(Instant.now().minus(numberOfHours, HOURS));
    }

    @BeforeEach
    public void beforeEach() {
        // five test users
        User test1 = createTestUser("test1");
        User test2 = createTestUser("test2");
        User test3 = createTestUser("test3");
        User test4 = createTestUser("test4");
        User test5 = createTestUser("test5");
        User test6 = createTestUser("test6");

        // "test1" also follows "test2" and "test3"
        userRepository.followUserWithUuid(test1.getUuid(), test2.getUuid());
        userRepository.followUserWithUuid(test1.getUuid(), test3.getUuid());

        // create 10 posts as "test1" (5 posts made 30 min ago, 5 made 10 min ago)
        for (int i = 0; i < 5; i++) {
            Post post = createTestPost(test1, "" + i, 30L);
            // make 1 user like every one of these posts
            postRepository.likePostWithUuid(test5.getUuid(), post.getUuid());
        }
        for (int i = 5; i < 10; i++) {
            Post post = createTestPost(test1, "" + i, 10L);
            // make 2 users like every one of these posts
            postRepository.likePostWithUuid(test4.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test5.getUuid(), post.getUuid());
        }

        // create 5 posts as "test2" (all of them made 80 min ago)
        for (int i = 10; i < 15; i++) {
            Post post = createTestPost(test2, "" + i, 80L);
            // make 3 users like every one of these posts
            postRepository.likePostWithUuid(test3.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test4.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test5.getUuid(), post.getUuid());
        }

        // create 5 posts as "test3" (all of them made 1 min ago)
        for (int i = 15; i < 20; i++) {
            Post post = createTestPost(test3, "" + i, 1L);
            // make 4 users like every one of these posts
            postRepository.likePostWithUuid(test2.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test3.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test4.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test5.getUuid(), post.getUuid());
        }

        // create 5 posts as "test4"
        for (int i = 20; i < 25; i++) {
            Post post = createTestPost(test4, "" + i, 0L);
            postRepository.likePostWithUuid(test1.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test2.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test3.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test5.getUuid(), post.getUuid());
            postRepository.likePostWithUuid(test6.getUuid(), post.getUuid());
        }

        /*
            Test database has 25 posts
            User "test1":
                * posts 0, 1, 2, 3, 4 made 30 min before
                * posts 5, 6, 7, 8, 9 made 10 min before
            User "test2":
                * posts 10, 11, 12, 13, 14 made 80 min before
            User "test3":
                * posts 15, 16, 17, 18, 19 made 1 min before
            User "test4":
                * posts 20, 21, 22, 23, 24
            Posts 0, 1, 2, 3, 4 have 1 like each
            Posts 5, 6, 7, 8, 9 have 2 likes each
            Posts 10, 11, 12, 13, 14 have 3 likes each
            Posts 15, 16, 17, 18, 19 have 4 likes each
            Posts 20, 21, 22, 23, 24 have 5 likes each
         */
    }

    @Test
    public void savedPostGetsUuid() {
        User user = userRepository.findByUsername("test1").get();

        Post post = new Post(user, "Test");
        assertNull(post.getUuid());

        Post savedPost = postRepository.save(post);
        assertNotNull(savedPost.getUuid());
    }

    @Test
    public void getFeedForUserWithUuid_IsEmptyWhenUserDoesntExist() {
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid(UUID.randomUUID(), 0L, 10L);

        assertEquals(0, posts.size());
    }

    @Test
    public void getFeedForUserWithUuid_ContainsMostRecentPosts() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid(user.getUuid(), 0L, 10L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(10, posts.size());
        // expect posts with content 5, 6, 7, 8, 9, 15, 16, 17, 18, 19 (all made within last 15 minutes)
        Arrays.asList(5, 6, 7, 8, 9, 15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_DoesNotContainDeletedPosts() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // create a post
        Post post = createTestPost(user, "200", 0L);
        post.markAsDeleted(); // mark as deleted even before saving it
        postRepository.save(post);

        // when
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid(user.getUuid(), 0L, 10L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(10, posts.size());
        // expect posts with content 5, 6, 7, 8, 9, 15, 16, 17, 18, 19
        Arrays.asList(5, 6, 7, 8, 9, 15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_SkipArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid(user.getUuid(), 5L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, posts.size());
        // expect posts with content: 5, 6, 7, 8, 9 (since posts made 1 min before were skipped)
        Arrays.asList(5, 6, 7, 8, 9).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_LimitArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid(user.getUuid(), 0L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, posts.size());
        // expect posts with content: 15, 16, 17, 18, 19 (since they were posted 1 min before) and limit is set to 5
        Arrays.asList(15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_FeedWithAllPostsIsSorted() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForUserWithUuid(user.getUuid(), 0L, 25L);

        // make a list of contents of retrieved posts
        List<String> contents = allPosts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(20, allPosts.size());

        // expect order:
        // first: 19, 18, 17, 16, 15 (posted 1 min ago)
        // then: 9, 8, 7, 6, 5       (posted 10 min ago)
        // then: 4, 3, 2, 1, 0       (posted 30 min ago, but before posts above were made)
        // then: 14, 13, 12, 11, 10  (posted 80 min ago)
        List<Integer> correctOrder = Arrays.asList(19, 18, 17, 16, 15, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 14, 13, 12, 11, 10);
        for (int i = 0; i < correctOrder.size(); i++) {
            assertEquals(correctOrder.get(i).toString(), contents.get(i));
        }
    }

    @Test
    public void getFeedForUserWithUuid_PostsHaveCorrectAuthors() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());
        User test2 = userRepository.findByUsername("test2").orElse(new User());
        User test3 = userRepository.findByUsername("test3").orElse(new User());

        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForUserWithUuid(test1.getUuid(), 0L, 20L); // limit to 20, to show all posts

        // then
        // expected posts of test1: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        List<FeedPost> test1Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test1.getUuid())
                .collect(Collectors.toList());

        assertEquals(10, test1Posts.size());

        List<String> test1Contents = test1Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach(c -> {
            assertTrue(test1Contents.contains(c.toString()));
        });

        // expected posts of test2: 10, 11, 12, 13, 14
        List<FeedPost> test2Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test2.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test2Posts.size());

        List<String> test2Contents = test2Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(10, 11, 12, 13, 14).forEach(c -> {
            assertTrue(test2Contents.contains(c.toString()));
        });

        // expected posts of test3: 15, 16, 17, 18, 19
        List<FeedPost> test3Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test3.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test3Posts.size());

        List<String> test3Contents = test3Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(15, 16, 17, 18, 19).forEach(c -> {
            assertTrue(test3Contents.contains(c.toString()));
        });
    }

    @Test
    public void getPostWithUuid_ReturnsEmptyObjectWhenUuidDoesNotExistInDb() {

        // when
        Optional<FeedPost> post = postRepository.getPostWithUuid(UUID.randomUUID());

        // then
        assertTrue(post.isEmpty());
    }

    @Test
    public void getPostWithUuid_ReturnsEmptyObjectWhenObjectMarkedAsDeleted() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());

        Post b = createTestPost(test1, "test post", 0L);
        b.markAsDeleted();
        postRepository.save(b);

        // when
        Optional<FeedPost> post = postRepository.getPostWithUuid(b.getUuid());

        // then
        assertTrue(post.isEmpty());
    }

    @Test
    public void getPostWithUuid_ReturnsCorrectObjectWhenUuidExistsInDb() {
        // setup a user with one post
        User u = createTestUser("user");

        Post b = createTestPost(u, "this is a test post", 0L);

        // when
        Optional<FeedPost> post = postRepository.getPostWithUuid(b.getUuid());

        // then
        assertTrue(post.isPresent());

        FeedPost feedPost = post.get();
        assertEquals(b.getUuid(), feedPost.getUuid());
        assertEquals(b.getAuthor(), feedPost.getAuthor());
        assertEquals(b.getContent(), feedPost.getContent());
        assertEquals(b.getCreationDate(), feedPost.getDate());
    }

    @Test
    public void getPostWithUuid_HoldsUuidOfParentPost() {
        // setup a user with one post and one response to that post
        User u = createTestUser("user");

        Post b = createTestPost(u, "this is a test post", 0L);

        // this post responds to the post above.
        // when we retrieve this response post from the database
        // its field 'respondsTo' should hold UUID of the previous post
        Post r = new ResponsePost(u, "response", b);
        postRepository.save(r);

        // when
        // find the response
        Optional<FeedPost> post = postRepository.getPostWithUuid(r.getUuid());

        // then
        assertTrue(post.isPresent());

        FeedPost feedPost = post.get();
        UUID parentUuid = b.getUuid();
        assertEquals(parentUuid, feedPost.getRespondsTo());
    }

    @Test
    public void getPostWithUuid_HoldsUuidOfReferencedPost() {
        // setup a user with one post and one quote of that post
        User u = createTestUser("user");

        Post b = createTestPost(u, "this is a test post", 0L);

        // this post references the post above
        Post r = new QuotePost(u, "quote", b);
        postRepository.save(r);

        // when
        // find the quote
        Optional<FeedPost> post = postRepository.getPostWithUuid(r.getUuid());

        // then
        assertTrue(post.isPresent());

        FeedPost feedPost = post.get();
        UUID referencedUuid = b.getUuid();
        assertEquals(referencedUuid, feedPost.getQuotes());
    }

    @Test
    public void likePostWithUuid_IsEmptyWhenUuidInvalid() {
        // when
        Optional<Long> result = postRepository.likePostWithUuid(UUID.randomUUID(), UUID.randomUUID());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void likePostWithUuid_IsEmptyWhenPostMarkedAsDeleted() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());

        Post b = createTestPost(test1, "test post", 0L);
        b.markAsDeleted();
        postRepository.save(b);

        // when
        Optional<Long> result = postRepository.likePostWithUuid(test1.getUuid(), b.getUuid());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void likePostWithUuid_Works() {
        // create two users
        User u1 = createTestUser("u1");
        User u2 = createTestUser("u2");

        // u2 creates a post
        Post b = createTestPost(u2, "test", 0L);

        // check if u1 likes the post that u2 made
        Optional<Long> result = postRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isEmpty());

        // now, as the u1, like the post that u2 made
        postRepository.likePostWithUuid(u1.getUuid(), b.getUuid());

        // check again if u1 likes the post
        result = postRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isPresent());
    }

    @Test
    public void unlikePostWithUuid_Works() {
        // create two users
        User u1 = createTestUser("u1");
        User u2 = createTestUser("u2");

        // u2 creates a post
        Post b = createTestPost(u2, "test", 0L);

        // now, as the u1, like the post that u2 made
        postRepository.likePostWithUuid(u1.getUuid(), b.getUuid());

        // check if the like was registered
        Optional<Long> result = postRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isPresent());

        // now unlike that post
        postRepository.unlikePostWithUuid(u1.getUuid(), b.getUuid());

        // check if the unlike was registered
        result = postRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isEmpty());
    }

    @Test
    public void getInfoAboutPostWithUuid_IsEmptyWhenUuidInvalid() {
        // when
        Optional<PostInfo> bInfo = postRepository.getInfoAboutPostWithUuid(UUID.randomUUID());

        // then
        assertTrue(bInfo.isEmpty());
    }

    @Test
    public void getInfoAboutPostWithUuid_IsEmptyWhenPostMarkedAsDeleted() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());

        Post b = createTestPost(test1, "test post", 0L);
        b.markAsDeleted();
        postRepository.save(b);

        // when
        Optional<PostInfo> bInfo = postRepository
                .getInfoAboutPostWithUuid(b.getUuid());

        // then
        assertTrue(bInfo.isEmpty());
    }

    @Test
    public void getInfoAboutPostWithUuid_HoldsCorrectCounterValues() {
        // create four users
        User u1 = createTestUser("u1");
        User u2 = createTestUser("u2");
        User u3 = createTestUser("u3");
        User u4 = createTestUser("u4");

        // post a post as u1
        Post u1Post = createTestPost(u1, "test", 0L);

        // create three responses to that post (and delete one)
        Post u2Response1 = new ResponsePost(u2, "test", u1Post);
        Post u2Response2 = new ResponsePost(u2, "test", u1Post);
        Post u2Response3 = new ResponsePost(u2, "test", u1Post);
        postRepository.save(u2Response1);
        postRepository.save(u2Response2);
        u2Response3.markAsDeleted(); // mark this response as deleted before saving
        postRepository.save(u2Response3);

        // create two quotes (and delete one)
        Post u2Quote1 = new QuotePost(u2, "test", u1Post);
        Post u2Quote2 = new QuotePost(u2, "test", u1Post);
        postRepository.save(u2Quote1);
        u2Quote2.markAsDeleted(); // mark this quote as deleted before saving
        postRepository.save(u2Quote2);

        // like the initial post as u2, u3, u4
        postRepository.likePostWithUuid(u2.getUuid(), u1Post.getUuid());
        postRepository.likePostWithUuid(u3.getUuid(), u1Post.getUuid());
        postRepository.likePostWithUuid(u4.getUuid(), u1Post.getUuid());

        // when
        Optional<PostInfo> bInfo = postRepository.getInfoAboutPostWithUuid(u1Post.getUuid());

        // then
        assertTrue(bInfo.isPresent());

        // post should have 2 responses, 1 quote and 3 likes
        // because one response and one quote have been deleted
        assertEquals(2L, bInfo.get().getResponses());
        assertEquals(1L, bInfo.get().getQuotes());
        assertEquals(3L, bInfo.get().getLikes());
    }

    @Test
    public void getAllResponsesToPostWithUuid_IsEmptyWhenNoResponses() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // when
        List<FeedPost> posts = postRepository.getAllResponsesToPostWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(0, posts.size());
    }

    @Test
    public void getAllResponsesToPostWithUuid_DoesNotListResponsesMarkedAsDeleted() {
        // create a user
        User u = createTestUser("u1");

        // create a parent post and two responses
        Post b = createTestPost(u, "content", 0L);
        Post r1 = new ResponsePost(u, "r1", b);
        Post r2 = new ResponsePost(u, "r2", b);
        r2.markAsDeleted(); // mark second response as deleted right away
        postRepository.save(r1);
        postRepository.save(r2);

        // when
        List<FeedPost> posts = postRepository.getAllResponsesToPostWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(1, posts.size());
    }

    @Test
    public void getAllResponsesToPostWithUuid_SkipArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // make 5 responses
        for (int i = 0; i < 5; i++) {
            Post b1 = new ResponsePost(u, "response " + i, b);
            postRepository.save(b1);
        }

        // when
        List<FeedPost> posts = postRepository.getAllResponsesToPostWithUuid(b.getUuid(), 2L, 10L);

        // then
        assertEquals(3, posts.size());
    }

    @Test
    public void getAllResponsesToPostWithUuid_LimitArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // make 5 responses
        for (int i = 0; i < 5; i++) {
            Post b1 = new ResponsePost(u, "response " + i, b);
            postRepository.save(b1);
        }

        // when
        List<FeedPost> posts = postRepository.getAllResponsesToPostWithUuid(b.getUuid(), 0L, 4L);

        // then
        assertEquals(4, posts.size());
    }

    @Test
    public void getAllResponsesToPostWithUuid_ReturnsCorrectObjects() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // make 5 responses
        for (int i = 0; i < 5; i++) {
            Post b1 = new ResponsePost(u, "response " + i, b);
            postRepository.save(b1);
        }

        // when
        List<FeedPost> posts = postRepository.getAllResponsesToPostWithUuid(b.getUuid(), 0L, 5L);

        // then
        assertEquals(5, posts.size());

        // response number 0 is the oldest one, expect it first
        for (int i = 0; i < posts.size(); i++) {
            FeedPost fPost = posts.get(i);

            assertTrue(fPost.getContent().contains("response " + i));
            assertEquals("u1", fPost.getAuthor().getUsername());
            assertEquals(b.getUuid(), fPost.getRespondsTo());
            assertNull(fPost.getQuotes());
        }
    }

    @Test
    public void getAllQuotesOfPostWithUuid_IsEmptyWhenNoResponses() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // when
        List<FeedPost> posts = postRepository.getAllQuotesOfPostWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(0, posts.size());
    }

    @Test
    public void getAllQuotesOfPostWithUuid_DoesNotListQuotesMarkedAsDeleted() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);
        Post r1 = new QuotePost(u, "r1", b);
        Post r2 = new QuotePost(u, "r2", b);
        r2.markAsDeleted(); // mark second quote as deleted right away
        postRepository.save(r1);
        postRepository.save(r2);

        // when
        List<FeedPost> posts = postRepository.getAllQuotesOfPostWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(1, posts.size());
    }

    @Test
    public void getAllQuotesOfPostWithUuid_SkipArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // make 5 quotes
        for (int i = 0; i < 5; i++) {
            Post b1 = new QuotePost(u, "quote " + i, b);
            postRepository.save(b1);
        }

        // when
        List<FeedPost> posts = postRepository.getAllQuotesOfPostWithUuid(b.getUuid(), 2L, 10L);

        // then
        assertEquals(3, posts.size());
    }

    @Test
    public void getAllQuotesOfPostWithUuid_LimitArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // make 5 quotes
        for (int i = 0; i < 5; i++) {
            Post b1 = new QuotePost(u, "reblobb " + i, b);
            postRepository.save(b1);
        }

        // when
        List<FeedPost> posts = postRepository.getAllQuotesOfPostWithUuid(b.getUuid(), 0L, 4L);

        // then
        assertEquals(4, posts.size());
    }

    @Test
    public void getAllQuotesOfPostWithUuid_ReturnsCorrectObjects() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Post b = createTestPost(u, "content", 0L);

        // make 5 quotes
        for (int i = 0; i < 5; i++) {
            Post b1 = new QuotePost(u, "quote " + i, b);
            postRepository.save(b1);
        }

        // when
        List<FeedPost> posts = postRepository.getAllQuotesOfPostWithUuid(b.getUuid(), 0L, 5L);

        // then
        assertEquals(5, posts.size());

        // quote number 0 is the oldest one, expect it first
        for (int i = 0; i < posts.size(); i++) {
            FeedPost fPost = posts.get(i);

            assertTrue(fPost.getContent().contains("quote " + i));
            assertEquals("u1", fPost.getAuthor().getUsername());
            assertEquals(b.getUuid(), fPost.getQuotes());
            assertNull(fPost.getRespondsTo());
        }
    }

    @Test
    public void getFeedForUserWithUuid_Popular_IsEmptyWhenUserDoesntExist() {
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid_Popular(
                        UUID.randomUUID(), getDateHoursAgo(24),
                        0L, 10L);

        assertEquals(0, posts.size());
    }

    @Test
    public void getFeedForUserWithUuid_Popular_DoesNotContainDeletedPosts() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // create a post
        Post post = createTestPost(user, "200", 0L);
        post.markAsDeleted(); // mark as deleted even before saving it
        postRepository.save(post);

        // when
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid_Popular(
                        user.getUuid(), getDateHoursAgo(24),
                        0L, 10L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(10, posts.size());
        // expect posts with content 19, 18, 17, 16, 15, 14, 13, 12, 11, 10
        Arrays.asList(19, 18, 17, 16, 15, 14, 13, 12, 11, 10).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_Popular_SkipArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid_Popular(
                        user.getUuid(), getDateHoursAgo(24),
                        5L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, posts.size());
        // expect posts with content: 14, 13, 12, 11, 10
        Arrays.asList(14, 13, 12, 11, 10).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_Popular_LimitArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> posts = postRepository
                .getFeedForUserWithUuid_Popular(
                        user.getUuid(), getDateHoursAgo(24),
                        0L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, posts.size());
        // expect posts with content: 15, 16, 17, 18, 19
        Arrays.asList(15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_Popular_FeedWithAllPostsIsSorted() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForUserWithUuid_Popular(
                        user.getUuid(), getDateHoursAgo(24),
                        0L, 25L);

        // make a list of contents of retrieved posts
        List<String> contents = allPosts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(20, allPosts.size());

        /*
         since we expect 20 posts sorted by likes and date (in that order)
         we should expect them in order:
            * first 19, 18, 17, 16, 15 (because each post has 4 likes)
            * second 14, 13, 12, 11, 10 (because each post has 3 likes)
            * third 9, 8, 7, 6, 5 (because each post has 2 likes)
            * fourth 4, 3, 2, 1, 0 (because each post has 1 like)
         */
        List<Integer> correctOrder = Arrays.asList(19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        for (int i = 0; i < correctOrder.size(); i++) {
            assertEquals(correctOrder.get(i).toString(), contents.get(i));
        }
    }

    @Test
    public void getFeedForUserWithUuid_Popular_FeedFiltersByDate() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForUserWithUuid_Popular(
                        user.getUuid(), getDateHoursAgo(1),
                        0L, 20L);

        // make a list of contents of retrieved posts
        List<String> contents = allPosts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(15, allPosts.size());

        /*
         since we expect 15 posts sorted by likes and date (in that order)
         we should expect them in order:
            * first 19, 18, 17, 16, 15 (because each post has 4 likes)
            * second 9, 8, 7, 6, 5 (because each post has 2 likes)
            * third 4, 3, 2, 1, 0 (because each post has 1 like)

         posts 14, 13, 12, 11, 10 won't show up, because they should be
         filtered out (they had been posted 80 minutes before, and we only get
         posts posted in the last 60 minutes)
         */
        List<Integer> correctOrder = Arrays.asList(19, 18, 17, 16, 15, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        for (int i = 0; i < correctOrder.size(); i++) {
            assertEquals(correctOrder.get(i).toString(), contents.get(i));
        }
    }

    @Test
    public void getFeedForUserWithUuid_Popular_PostsHaveCorrectAuthors() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());
        User test2 = userRepository.findByUsername("test2").orElse(new User());
        User test3 = userRepository.findByUsername("test3").orElse(new User());

        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForUserWithUuid_Popular(
                        test1.getUuid(), getDateHoursAgo(24),
                        0L, 25L);

        // then
        // expected posts of test1: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        List<FeedPost> test1Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test1.getUuid())
                .collect(Collectors.toList());

        assertEquals(10, test1Posts.size());

        List<String> test1Contents = test1Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach(c -> {
            assertTrue(test1Contents.contains(c.toString()));
        });

        // expected posts of test2: 10, 11, 12, 13, 14
        List<FeedPost> test2Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test2.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test2Posts.size());

        List<String> test2Contents = test2Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(10, 11, 12, 13, 14).forEach(c -> {
            assertTrue(test2Contents.contains(c.toString()));
        });

        // expected posts of test3: 15, 16, 17, 18, 19
        List<FeedPost> test3Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test3.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test3Posts.size());

        List<String> test3Contents = test3Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(15, 16, 17, 18, 19).forEach(c -> {
            assertTrue(test3Contents.contains(c.toString()));
        });
    }

    @Test
    public void getFeedForAnonymousUser_Popular_DoesNotContainDeletedPosts() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // create a post
        Post post = createTestPost(user, "200", 0L);
        post.markAsDeleted(); // mark as deleted even before saving it
        postRepository.save(post);

        // when
        List<FeedPost> posts = postRepository
                .getFeedForAnonymousUser_Popular(getDateHoursAgo(24),0L, 10L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(10, posts.size());
        // expect posts with content 24, 23, 22, 21, 20, 19, 18, 17, 16, 15
        Arrays.asList(24, 23, 22, 21, 20, 19, 18, 17, 16, 15).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForAnonymousUser_Popular_SkipArgumentWorks() {
        // when
        List<FeedPost> posts = postRepository
                .getFeedForAnonymousUser_Popular(getDateHoursAgo(24),5L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, posts.size());
        // expect posts with content: 19, 18, 17, 16, 15
        Arrays.asList(19, 18, 17, 16, 15).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForAnonymousUser_Popular_LimitArgumentWorks() {
        // when
        List<FeedPost> posts = postRepository
                .getFeedForAnonymousUser_Popular(getDateHoursAgo(24),0L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = posts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, posts.size());
        // expect posts with content: 24, 23, 22, 21, 20
        Arrays.asList(24, 23, 22, 21, 20).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForAnonymousUser_Popular_FeedWithAllPostsIsSorted() {
        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForAnonymousUser_Popular(getDateHoursAgo(24),0L, 25L);

        // make a list of contents of retrieved posts
        List<String> contents = allPosts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(25, allPosts.size());

        /*
         since we expect 25 posts sorted by likes and date (in that order)
         we should expect them in order:
            * first 24, 23, 22, 21, 20 (because each post has 5 likes)
            * second 19, 18, 17, 16, 15 (because each post has 4 likes)
            * third 14, 13, 12, 11, 10 (because each post has 3 likes)
            * fourth 9, 8, 7, 6, 5 (because each post has 2 likes)
            * fifth 4, 3, 2, 1, 0 (because each post has 1 like)
         */
        List<Integer> correctOrder = Arrays.asList(24, 23, 22, 21, 20, 19, 18, 17, 16, 15,
                14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        for (int i = 0; i < correctOrder.size(); i++) {
            assertEquals(correctOrder.get(i).toString(), contents.get(i));
        }
    }

    @Test
    public void getFeedForAnonymousUser_Popular_FeedFiltersByDate() {
        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForAnonymousUser_Popular(getDateHoursAgo(1),0L, 25L); // limit to 25

        // make a list of contents of retrieved posts
        List<String> contents = allPosts.stream().map(FeedPost::getContent).collect(Collectors.toList());

        // then
        assertEquals(20, allPosts.size());

        /*
         since we expect 20 posts sorted by likes and date (in that order)
         we should expect them in order:
            * first 24, 23, 22, 21, 20 (because each post has 5 likes)
            * second 19, 18, 17, 16, 15 (because each post has 4 likes)
            * third 9, 8, 7, 6, 5 (because each post has 2 likes)
            * fourth 4, 3, 2, 1, 0 (because each post has 1 like)

         posts 14, 13, 12, 11, 10 won't show up, because they should be
         filtered out (they had been posted 80 minutes before, and we only get
         posts posted in the last 60 minutes)
         */
        List<Integer> correctOrder = Arrays.asList(24, 23, 22, 21, 20, 19, 18, 17, 16, 15,
                9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        for (int i = 0; i < correctOrder.size(); i++) {
            assertEquals(correctOrder.get(i).toString(), contents.get(i));
        }
    }

    @Test
    public void getFeedForAnonymousUser_Popular_PostsHaveCorrectAuthors() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());
        User test2 = userRepository.findByUsername("test2").orElse(new User());
        User test3 = userRepository.findByUsername("test3").orElse(new User());
        User test4 = userRepository.findByUsername("test4").orElse(new User());

        // when
        List<FeedPost> allPosts = postRepository
                .getFeedForAnonymousUser_Popular(getDateHoursAgo(24),0L, 25L); // limit to 25, to show all posts

        // then
        // expected posts of test1: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        List<FeedPost> test1Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test1.getUuid())
                .collect(Collectors.toList());

        assertEquals(10, test1Posts.size());

        List<String> test1Contents = test1Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach(c -> {
            assertTrue(test1Contents.contains(c.toString()));
        });

        // expected posts of test2: 10, 11, 12, 13, 14
        List<FeedPost> test2Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test2.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test2Posts.size());

        List<String> test2Contents = test2Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(10, 11, 12, 13, 14).forEach(c -> {
            assertTrue(test2Contents.contains(c.toString()));
        });

        // expected posts of test3: 15, 16, 17, 18, 19
        List<FeedPost> test3Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test3.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test3Posts.size());

        List<String> test3Contents = test3Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(15, 16, 17, 18, 19).forEach(c -> {
            assertTrue(test3Contents.contains(c.toString()));
        });

        // expected posts of test4: 20, 21, 22, 23, 24
        List<FeedPost> test4Posts = allPosts.stream().filter(b -> b.getAuthor().getUuid() == test4.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test4Posts.size());

        List<String> test4Contents = test4Posts.stream().map(FeedPost::getContent).collect(Collectors.toList());
        Arrays.asList(20, 21, 22, 23, 24).forEach(c -> {
            assertTrue(test4Contents.contains(c.toString()));
        });
    }
}
