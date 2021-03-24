package ml.echelon133.microblog.post;

import ml.echelon133.microblog.tag.Tag;
import ml.echelon133.microblog.tag.TagDoesntExistException;
import ml.echelon133.microblog.tag.TagService;
import ml.echelon133.microblog.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PostServiceTests {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostService postService;

    @Test
    public void getByUuid_ThrowsWhenPostDoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.getPostWithUuid(uuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.getByUuid(uuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getByUuid_ReturnsObject() throws Exception {
        UUID uuid = UUID.randomUUID();

        FeedPost post = new FeedPost();
        post.setUuid(uuid);

        // given
        given(postRepository.getPostWithUuid(uuid)).willReturn(Optional.of(post));

        // when
        FeedPost fPost = postService.getByUuid(uuid);

        // then
        assertEquals(uuid, fPost.getUuid());
    }

    @Test
    public void getPostInfo_ThrowsWhenPostDoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.getInfoAboutPostWithUuid(uuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.getPostInfo(uuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getPostInfo_ReturnsObject() throws Exception {
        UUID uuid = UUID.randomUUID();

        PostInfo info = new PostInfo();
        info.setUuid(uuid);

        // given
        given(postRepository.getInfoAboutPostWithUuid(uuid)).willReturn(Optional.of(info));

        // when
        PostInfo receivedInfo = postService.getPostInfo(uuid);

        // then
        assertEquals(info, receivedInfo);
    }

    @Test
    public void getAllResponsesTo_ThrowsWhenPostDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(uuid)).willReturn(false);

        // when
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.getAllResponsesTo(uuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals(String.format("Post with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getAllResponsesTo_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(uuid)).willReturn(true);

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getAllResponsesTo(uuid, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getAllResponsesTo(uuid, 0L, -5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void getAllResponsesTo_ReturnsEmptyListIfNobodyResponded() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(uuid)).willReturn(true);
        given(postRepository.getAllResponsesToPostWithUuid(uuid, 0L, 5L))
                .willReturn(List.of());

        // when
        List<FeedPost> responses = postService.getAllResponsesTo(uuid, 0L, 5L);

        // then
        assertEquals(0, responses.size());
    }

    @Test
    public void getAllResponsesTo_ReturnsListOfResponses() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<FeedPost> mockList = List.of(new FeedPost(), new FeedPost());

        // given
        given(postRepository.existsById(uuid)).willReturn(true);
        given(postRepository.getAllResponsesToPostWithUuid(uuid, 0L, 5L))
                .willReturn(mockList);

        // when
        List<FeedPost> responses = postService.getAllResponsesTo(uuid, 0L, 5L);

        // then
        assertEquals(2, responses.size());
    }

    @Test
    public void getAllQuotesOf_ThrowsWhenPostDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(uuid)).willReturn(false);

        // when
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.getAllQuotesOf(uuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals(String.format("Post with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getAllQuotesOf_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(uuid)).willReturn(true);

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getAllQuotesOf(uuid, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getAllQuotesOf(uuid, 0L, -5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void getAllQuotesOf_ReturnsEmptyListIfNobodyQuoted() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(uuid)).willReturn(true);
        given(postRepository.getAllQuotesOfPostWithUuid(uuid, 0L, 5L))
                .willReturn(List.of());

        // when
        List<FeedPost> responses = postService.getAllQuotesOf(uuid, 0L, 5L);

        // then
        assertEquals(0, responses.size());
    }

    @Test
    public void getAllQuotesOf_ReturnsListOfQuotes() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<FeedPost> mockList = List.of(new FeedPost(), new FeedPost());

        // given
        given(postRepository.existsById(uuid)).willReturn(true);
        given(postRepository.getAllQuotesOfPostWithUuid(uuid, 0L, 5L))
                .willReturn(mockList);

        // when
        List<FeedPost> responses = postService.getAllQuotesOf(uuid, 0L, 5L);

        // then
        assertEquals(2, responses.size());
    }

    @Test
    public void checkIfUserWithUuidLikes_ThrowsWhenPostDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(uuid)).willReturn(false);

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.checkIfUserWithUuidLikes(any(User.class), uuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void checkIfUserWithUuidLikes_ReturnsFalseWhenThereIsNoLike() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");

        // given
        given(postRepository.existsById(uuid)).willReturn(true);
        given(postRepository.checkIfUserWithUuidLikes(user.getUuid(), uuid))
                .willReturn(Optional.empty());

        // when
        boolean result = postService.checkIfUserWithUuidLikes(user, uuid);

        // then
        assertFalse(result);
    }

    @Test
    public void checkIfUserWithUuidLikes_ReturnsTrueWhenThereIsLike() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");

        // given
        given(postRepository.existsById(uuid)).willReturn(true);
        given(postRepository.checkIfUserWithUuidLikes(user.getUuid(), uuid))
                .willReturn(Optional.of(1L));
        // when
        boolean result = postService.checkIfUserWithUuidLikes(user, uuid);

        // then
        assertTrue(result);
    }

    @Test
    public void likePost_ThrowsWhenPostDoesntExist() {
        User user = new User("test1", "mail@test.com", "", "");
        UUID postUuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(postUuid)).willReturn(false);

        // when
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.likePost(user, postUuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", postUuid), message);
    }

    @Test
    public void likePost_WhenUserDoesntAlreadyLike() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID postUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(postRepository.existsById(postUuid)).willReturn(true);
        given(postRepository.checkIfUserWithUuidLikes(u1Uuid, postUuid))
                .willReturn(Optional.empty());
        given(postRepository.likePostWithUuid(u1Uuid, postUuid))
                .willReturn(Optional.of(1L));

        // when
        boolean result = postService.likePost(user, postUuid);

        // then
        assertTrue(result);
    }

    @Test
    public void likePost_WhenUserAlreadyLikes() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID postUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(postRepository.existsById(postUuid)).willReturn(true);
        given(postRepository.checkIfUserWithUuidLikes(u1Uuid, postUuid))
                .willReturn(Optional.of(1L));

        // when
        boolean result = postService.likePost(user, postUuid);

        // then
        assertTrue(result);
    }

    @Test
    public void likePost_WhenLikeFails() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID postUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(postRepository.existsById(postUuid)).willReturn(true);
        given(postRepository.checkIfUserWithUuidLikes(u1Uuid, postUuid))
                .willReturn(Optional.empty());
        given(postRepository.likePostWithUuid(u1Uuid, postUuid))
                .willReturn(Optional.empty());

        // when
        boolean result = postService.likePost(user, postUuid);

        // then
        assertFalse(result);
    }

    @Test
    public void unlikePost_ThrowsWhenPostDoesntExist() {
        User user = new User("test1", "mail@test.com", "", "");
        UUID postUuid = UUID.randomUUID();

        // given
        given(postRepository.existsById(postUuid)).willReturn(false);

        // when
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.unlikePost(user, postUuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", postUuid), message);
    }

    @Test
    public void unlikePost_WhenUnlikeSucceeds() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID postUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(postRepository.existsById(postUuid)).willReturn(true);
        given(postRepository.checkIfUserWithUuidLikes(u1Uuid, postUuid))
                .willReturn(Optional.empty());

        // when
        boolean result = postService.unlikePost(user, postUuid);

        // then
        assertTrue(result);
    }

    @Test
    public void unlikePost_WhenUnlikeFails() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID postUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(postRepository.existsById(postUuid)).willReturn(true);
        given(postRepository.checkIfUserWithUuidLikes(u1Uuid, postUuid))
                .willReturn(Optional.of(1L));

        // when
        boolean result = postService.unlikePost(user, postUuid);

        // then
        assertFalse(result);
    }

    @Test
    public void getFeedForUser_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uuid = UUID.randomUUID();
        User u = new User();

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getFeedForUser(u, IPostService.PostsSince.ONE_HOUR, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getFeedForUser(u, IPostService.PostsSince.ONE_HOUR, 0L, -1L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void getFeedForUser_ReturnsCorrectlyFilteredResults() {
        UUID uuid = UUID.randomUUID();
        User u = new User();
        u.setUuid(uuid);

        Date dateNow = new Date();
        Date dateOneHourAgo = Date.from(dateNow.toInstant().minus(1, HOURS));
        Date dateSixHoursAgo = Date.from(dateNow.toInstant().minus(6, HOURS));
        Date dateTwelveHoursAgo = Date.from(dateNow.toInstant().minus(12, HOURS));
        Date dayAgo = Date.from(dateNow.toInstant().minus(24, HOURS));

        // inject fixed clock into the service
        postService.setClock(Clock.fixed(dateNow.toInstant(), ZoneId.systemDefault()));

        // given
        given(postRepository
                .getFeedForUserWithUuid_PostedBetween(uuid, dateOneHourAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost()));
        given(postRepository
                .getFeedForUserWithUuid_PostedBetween(uuid, dateSixHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost()));
        given(postRepository
                .getFeedForUserWithUuid_PostedBetween(uuid, dateTwelveHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost(), new FeedPost()));
        given(postRepository
                .getFeedForUserWithUuid_PostedBetween(uuid, dayAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost(), new FeedPost(), new FeedPost()));

        // when
        List<FeedPost> oneHourResults = postService
                .getFeedForUser(u, IPostService.PostsSince.ONE_HOUR, 0L, 5L);

        List<FeedPost> sixHoursResults = postService
                .getFeedForUser(u, IPostService.PostsSince.SIX_HOURS, 0L, 5L);

        List<FeedPost> twelveHoursResults = postService
                .getFeedForUser(u, IPostService.PostsSince.TWELVE_HOURS, 0L, 5L);

        List<FeedPost> dayResults = postService
                .getFeedForUser(u, IPostService.PostsSince.DAY, 0L, 5L);

        // then
        assertEquals(1, oneHourResults.size());
        assertEquals(2, sixHoursResults.size());
        assertEquals(3, twelveHoursResults.size());
        assertEquals(4, dayResults.size());
    }

    @Test
    public void processPostAndSave_FindsNewTagsInContent() throws Exception {
        String expected1 = "test";
        String expected2 = "anothertest";
        String content = String.format("This is #%s and #%s", expected1, expected2);

        Post post = new Post(new User(), content);

        // given
        given(tagService.findByName(expected1))
                .willThrow(new TagDoesntExistException(expected1));
        given(tagService.findByName(expected2))
                .willThrow(new TagDoesntExistException(expected2));
        given(postRepository.save(post)).willReturn(post);

        // when
        Post processed = postService.processPostAndSave(post);

        // then
        assertEquals(2, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1));
        assertTrue(tagNames.contains(expected2));
    }

    @Test
    public void processPostAndSave_FindsExistingTagsInContent() throws Exception {
        String expected1 = "test";
        String expected2 = "anothertest";
        Tag tag1 = new Tag(expected1);
        Tag tag2 = new Tag(expected2);

        String content = String.format("This is #%s and #%s", expected1, expected2);

        Post post = new Post(new User(), content);

        // given
        given(tagService.findByName(expected1)).willReturn(tag1);
        given(tagService.findByName(expected2)).willReturn(tag2);
        given(postRepository.save(post)).willReturn(post);

        // when
        Post processed = postService.processPostAndSave(post);

        // then
        assertEquals(2, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1));
        assertTrue(tagNames.contains(expected2));

        assertTrue(processed.getTags().contains(tag1));
        assertTrue(processed.getTags().contains(tag2));
    }

    @Test
    public void processPostAndSave_OnlyFindsValidTagsInContent() throws Exception {
        String invalidTag1 = "a"; // too short (min length is 2)
        String expected1 = "C1"; // just right minimum length
        String expected2 = "DDDDDDDDDDdddddddddd"; // just right maximum length
        String expected3 = "bbbbbbbbbbbbbbbbbbbb"; // just right maximum length

        // add some trailing characters to expected3 to check if they are ignored
        String content = String.format("#%s and #%s. Also #%s and #%sbbbb", expected1, expected2, invalidTag1, expected3);

        Post post = new Post(new User(), content);

        // given
        given(tagService.findByName(expected1.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected1));
        given(tagService.findByName(expected2.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected2));
        given(tagService.findByName(expected3.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected3));
        given(postRepository.save(post)).willReturn(post);

        // when
        Post processed = postService.processPostAndSave(post);

        // then
        assertEquals(3, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1.toLowerCase()));
        assertTrue(tagNames.contains(expected2.toLowerCase()));
        assertTrue(tagNames.contains(expected3.toLowerCase()));
    }

    @Test
    public void processPostAndSave_DuplicateTagsCountOnlyOnce() throws Exception {
        String expected1 = "test";
        String duplicate1 = "test";
        String duplicate2 = "TEST";

        String content = String.format("#%s #%s #%s", expected1, duplicate1, duplicate2);

        Post post = new Post(new User(), content);

        // given
        given(tagService.findByName(expected1.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected1));
        given(postRepository.save(post)).willReturn(post);

        // when
        Post processed = postService.processPostAndSave(post);

        // then
        assertEquals(1, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1.toLowerCase()));
    }

    @Test
    public void postQuote_ThrowsWhenPostDoesntExist() {
        User author = new User("test1", "", "" ,"");
        UUID postUuid = UUID.randomUUID();

        String content = "Test";

        // given
        given(postRepository.findById(postUuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.postQuote(author, content, postUuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", postUuid), message);
    }

    @Test
    public void postQuote_ThrowsWhenPostMarkedAsDeleted() {
        User author = new User("test1", "", "" ,"");
        UUID postUuid = UUID.randomUUID();

        Post parentPost = new Post(author, "test");
        parentPost.setUuid(postUuid);
        parentPost.markAsDeleted();

        // given
        given(postRepository.findById(postUuid)).willReturn(Optional.of(parentPost));

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.postQuote(author, "test", postUuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", postUuid), message);
    }

    @Test
    public void postResponse_ThrowsWhenPostDoesntExist() {
        User author = new User("test1", "", "" ,"");
        UUID postUuid = UUID.randomUUID();

        String content = "Test";

        // given
        given(postRepository.findById(postUuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.postResponse(author, content, postUuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", postUuid), message);
    }

    @Test
    public void postResponse_ThrowsWhenPostMarkedAsDeleted() {
        User author = new User("test1", "", "" ,"");
        UUID postUuid = UUID.randomUUID();

        Post post = new Post(author, "test");
        post.setUuid(postUuid);
        post.markAsDeleted();

        // given
        given(postRepository.findById(postUuid)).willReturn(Optional.of(post));

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.postResponse(author, "test", postUuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", postUuid), message);
    }

    @Test
    public void markPostAsDeleted_ThrowsWhenPostDoesntExist() {
        User author = new User("test1", "", "" ,"");
        UUID postUuid = UUID.randomUUID();

        // given
        given(postRepository.findById(postUuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(PostDoesntExistException.class, () -> {
            postService.markPostAsDeleted(author, postUuid);
        }).getMessage();

        assertEquals(String.format("Post with UUID %s doesn't exist", postUuid), message);
    }

    @Test
    public void markPostAsDeleted_ThrowsWhenUserNotAuthorized() {
        // user that tries to delete
        User u1 = new User("u1", "", "", "");
        u1.setUuid(UUID.randomUUID());

        // actual author (only this user can delete his own posts)
        User author = new User("test1", "", "" ,"");
        author.setUuid(UUID.randomUUID());

        UUID postUuid = UUID.randomUUID();

        Post post = new Post(author, "test");
        post.setUuid(postUuid);

        // given
        given(postRepository.findById(postUuid)).willReturn(Optional.of(post));

        // then
        String message = assertThrows(UserCannotDeletePostException.class, () -> {
            postService.markPostAsDeleted(u1, postUuid);
        }).getMessage();

        assertEquals(String.format("User %s cannot delete post with %s uuid", u1.getUsername(), postUuid), message);
    }

    @Test
    public void markPostAsDeleted_ReturnsTrueWhenMarkedCorrectly() throws Exception {
        UUID postUuid = UUID.randomUUID();

        User author = new User("u1", "", "", "");
        author.setUuid(UUID.randomUUID());

        // to make sure that the post author's UUID and the loggedUser's UUID are compared by value and not
        // by address, make loggedUser's UUID a new object with same UUID value as the author's
        User loggedUser = new User("u1", "", "", "");
        loggedUser.setUuid(UUID.fromString(author.getUuid().toString()));

        Post post = new Post(author, "test");
        post.setUuid(postUuid);

        // given
        given(postRepository.findById(postUuid)).willReturn(Optional.of(post));
        given(postRepository.save(post)).willReturn(post);

        // when
        boolean response = postService.markPostAsDeleted(loggedUser, postUuid);

        // then
        assertTrue(response);
    }

    @Test
    public void getFeedForUser_Popular_ThrowsWhenSkipAndLimitArgumentsNegative() {
        User u = new User();

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getFeedForUser_Popular(u, IPostService.PostsSince.ONE_HOUR, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getFeedForUser_Popular(u, IPostService.PostsSince.ONE_HOUR, 0L, -1L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void getFeedForUser_Popular_ReturnsCorrectlyFilteredResults() {
        UUID uuid = UUID.randomUUID();
        User u = new User();
        u.setUuid(uuid);

        Date dateNow = new Date();
        Date dateOneHourAgo = Date.from(dateNow.toInstant().minus(1, HOURS));
        Date dateSixHoursAgo = Date.from(dateNow.toInstant().minus(6, HOURS));
        Date dateTwelveHoursAgo = Date.from(dateNow.toInstant().minus(12, HOURS));
        Date dayAgo = Date.from(dateNow.toInstant().minus(24, HOURS));

        // inject fixed clock into the service
        postService.setClock(Clock.fixed(dateNow.toInstant(), ZoneId.systemDefault()));

        // given
        given(postRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(uuid, dateOneHourAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost()));
        given(postRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(uuid, dateSixHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost()));
        given(postRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(uuid, dateTwelveHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost(), new FeedPost()));
        given(postRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(uuid, dayAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost(), new FeedPost(), new FeedPost()));

        // when
        List<FeedPost> oneHourResults = postService
                .getFeedForUser_Popular(u, IPostService.PostsSince.ONE_HOUR, 0L, 5L);

        List<FeedPost> sixHoursResults = postService
                .getFeedForUser_Popular(u, IPostService.PostsSince.SIX_HOURS, 0L, 5L);

        List<FeedPost> twelveHoursResults = postService
                .getFeedForUser_Popular(u, IPostService.PostsSince.TWELVE_HOURS, 0L, 5L);

        List<FeedPost> dayResults = postService
                .getFeedForUser_Popular(u, IPostService.PostsSince.DAY, 0L, 5L);


        // then
        assertEquals(1, oneHourResults.size());
        assertEquals(2, sixHoursResults.size());
        assertEquals(3, twelveHoursResults.size());
        assertEquals(4, dayResults.size());
    }

    @Test
    public void getFeedForAnonymousUser_ThrowsWhenSkipAndLimitArgumentsNegative() {

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getFeedForAnonymousUser(IPostService.PostsSince.ONE_HOUR, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            postService.getFeedForAnonymousUser(IPostService.PostsSince.ONE_HOUR, 0L, -1L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void getFeedForAnonymousUser_ReturnsCorrectlyFilteredResults() {

        Date dateNow = new Date();
        Date dateOneHourAgo = Date.from(dateNow.toInstant().minus(1, HOURS));
        Date dateSixHoursAgo = Date.from(dateNow.toInstant().minus(6, HOURS));
        Date dateTwelveHoursAgo = Date.from(dateNow.toInstant().minus(12, HOURS));
        Date dayAgo = Date.from(dateNow.toInstant().minus(24, HOURS));

        // inject fixed clock into the service
        postService.setClock(Clock.fixed(dateNow.toInstant(), ZoneId.systemDefault()));

        // given
        given(postRepository
                .getFeedForAnonymousUser_Popular_PostedBetween(dateOneHourAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost()));
        given(postRepository
                .getFeedForAnonymousUser_Popular_PostedBetween(dateSixHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost()));
        given(postRepository
                .getFeedForAnonymousUser_Popular_PostedBetween(dateTwelveHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost(), new FeedPost()));
        given(postRepository
                .getFeedForAnonymousUser_Popular_PostedBetween(dayAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedPost(), new FeedPost(), new FeedPost(), new FeedPost()));

        // when
        List<FeedPost> oneHourResults = postService
                .getFeedForAnonymousUser(IPostService.PostsSince.ONE_HOUR, 0L, 5L);

        List<FeedPost> sixHoursResults = postService
                .getFeedForAnonymousUser(IPostService.PostsSince.SIX_HOURS, 0L, 5L);

        List<FeedPost> twelveHoursResults = postService
                .getFeedForAnonymousUser(IPostService.PostsSince.TWELVE_HOURS, 0L, 5L);

        List<FeedPost> dayResults = postService
                .getFeedForAnonymousUser(IPostService.PostsSince.DAY, 0L, 5L);


        // then
        assertEquals(1, oneHourResults.size());
        assertEquals(2, sixHoursResults.size());
        assertEquals(3, twelveHoursResults.size());
        assertEquals(4, dayResults.size());
    }
}
