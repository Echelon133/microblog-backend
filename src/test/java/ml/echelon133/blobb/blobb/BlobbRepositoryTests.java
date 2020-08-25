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

    @BeforeEach
    public void beforeEach() {
        // three test users
        User test1 = new User("test1", "", "", "");
        User test2 = new User("test2", "", "", "");
        User test3 = new User("test3", "", "", "");

        User savedTest1 = userRepository.save(test1);
        User savedTest2 = userRepository.save(test2);
        User savedTest3 = userRepository.save(test3);

        // "test1" follows "test2" and "test3"
        userRepository.followUserWithUuid(test1.getUuid(), test1.getUuid()); // every user must follow themselves
        userRepository.followUserWithUuid(test1.getUuid(), test2.getUuid());
        userRepository.followUserWithUuid(test1.getUuid(), test3.getUuid());

        // create 10 posts as "test1" (5 posts made 30 min ago, 5 made 10 min ago)
        for (int i = 0; i < 5; i++) {
            Date date30MinAgo = Date.from(Instant.now().minus(30, MINUTES));
            Blobb blobb = new Blobb(savedTest1, "" + i);
            blobb.setCreationDate(date30MinAgo);
            blobbRepository.save(blobb);
        }
        for (int i = 5; i < 10; i++) {
            Date date10MinAgo = Date.from(Instant.now().minus(10, MINUTES));
            Blobb blobb = new Blobb(savedTest1, "" + i);
            blobb.setCreationDate(date10MinAgo);
            blobbRepository.save(blobb);
        }

        // create 10 posts as "test2" (all of them made 30 min ago)
        for (int i = 10; i < 15; i++) {
            Date date30MinAgo = Date.from(Instant.now().minus(30, MINUTES));
            Blobb blobb = new Blobb(savedTest2, "" + i);
            blobb.setCreationDate(date30MinAgo);
            blobbRepository.save(blobb);
        }

        // create 5 posts as "test3" (all of them made 1 min ago)
        for (int i = 15; i < 20; i++) {
            Date date1MinAgo = Date.from(Instant.now().minus(1, MINUTES));
            Blobb blobb = new Blobb(savedTest3, "" + i);
            blobb.setCreationDate(date1MinAgo);
            blobbRepository.save(blobb);
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
}
