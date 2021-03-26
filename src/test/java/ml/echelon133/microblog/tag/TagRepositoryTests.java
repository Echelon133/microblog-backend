package ml.echelon133.microblog.tag;

import ml.echelon133.microblog.post.Post;
import ml.echelon133.microblog.post.PostRepository;
import ml.echelon133.microblog.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class TagRepositoryTests {

    private TagRepository tagRepository;

    private PostRepository postRepository;

    @Autowired
    public TagRepositoryTests(TagRepository tagRepository,
                              PostRepository postRepository) {
        this.tagRepository = tagRepository;
        this.postRepository = postRepository;
    }

    private User createTestUser() {
        return new User("test", "", "", "");
    }

    private Tag createTag(String name) {
        return new Tag(name);
    }

    private Post createPost(User author, String content, Tag... tags) {
        Post b = new Post(author, content);
        for (Tag t : tags) {
            b.addTag(t);
        }
        return b;
    }

    @BeforeEach
    private void beforeEach() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    public void savedTagGetsUuid() {
        Tag tag = createTag("#test");
        assertNull(tag.getUuid());

        Tag savedTag = tagRepository.save(tag);
        assertNotNull(savedTag.getUuid());
    }

    @Test
    public void findByName_FindsExistingTags() {
        Tag tag = createTag("#test");
        Tag savedTag = tagRepository.save(tag);

        Optional<Tag> foundTag = tagRepository.findByName("#test");

        assertEquals(savedTag, foundTag.orElse(null));
    }

    @Test
    public void findMostPopularTags_DoesNotCountDeletedPosts() {
        User u1 = createTestUser();

        // create a single post tagged '#test' and mark it as deleted
        Post b1 = createPost(u1, "test", createTag("#test"));
        b1.markAsDeleted();
        postRepository.save(b1);

        // create another post tagged '#asdf' and don't delete it
        Post b2 = createPost(u1, "test", createTag("#asdf"));
        postRepository.save(b2);

        // when
        Date hourAgo = Date.from(Instant.now().minus(1, HOURS));
        List<Tag> popular = tagRepository.findMostPopularTags_Between(hourAgo, new Date(), 10L);

        // then
        assertEquals(1L, popular.size());
        List<String> tagNames = popular.stream().map(Tag::getName).collect(Collectors.toList());
        assertTrue(tagNames.contains("#asdf"));
    }

    @Test
    public void findMostPopularTags_CorrectlyCalculatesPopularity() {
        User u1 = createTestUser();

        Tag test1 = tagRepository.save(createTag("#test1"));
        Tag test2 = tagRepository.save(createTag("#test2"));
        Tag test3 = tagRepository.save(createTag("#test3"));
        Tag test4 = tagRepository.save(createTag("#test4"));

        // make 100 posts tagged '#test1'
        for (int i = 0; i < 100; i++) {
            Post b = createPost(u1, "content", test1);
            postRepository.save(b);
        }

        // make 200 posts tagged '#test2'
        for (int i = 0; i < 200; i++) {
            Post b = createPost(u1, "content", test2);
            postRepository.save(b);
        }

        // make 300 posts tagged '#test3'
        for (int i = 0; i < 300; i++) {
            Post b = createPost(u1, "content", test3);
            postRepository.save(b);
        }

        // make 400 posts tagged '#test4'
        for (int i = 0; i < 400; i++) {
            Post b = createPost(u1, "content", test4);
            postRepository.save(b);
        }

        // when
        Date hourAgo = Date.from(Instant.now().minus(1, HOURS));
        List<Tag> popular = tagRepository.findMostPopularTags_Between(hourAgo, new Date(), 10L);

        // then
        assertEquals(4, popular.size());

        List<String> tagNames = popular.stream().map(Tag::getName).collect(Collectors.toList());
        List<String> expectedOrder = Arrays.asList("#test4", "#test3", "#test2", "#test1");

        for (int i = 0; i < popular.size(); i++) {
            assertEquals(expectedOrder.get(i), tagNames.get(i));
        }
    }

    @Test
    public void findMostPopularTags_IgnoresPostsNotCreatedBetweenDates() {
        Date hourAgo = Date.from(Instant.now().minus(1, HOURS));
        Date twoHoursAgo = Date.from(Instant.now().minus(2, HOURS));
        Date weekAgo = Date.from(Instant.now().minus(7, DAYS));
        Date twoWeeksAgo = Date.from(Instant.now().minus(14, DAYS));

        User u1 = createTestUser();

        Tag test1 = tagRepository.save(createTag("#test1"));
        Tag test2 = tagRepository.save(createTag("#test2"));
        Tag test3 = tagRepository.save(createTag("#test3"));
        Tag test4 = tagRepository.save(createTag("#test4"));

        // make 100 posts tagged '#test1' (created now)
        for (int i = 0; i < 100; i++) {
            Post b = createPost(u1, "content", test1);
            postRepository.save(b);
        }

        // make 200 posts tagged '#test2' (created two hours ago)
        for (int i = 0; i < 200; i++) {
            Post b = createPost(u1, "content", test2);
            b.setCreationDate(twoHoursAgo);
            postRepository.save(b);
        }

        // make 300 posts tagged '#test3' (created two hours ago)
        for (int i = 0; i < 300; i++) {
            Post b = createPost(u1, "content", test3);
            b.setCreationDate(twoHoursAgo);
            postRepository.save(b);
        }

        // make 400 posts tagged '#test4' (created a week ago)
        for (int i = 0; i < 400; i++) {
            Post b = createPost(u1, "content", test4);
            b.setCreationDate(weekAgo);
            postRepository.save(b);
        }

        // when
        // popular in the last hour
        List<Tag> popular1 = tagRepository.findMostPopularTags_Between(hourAgo, new Date(), 10L);
        // popular between two weeks ago and one hour ago
        List<Tag> popular2 = tagRepository.findMostPopularTags_Between(twoWeeksAgo, hourAgo, 10L);

        // then
        // only one tag has been used in the last hour
        assertEquals(1, popular1.size());
        // three tags have been used in the second case
        assertEquals(3, popular2.size());

        List<String> tagNames1 = popular1.stream().map(Tag::getName).collect(Collectors.toList());
        List<String> tagNames2 = popular2.stream().map(Tag::getName).collect(Collectors.toList());

        List<String> expectedOrder1 = Collections.singletonList("#test1");
        List<String> expectedOrder2 = Arrays.asList("#test4", "#test3", "#test2");

        for (int i = 0; i < popular1.size(); i++) {
            assertEquals(expectedOrder1.get(i), tagNames1.get(i));
        }
        for (int i = 0; i < popular2.size(); i++) {
            assertEquals(expectedOrder2.get(i), tagNames2.get(i));
        }
    }

    @Test
    public void findMostPopularTags_LimitsNumberOfResults() {
        User u1 = createTestUser();

        Tag test1 = tagRepository.save(createTag("#test1"));
        Tag test2 = tagRepository.save(createTag("#test2"));
        Tag test3 = tagRepository.save(createTag("#test3"));
        Tag test4 = tagRepository.save(createTag("#test4"));

        // make 100 posts tagged '#test1'
        for (int i = 0; i < 100; i++) {
            Post b = createPost(u1, "content", test1);
            postRepository.save(b);
        }

        // make 200 posts tagged '#test2'
        for (int i = 0; i < 200; i++) {
            Post b = createPost(u1, "content", test2);
            postRepository.save(b);
        }

        // make 300 posts tagged '#test3'
        for (int i = 0; i < 300; i++) {
            Post b = createPost(u1, "content", test3);
            postRepository.save(b);
        }

        // make 400 posts tagged '#test4'
        for (int i = 0; i < 400; i++) {
            Post b = createPost(u1, "content", test4);
            postRepository.save(b);
        }

        // when
        Date hourAgo = Date.from(Instant.now().minus(1, HOURS));
        List<Tag> popular = tagRepository.findMostPopularTags_Between(hourAgo, new Date(), 2L);

        // then
        assertEquals(2, popular.size());

        List<String> tagNames = popular.stream().map(Tag::getName).collect(Collectors.toList());
        List<String> expectedOrder = Arrays.asList("#test4", "#test3");

        for (int i = 0; i < popular.size(); i++) {
            assertEquals(expectedOrder.get(i), tagNames.get(i));
        }
    }

    @Test
    public void findRecentPostsTagged_IsEmptyWhenNoPostsMade() {
        Tag t = createTag("#tag");
        UUID tagUuid = tagRepository.save(t).getUuid();

        // when
        List<RecentPost> recent = tagRepository.findRecentPostsTagged(tagUuid, 0L, 10L);

        // then
        assertEquals(0, recent.size());
    }

    @Test
    public void findRecentPostsTagged_ReturnsObjectsInCorrectOrder() {
        Tag savedTag = tagRepository.save(createTag("#test"));

        Date now = new Date();
        Date hourAgo = Date.from(Instant.now().minus(1, HOURS));
        Date twoHoursAgo = Date.from(Instant.now().minus(2, HOURS));

        // create posts
        Post b1 = createPost(createTestUser(), "content1", savedTag);
        b1.setCreationDate(now);

        Post b2 = createPost(createTestUser(), "content2", savedTag);
        b2.setCreationDate(hourAgo);

        Post b3 = createPost(createTestUser(), "content3", savedTag);
        b3.setCreationDate(twoHoursAgo);

        postRepository.save(b1);
        postRepository.save(b2);
        postRepository.save(b3);

        // when
        List<RecentPost> recent = tagRepository.findRecentPostsTagged(savedTag.getUuid(), 0L, 10L);

        // then
        List<String> recentContents = recent.stream().map(RecentPost::getContent).collect(Collectors.toList());

        assertEquals(3, recent.size());

        List<String> expectedOrder = List.of("content1", "content2", "content3");

        for (int i = 0; i < recent.size(); i++) {
            assertEquals(expectedOrder.get(i), recentContents.get(i));
        }
    }

    @Test
    public void findRecentPostsTagged_LimitsNumberOfResults() {
        Tag savedTag = tagRepository.save(createTag("#test"));

        Date now = new Date();
        Date hourAgo = Date.from(Instant.now().minus(1, HOURS));
        Date twoHoursAgo = Date.from(Instant.now().minus(2, HOURS));

        // create posts
        Post b1 = createPost(createTestUser(), "content1", savedTag);
        b1.setCreationDate(now);

        Post b2 = createPost(createTestUser(), "content2", savedTag);
        b2.setCreationDate(hourAgo);

        Post b3 = createPost(createTestUser(), "content3", savedTag);
        b3.setCreationDate(twoHoursAgo);

        postRepository.save(b1);
        postRepository.save(b2);
        postRepository.save(b3);

        // when
        List<RecentPost> recent = tagRepository.findRecentPostsTagged(savedTag.getUuid(), 0L, 1L);

        // then
        List<String> recentContents = recent.stream().map(RecentPost::getContent).collect(Collectors.toList());

        assertEquals(1, recent.size());

        List<String> expectedOrder = Collections.singletonList("content1");

        for (int i = 0; i < recent.size(); i++) {
            assertEquals(expectedOrder.get(i), recentContents.get(i));
        }
    }

    @Test
    public void findRecentPostsTagged_SkipsResults() {
        Tag savedTag = tagRepository.save(createTag("#test"));

        Date now = new Date();
        Date hourAgo = Date.from(Instant.now().minus(1, HOURS));
        Date twoHoursAgo = Date.from(Instant.now().minus(2, HOURS));

        // create posts
        Post b1 = createPost(createTestUser(), "content1", savedTag);
        b1.setCreationDate(now);

        Post b2 = createPost(createTestUser(), "content2", savedTag);
        b2.setCreationDate(hourAgo);

        Post b3 = createPost(createTestUser(), "content3", savedTag);
        b3.setCreationDate(twoHoursAgo);

        postRepository.save(b1);
        postRepository.save(b2);
        postRepository.save(b3);

        // when
        List<RecentPost> recent = tagRepository.findRecentPostsTagged(savedTag.getUuid(), 1L, 10L);

        // then
        List<String> recentContents = recent.stream().map(RecentPost::getContent).collect(Collectors.toList());

        assertEquals(2, recent.size());

        List<String> expectedOrder = List.of("content2", "content3");

        for (int i = 0; i < recent.size(); i++) {
            assertEquals(expectedOrder.get(i), recentContents.get(i));
        }
    }
}
