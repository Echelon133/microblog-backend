package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;
import ml.echelon133.blobb.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class BlobbRepositoryTests {

    private UserRepository userRepository;

    private BlobbRepository blobbRepository;

    @Autowired
    public BlobbRepositoryTests(UserRepository userRepository, BlobbRepository blobbRepository) {
        this.userRepository = userRepository;
        this.blobbRepository = blobbRepository;
    }

    private User createTestUser(String username) {
        User u = new User(username, "", "", "");
        userRepository.save(u);
        // each user should follow themselves
        userRepository.followUserWithUuid(u.getUuid(), u.getUuid());
        return u;
    }

    private Blobb createTestBlobb(User user, String content, Long minutesAgo) {
        /*
            Sometimes when createTestBlobb is called from a loop,
            two posts using Date.from(...) get the same date object
            and this creates a chance of them being retrieved from the database in the
            wrong order when sorted by date, even though we clearly intended to create
            one object before another. When two blobbs are swapped, all tests
            that depend on blobbs order fail.

            To avoid this we can sleep 1ms before getting a Date object,
            which is not a clear solution, but it works.
         */
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {}

        Date ago = Date.from(Instant.now().minus(minutesAgo, MINUTES));
        Blobb blobb = new Blobb(user, content);
        blobb.setCreationDate(ago);
        return blobbRepository.save(blobb);
    }

    @BeforeEach
    public void beforeEach() {
        // five test users
        User test1 = createTestUser("test1");
        User test2 = createTestUser("test2");
        User test3 = createTestUser("test3");
        User test4 = createTestUser("test4");
        User test5 = createTestUser("test5");

        // "test1" also follows "test2" and "test3"
        userRepository.followUserWithUuid(test1.getUuid(), test2.getUuid());
        userRepository.followUserWithUuid(test1.getUuid(), test3.getUuid());

        // create 10 posts as "test1" (5 posts made 30 min ago, 5 made 10 min ago)
        for (int i = 0; i < 5; i++) {
            Blobb blobb = createTestBlobb(test1, "" + i, 30L);
            // make 1 user like every one of these blobbs
            blobbRepository.likeBlobbWithUuid(test5.getUuid(), blobb.getUuid());
        }
        for (int i = 5; i < 10; i++) {
            Blobb blobb = createTestBlobb(test1, "" + i, 10L);
            // make 2 users like every one of these blobbs
            blobbRepository.likeBlobbWithUuid(test4.getUuid(), blobb.getUuid());
            blobbRepository.likeBlobbWithUuid(test5.getUuid(), blobb.getUuid());
        }

        // create 10 posts as "test2" (all of them made 30 min ago)
        for (int i = 10; i < 15; i++) {
            Blobb blobb = createTestBlobb(test2, "" + i, 30L);
            // make 3 users like every one of these blobbs
            blobbRepository.likeBlobbWithUuid(test3.getUuid(), blobb.getUuid());
            blobbRepository.likeBlobbWithUuid(test4.getUuid(), blobb.getUuid());
            blobbRepository.likeBlobbWithUuid(test5.getUuid(), blobb.getUuid());
        }

        // create 5 posts as "test3" (all of them made 1 min ago)
        for (int i = 15; i < 20; i++) {
            Blobb blobb = createTestBlobb(test3, "" + i, 1L);
            // make 4 users like every one of these blobbs
            blobbRepository.likeBlobbWithUuid(test2.getUuid(), blobb.getUuid());
            blobbRepository.likeBlobbWithUuid(test3.getUuid(), blobb.getUuid());
            blobbRepository.likeBlobbWithUuid(test4.getUuid(), blobb.getUuid());
            blobbRepository.likeBlobbWithUuid(test5.getUuid(), blobb.getUuid());
        }

        /*
            Test database has 20 posts
            User "test1":
                * posts 0, 1, 2, 3, 4 made 30 min before
                * posts 5, 6, 7, 8, 9 made 10 min before
            User "test2":
                * posts 10, 11, 12, 13, 14 made 30 min before
            User "test3":
                * posts 15, 16, 17, 18, 19 made 1 min before
            Posts 0, 1, 2, 3, 4 have 1 like each
            Posts 5, 6, 7, 8, 9 have 2 likes each
            Posts 10, 11, 12, 13, 14 have 3 likes each
            Posts 15, 16, 17, 18, 19 have 4 likes each
         */
    }

    @Test
    public void savedBlobbGetsUuid() {
        User user = userRepository.findByUsername("test1").get();

        Blobb blobb = new Blobb(user, "Test");
        assertNull(blobb.getUuid());

        Blobb savedBlobb = blobbRepository.save(blobb);
        assertNotNull(savedBlobb.getUuid());
    }

    @Test
    public void getFeedForUserWithUuid_IsEmptyWhenUserDoesntExist() {
        Date date7DaysAgo = Date.from(Instant.now().minus(1, MINUTES));
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_PostedBetween(UUID.randomUUID(),
                        date7DaysAgo,
                        new Date(), 0L, 10L);

        assertEquals(0, blobbs.size());
    }

    @Test
    public void getFeedForUserWithUuid_ContainsMostRecentPosts() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        Date date15MinAgo = Date.from(Instant.now().minus(15, MINUTES));

        // when
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(),
                        date15MinAgo,
                        new Date(), 0L, 10L);

        // make a list of contents of retrieved posts
        List<String> contents = blobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(10, blobbs.size());
        // expect posts with content 5, 6, 7, 8, 9, 15, 16, 17, 18, 19 (all made within last 15 minutes)
        Arrays.asList(5, 6, 7, 8, 9, 15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_DoesNotContainDeletedPosts() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // create a post
        Blobb blobb = createTestBlobb(user, "200", 0L);
        blobb.markAsDeleted(); // mark as deleted even before saving it
        blobbRepository.save(blobb);

        Date date15MinAgo = Date.from(Instant.now().minus(15, MINUTES));

        // when
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(),
                        date15MinAgo,
                        new Date(), 0L, 20L);

        // make a list of contents of retrieved posts
        List<String> contents = blobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(10, blobbs.size());
        // expect posts with content 5, 6, 7, 8, 9, 15, 16, 17, 18, 19 (all made within last 15 minutes)
        Arrays.asList(5, 6, 7, 8, 9, 15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_SkipArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        Date date15MinAgo = Date.from(Instant.now().minus(15, MINUTES));

        // when
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(),
                        date15MinAgo,
                        new Date(), 5L, 10L);

        // make a list of contents of retrieved posts
        List<String> contents = blobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, blobbs.size());
        // expect posts with content: 5, 6, 7, 8, 9 (since posts made 1 min before were skipped)
        Arrays.asList(5, 6, 7, 8, 9).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_LimitArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        Date date15MinAgo = Date.from(Instant.now().minus(15, MINUTES));

        // when
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(),
                        date15MinAgo,
                        new Date(), 0L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = blobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, blobbs.size());
        // expect posts with content: 15, 16, 17, 18, 19 (since they were posted 1 min before) and limit is set to 5
        Arrays.asList(15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_FeedWithAllPostsIsSorted() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // using this date shows all of the posts, since the oldest posts are from 30 min before
        Date date40MinAgo = Date.from(Instant.now().minus(40, MINUTES));

        // when
        List<FeedBlobb> allBlobbs = blobbRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(),
                        date40MinAgo,
                        new Date(), 0L, 20L); // limit to 20, to show all posts

        // make a list of contents of retrieved posts
        List<String> contents = allBlobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(20, allBlobbs.size());

        // expect order:
        // first: 19, 18, 17, 16, 15 (posted 1 min ago)
        // then: 9, 8, 7, 6, 5       (posted 10 min ago)
        // then: 14, 13, 12, 11, 10  (posted 30 min ago)
        // then: 4, 3, 2, 1, 0       (posted 30 min ago, but before posts above were made)
        List<Integer> correctOrder = Arrays.asList(19, 18, 17, 16, 15, 9, 8, 7, 6, 5, 14, 13, 12, 11, 10, 4, 3, 2, 1, 0);
        for (int i = 0; i < correctOrder.size(); i++) {
            assertEquals(correctOrder.get(i).toString(), contents.get(i));
        }
    }

    @Test
    public void getFeedForUserWithUuid_PostsHaveCorrectAuthors() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());
        User test2 = userRepository.findByUsername("test2").orElse(new User());
        User test3 = userRepository.findByUsername("test3").orElse(new User());

        // using this date shows all of the posts, since the oldest posts are from 30 min before
        Date date40MinAgo = Date.from(Instant.now().minus(40, MINUTES));

        // when
        List<FeedBlobb> allBlobbs = blobbRepository
                .getFeedForUserWithUuid_PostedBetween(test1.getUuid(),
                        date40MinAgo,
                        new Date(), 0L, 20L); // limit to 20, to show all posts

        // then
        // expected posts of test1: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        List<FeedBlobb> test1Posts = allBlobbs.stream().filter(b -> b.getAuthor().getUuid() == test1.getUuid())
                .collect(Collectors.toList());

        assertEquals(10, test1Posts.size());

        List<String> test1Contents = test1Posts.stream().map(FeedBlobb::getContent).collect(Collectors.toList());
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach(c -> {
            assertTrue(test1Contents.contains(c.toString()));
        });

        // expected posts of test2: 10, 11, 12, 13, 14
        List<FeedBlobb> test2Posts = allBlobbs.stream().filter(b -> b.getAuthor().getUuid() == test2.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test2Posts.size());

        List<String> test2Contents = test2Posts.stream().map(FeedBlobb::getContent).collect(Collectors.toList());
        Arrays.asList(10, 11, 12, 13, 14).forEach(c -> {
            assertTrue(test2Contents.contains(c.toString()));
        });

        // expected posts of test3: 15, 16, 17, 18, 19
        List<FeedBlobb> test3Posts = allBlobbs.stream().filter(b -> b.getAuthor().getUuid() == test3.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test3Posts.size());

        List<String> test3Contents = test3Posts.stream().map(FeedBlobb::getContent).collect(Collectors.toList());
        Arrays.asList(15, 16, 17, 18, 19).forEach(c -> {
            assertTrue(test3Contents.contains(c.toString()));
        });
    }

    @Test
    public void getBlobbWithUuid_ReturnsEmptyObjectWhenUuidDoesNotExistInDb() {

        // when
        Optional<FeedBlobb> blobb = blobbRepository.getBlobbWithUuid(UUID.randomUUID());

        // then
        assertTrue(blobb.isEmpty());
    }

    @Test
    public void getBlobbWithUuid_ReturnsEmptyObjectWhenObjectMarkedAsDeleted() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());

        Blobb b = createTestBlobb(test1, "test blobb", 0L);
        b.markAsDeleted();
        blobbRepository.save(b);

        // when
        Optional<FeedBlobb> blobb = blobbRepository.getBlobbWithUuid(b.getUuid());

        // then
        assertTrue(blobb.isEmpty());
    }

    @Test
    public void getBlobbWithUuid_ReturnsCorrectObjectWhenUuidExistsInDb() {
        // setup a user with one post
        User u = createTestUser("user");

        Blobb b = createTestBlobb(u, "this is a test blobb", 0L);

        // when
        Optional<FeedBlobb> blobb = blobbRepository.getBlobbWithUuid(b.getUuid());

        // then
        assertTrue(blobb.isPresent());

        FeedBlobb feedBlobb = blobb.get();
        assertEquals(b.getUuid(), feedBlobb.getUuid());
        assertEquals(b.getAuthor(), feedBlobb.getAuthor());
        assertEquals(b.getContent(), feedBlobb.getContent());
        assertEquals(b.getCreationDate(), feedBlobb.getDate());
    }

    @Test
    public void getBlobbWithUuid_HoldsUuidOfParentBlobb() {
        // setup a user with one post and one response to that post
        User u = createTestUser("user");

        Blobb b = createTestBlobb(u, "this is a test blobb", 0L);

        // this blobb responds to the blobb above.
        // when we retrieve this response blobb from the database
        // its field 'respondsTo' should hold UUID of the previous blobb
        Blobb r = new ResponseBlobb(u, "response", b);
        blobbRepository.save(r);

        // when
        // find the response
        Optional<FeedBlobb> blobb = blobbRepository.getBlobbWithUuid(r.getUuid());

        // then
        assertTrue(blobb.isPresent());

        FeedBlobb feedBlobb = blobb.get();
        UUID parentUuid = b.getUuid();
        assertEquals(parentUuid, feedBlobb.getRespondsTo());
    }

    @Test
    public void getBlobbWithUuid_HoldsUuidOfReferencedBlobb() {
        // setup a user with one post and one reblobb of that post
        User u = createTestUser("user");

        Blobb b = createTestBlobb(u, "this is a test blobb", 0L);

        // this blobb references the blobb above
        Blobb r = new Reblobb(u, "reblobb", b);
        blobbRepository.save(r);

        // when
        // find the reblobb
        Optional<FeedBlobb> blobb = blobbRepository.getBlobbWithUuid(r.getUuid());

        // then
        assertTrue(blobb.isPresent());

        FeedBlobb feedBlobb = blobb.get();
        UUID referencedUuid = b.getUuid();
        assertEquals(referencedUuid, feedBlobb.getReblobbs());
    }

    @Test
    public void likeBlobbWithUuid_IsEmptyWhenUuidInvalid() {
        // when
        Optional<Long> result = blobbRepository.likeBlobbWithUuid(UUID.randomUUID(), UUID.randomUUID());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void likeBlobbWithUuid_IsEmptyWhenBlobbMarkedAsDeleted() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());

        Blobb b = createTestBlobb(test1, "test blobb", 0L);
        b.markAsDeleted();
        blobbRepository.save(b);

        // when
        Optional<Long> result = blobbRepository.likeBlobbWithUuid(test1.getUuid(), b.getUuid());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void likeBlobbWithUuid_Works() {
        // create two users
        User u1 = createTestUser("u1");
        User u2 = createTestUser("u2");

        // u2 creates a post
        Blobb b = createTestBlobb(u2, "test", 0L);

        // check if u1 likes the post that u2 made
        Optional<Long> result = blobbRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isEmpty());

        // now, as the u1, like the post that u2 made
        blobbRepository.likeBlobbWithUuid(u1.getUuid(), b.getUuid());

        // check again if u1 likes the post
        result = blobbRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isPresent());
    }

    @Test
    public void unlikeBlobbWithUuid_Works() {
        // create two users
        User u1 = createTestUser("u1");
        User u2 = createTestUser("u2");

        // u2 creates a post
        Blobb b = createTestBlobb(u2, "test", 0L);

        // now, as the u1, like the post that u2 made
        blobbRepository.likeBlobbWithUuid(u1.getUuid(), b.getUuid());

        // check if the like was registered
        Optional<Long> result = blobbRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isPresent());

        // now unlike that post
        blobbRepository.unlikeBlobbWithUuid(u1.getUuid(), b.getUuid());

        // check if the unlike was registered
        result = blobbRepository
                .checkIfUserWithUuidLikes(u1.getUuid(), b.getUuid());

        assertTrue(result.isEmpty());
    }

    @Test
    public void getInfoAboutBlobbWithUuid_IsEmptyWhenUuidInvalid() {
        // when
        Optional<BlobbInfo> bInfo = blobbRepository.getInfoAboutBlobbWithUuid(UUID.randomUUID());

        // then
        assertTrue(bInfo.isEmpty());
    }

    @Test
    public void getInfoAboutBlobbWithUuid_IsEmptyWhenBlobbMarkedAsDeleted() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());

        Blobb b = createTestBlobb(test1, "test blobb", 0L);
        b.markAsDeleted();
        blobbRepository.save(b);

        // when
        Optional<BlobbInfo> bInfo = blobbRepository
                .getInfoAboutBlobbWithUuid(b.getUuid());

        // then
        assertTrue(bInfo.isEmpty());
    }

    @Test
    public void getInfoAboutBlobbWithUuid_HoldsCorrectCounterValues() {
        // create four users
        User u1 = createTestUser("u1");
        User u2 = createTestUser("u2");
        User u3 = createTestUser("u3");
        User u4 = createTestUser("u4");

        // post a blobb as u1
        Blobb u1Blobb = createTestBlobb(u1, "test", 0L);

        // create three responses to that blobb (and delete one)
        Blobb u2Response1 = new ResponseBlobb(u2, "test", u1Blobb);
        Blobb u2Response2 = new ResponseBlobb(u2, "test", u1Blobb);
        Blobb u2Response3 = new ResponseBlobb(u2, "test", u1Blobb);
        blobbRepository.save(u2Response1);
        blobbRepository.save(u2Response2);
        u2Response3.markAsDeleted(); // mark this response as deleted before saving
        blobbRepository.save(u2Response3);

        // create two reblobbs (and delete one)
        Blobb u2Reblobb1 = new Reblobb(u2, "test", u1Blobb);
        Blobb u2Reblobb2 = new Reblobb(u2, "test", u1Blobb);
        blobbRepository.save(u2Reblobb1);
        u2Reblobb2.markAsDeleted(); // mark this reblobb as deleted before saving
        blobbRepository.save(u2Reblobb2);

        // like the initial blobb as u2, u3, u4
        blobbRepository.likeBlobbWithUuid(u2.getUuid(), u1Blobb.getUuid());
        blobbRepository.likeBlobbWithUuid(u3.getUuid(), u1Blobb.getUuid());
        blobbRepository.likeBlobbWithUuid(u4.getUuid(), u1Blobb.getUuid());

        // when
        Optional<BlobbInfo> bInfo = blobbRepository.getInfoAboutBlobbWithUuid(u1Blobb.getUuid());

        // then
        assertTrue(bInfo.isPresent());

        // blobb should have 2 responses, 1 reblobb and 3 likes
        // because one response and one reblobb have been deleted
        assertEquals(2L, bInfo.get().getResponses());
        assertEquals(1L, bInfo.get().getReblobbs());
        assertEquals(3L, bInfo.get().getLikes());
    }

    @Test
    public void getAllResponsesToBlobbWithUuid_IsEmptyWhenNoResponses() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllResponsesToBlobbWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(0, blobbs.size());
    }

    @Test
    public void getAllResponsesToBlobbWithUuid_DoesNotListResponsesMarkedAsDeleted() {
        // create a user
        User u = createTestUser("u1");

        // create a parent post and two responses
        Blobb b = createTestBlobb(u, "content", 0L);
        Blobb r1 = new ResponseBlobb(u, "r1", b);
        Blobb r2 = new ResponseBlobb(u, "r2", b);
        r2.markAsDeleted(); // mark second response as deleted right away
        blobbRepository.save(r1);
        blobbRepository.save(r2);

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllResponsesToBlobbWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(1, blobbs.size());
    }

    @Test
    public void getAllResponsesToBlobbWithUuid_SkipArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // make 5 responses
        for (int i = 0; i < 5; i++) {
            Blobb b1 = new ResponseBlobb(u, "response " + i, b);
            blobbRepository.save(b1);
        }

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllResponsesToBlobbWithUuid(b.getUuid(), 2L, 10L);

        // then
        assertEquals(3, blobbs.size());
    }

    @Test
    public void getAllResponsesToBlobbWithUuid_LimitArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // make 5 responses
        for (int i = 0; i < 5; i++) {
            Blobb b1 = new ResponseBlobb(u, "response " + i, b);
            blobbRepository.save(b1);
        }

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllResponsesToBlobbWithUuid(b.getUuid(), 0L, 4L);

        // then
        assertEquals(4, blobbs.size());
    }

    @Test
    public void getAllResponsesToBlobbWithUuid_ReturnsCorrectObjects() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // make 5 responses
        for (int i = 0; i < 5; i++) {
            Blobb b1 = new ResponseBlobb(u, "response " + i, b);
            blobbRepository.save(b1);
        }

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllResponsesToBlobbWithUuid(b.getUuid(), 0L, 5L);

        // then
        assertEquals(5, blobbs.size());

        // response number 0 is the oldest one, expect it first
        for (int i = 0; i < blobbs.size(); i++) {
            FeedBlobb fBlobb = blobbs.get(i);

            assertTrue(fBlobb.getContent().contains("response " + i));
            assertEquals("u1", fBlobb.getAuthor().getUsername());
            assertEquals(b.getUuid(), fBlobb.getRespondsTo());
            assertNull(fBlobb.getReblobbs());
        }
    }

    @Test
    public void getAllReblobbsOfBlobbWithUuid_IsEmptyWhenNoResponses() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllReblobbsOfBlobbWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(0, blobbs.size());
    }

    @Test
    public void getAllReblobbsOfBlobbWithUuid_DoesNotListReblobbsMarkedAsDeleted() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);
        Blobb r1 = new Reblobb(u, "r1", b);
        Blobb r2 = new Reblobb(u, "r2", b);
        r2.markAsDeleted(); // mark second reblobb as deleted right away
        blobbRepository.save(r1);
        blobbRepository.save(r2);

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllReblobbsOfBlobbWithUuid(b.getUuid(), 0L, 10L);

        // then
        assertEquals(1, blobbs.size());
    }

    @Test
    public void getAllReblobbsOfBlobbWithUuid_SkipArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // make 5 reblobbs
        for (int i = 0; i < 5; i++) {
            Blobb b1 = new Reblobb(u, "reblobb " + i, b);
            blobbRepository.save(b1);
        }

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllReblobbsOfBlobbWithUuid(b.getUuid(), 2L, 10L);

        // then
        assertEquals(3, blobbs.size());
    }

    @Test
    public void getAllReblobbsOfBlobbWithUuid_LimitArgumentWorks() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // make 5 reblobbs
        for (int i = 0; i < 5; i++) {
            Blobb b1 = new Reblobb(u, "reblobb " + i, b);
            blobbRepository.save(b1);
        }

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllReblobbsOfBlobbWithUuid(b.getUuid(), 0L, 4L);

        // then
        assertEquals(4, blobbs.size());
    }

    @Test
    public void getAllReblobbsOfBlobbWithUuid_ReturnsCorrectObjects() {
        // create a user
        User u = createTestUser("u1");

        // create a single post
        Blobb b = createTestBlobb(u, "content", 0L);

        // make 5 reblobbs
        for (int i = 0; i < 5; i++) {
            Blobb b1 = new Reblobb(u, "reblobb " + i, b);
            blobbRepository.save(b1);
        }

        // when
        List<FeedBlobb> blobbs = blobbRepository.getAllReblobbsOfBlobbWithUuid(b.getUuid(), 0L, 5L);

        // then
        assertEquals(5, blobbs.size());

        // reblobb number 0 is the oldest one, expect it first
        for (int i = 0; i < blobbs.size(); i++) {
            FeedBlobb fBlobb = blobbs.get(i);

            assertTrue(fBlobb.getContent().contains("reblobb " + i));
            assertEquals("u1", fBlobb.getAuthor().getUsername());
            assertEquals(b.getUuid(), fBlobb.getReblobbs());
            assertNull(fBlobb.getRespondsTo());
        }
    }

    @Test
    public void getFeedForUserWithUuid_Popular_IsEmptyWhenUserDoesntExist() {
        Date date7DaysAgo = Date.from(Instant.now().minus(1, MINUTES));
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(UUID.randomUUID(),
                        date7DaysAgo,
                        new Date(), 0L, 10L);

        assertEquals(0, blobbs.size());
    }

    @Test
    public void getFeedForUserWithUuid_Popular_DoesNotContainDeletedPosts() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // create a post
        Blobb blobb = createTestBlobb(user, "200", 0L);
        blobb.markAsDeleted(); // mark as deleted even before saving it
        blobbRepository.save(blobb);

        Date date40MinAgo = Date.from(Instant.now().minus(40, MINUTES));

        // when
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(user.getUuid(),
                        date40MinAgo,
                        new Date(), 0L, 10L);

        // make a list of contents of retrieved posts
        List<String> contents = blobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(10, blobbs.size());
        // expect posts with content 19, 18, 17, 16, 15, 14, 13, 12, 11, 10
        Arrays.asList(19, 18, 17, 16, 15, 14, 13, 12, 11, 10).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_Popular_SkipArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        Date date40MinAgo = Date.from(Instant.now().minus(40, MINUTES));

        // when
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(user.getUuid(),
                        date40MinAgo,
                        new Date(), 5L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = blobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, blobbs.size());
        // expect posts with content: 14, 13, 12, 11, 10
        Arrays.asList(14, 13, 12, 11, 10).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_Popular_LimitArgumentWorks() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        Date date40MinAgo = Date.from(Instant.now().minus(40, MINUTES));

        // when
        List<FeedBlobb> blobbs = blobbRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(user.getUuid(),
                        date40MinAgo,
                        new Date(), 0L, 5L);

        // make a list of contents of retrieved posts
        List<String> contents = blobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(5, blobbs.size());
        // expect posts with content: 15, 16, 17, 18, 19
        Arrays.asList(15, 16, 17, 18, 19).forEach(i -> {
            assertTrue(contents.contains(i.toString()));
        });
    }

    @Test
    public void getFeedForUserWithUuid_Popular_FeedWithAllPostsIsSorted() {
        User user = userRepository.findByUsername("test1").orElse(new User());

        // using this date shows all of the posts, since the oldest posts are from 30 min before
        Date date40MinAgo = Date.from(Instant.now().minus(40, MINUTES));

        // when
        List<FeedBlobb> allBlobbs = blobbRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(user.getUuid(),
                        date40MinAgo,
                        new Date(), 0L, 20L); // limit to 20, to show all posts

        // make a list of contents of retrieved posts
        List<String> contents = allBlobbs.stream().map(FeedBlobb::getContent).collect(Collectors.toList());

        // then
        assertEquals(20, allBlobbs.size());

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
    public void getFeedForUserWithUuid_Popular_PostsHaveCorrectAuthors() {
        User test1 = userRepository.findByUsername("test1").orElse(new User());
        User test2 = userRepository.findByUsername("test2").orElse(new User());
        User test3 = userRepository.findByUsername("test3").orElse(new User());

        // using this date shows all of the posts, since the oldest posts are from 30 min before
        Date date40MinAgo = Date.from(Instant.now().minus(40, MINUTES));

        // when
        List<FeedBlobb> allBlobbs = blobbRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(test1.getUuid(),
                        date40MinAgo,
                        new Date(), 0L, 20L); // limit to 20, to show all posts

        // then
        // expected posts of test1: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        List<FeedBlobb> test1Posts = allBlobbs.stream().filter(b -> b.getAuthor().getUuid() == test1.getUuid())
                .collect(Collectors.toList());

        assertEquals(10, test1Posts.size());

        List<String> test1Contents = test1Posts.stream().map(FeedBlobb::getContent).collect(Collectors.toList());
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach(c -> {
            assertTrue(test1Contents.contains(c.toString()));
        });

        // expected posts of test2: 10, 11, 12, 13, 14
        List<FeedBlobb> test2Posts = allBlobbs.stream().filter(b -> b.getAuthor().getUuid() == test2.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test2Posts.size());

        List<String> test2Contents = test2Posts.stream().map(FeedBlobb::getContent).collect(Collectors.toList());
        Arrays.asList(10, 11, 12, 13, 14).forEach(c -> {
            assertTrue(test2Contents.contains(c.toString()));
        });

        // expected posts of test3: 15, 16, 17, 18, 19
        List<FeedBlobb> test3Posts = allBlobbs.stream().filter(b -> b.getAuthor().getUuid() == test3.getUuid())
                .collect(Collectors.toList());

        assertEquals(5, test3Posts.size());

        List<String> test3Contents = test3Posts.stream().map(FeedBlobb::getContent).collect(Collectors.toList());
        Arrays.asList(15, 16, 17, 18, 19).forEach(c -> {
            assertTrue(test3Contents.contains(c.toString()));
        });
    }
}
