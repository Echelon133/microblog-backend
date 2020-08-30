package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.tag.Tag;
import ml.echelon133.blobb.tag.TagDoesntExistException;
import ml.echelon133.blobb.tag.TagService;
import ml.echelon133.blobb.user.User;
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
public class BlobbServiceTests {

    @Mock
    private BlobbRepository blobbRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private BlobbService blobbService;

    @Test
    public void getByUuid_ThrowsWhenBlobbDoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.getBlobbWithUuid(uuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(BlobbDoesntExistException.class, () -> {
            blobbService.getByUuid(uuid);
        }).getMessage();

        assertEquals(String.format("Blobb with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getByUuid_ReturnsObject() throws Exception {
        UUID uuid = UUID.randomUUID();

        FeedBlobb blobb = new FeedBlobb();
        blobb.setUuid(uuid);

        // given
        given(blobbRepository.getBlobbWithUuid(uuid)).willReturn(Optional.of(blobb));

        // when
        FeedBlobb fBlobb = blobbService.getByUuid(uuid);

        // then
        assertEquals(uuid, fBlobb.getUuid());
    }

    @Test
    public void getBlobbInfo_ThrowsWhenBlobbDoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.getInfoAboutBlobbWithUuid(uuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(BlobbDoesntExistException.class, () -> {
            blobbService.getBlobbInfo(uuid);
        }).getMessage();

        assertEquals(String.format("Blobb with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getBlobbInfo_ReturnsObject() throws Exception {
        UUID uuid = UUID.randomUUID();

        BlobbInfo info = new BlobbInfo();
        info.setUuid(uuid);

        // given
        given(blobbRepository.getInfoAboutBlobbWithUuid(uuid)).willReturn(Optional.of(info));

        // when
        BlobbInfo receivedInfo = blobbService.getBlobbInfo(uuid);

        // then
        assertEquals(info, receivedInfo);
    }

    @Test
    public void getAllResponsesTo_ThrowsWhenBlobbDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(uuid)).willReturn(false);

        // when
        String message = assertThrows(BlobbDoesntExistException.class, () -> {
            blobbService.getAllResponsesTo(uuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals(String.format("Blobb with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getAllResponsesTo_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            blobbService.getAllResponsesTo(uuid, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            blobbService.getAllResponsesTo(uuid, 0L, -5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void getAllResponsesTo_ReturnsEmptyListIfNobodyResponded() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);
        given(blobbRepository.getAllResponsesToBlobbWithUuid(uuid, 0L, 5L))
                .willReturn(List.of());

        // when
        List<FeedBlobb> responses = blobbService.getAllResponsesTo(uuid, 0L, 5L);

        // then
        assertEquals(0, responses.size());
    }

    @Test
    public void getAllResponsesTo_ReturnsListOfResponses() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<FeedBlobb> mockList = List.of(new FeedBlobb(), new FeedBlobb());

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);
        given(blobbRepository.getAllResponsesToBlobbWithUuid(uuid, 0L, 5L))
                .willReturn(mockList);

        // when
        List<FeedBlobb> responses = blobbService.getAllResponsesTo(uuid, 0L, 5L);

        // then
        assertEquals(2, responses.size());
    }

    @Test
    public void getAllReblobbsOf_ThrowsWhenBlobbDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(uuid)).willReturn(false);

        // when
        String message = assertThrows(BlobbDoesntExistException.class, () -> {
            blobbService.getAllReblobbsOf(uuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals(String.format("Blobb with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void getAllReblobbsOf_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            blobbService.getAllReblobbsOf(uuid, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            blobbService.getAllReblobbsOf(uuid, 0L, -5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void getAllReblobbsOf_ReturnsEmptyListIfNobodyReblobbed() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);
        given(blobbRepository.getAllReblobbsOfBlobbWithUuid(uuid, 0L, 5L))
                .willReturn(List.of());

        // when
        List<FeedBlobb> responses = blobbService.getAllReblobbsOf(uuid, 0L, 5L);

        // then
        assertEquals(0, responses.size());
    }

    @Test
    public void getAllReblobbsOf_ReturnsListOfReblobbs() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<FeedBlobb> mockList = List.of(new FeedBlobb(), new FeedBlobb());

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);
        given(blobbRepository.getAllReblobbsOfBlobbWithUuid(uuid, 0L, 5L))
                .willReturn(mockList);

        // when
        List<FeedBlobb> responses = blobbService.getAllReblobbsOf(uuid, 0L, 5L);

        // then
        assertEquals(2, responses.size());
    }

    @Test
    public void checkIfUserWithUuidLikes_ThrowsWhenBlobbDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(uuid)).willReturn(false);

        // then
        String message = assertThrows(BlobbDoesntExistException.class, () -> {
            blobbService.checkIfUserWithUuidLikes(any(User.class), uuid);
        }).getMessage();

        assertEquals(String.format("Blobb with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void checkIfUserWithUuidLikes_ReturnsFalseWhenThereIsNoLike() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);
        given(blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), uuid))
                .willReturn(Optional.empty());

        // when
        boolean result = blobbService.checkIfUserWithUuidLikes(user, uuid);

        // then
        assertFalse(result);
    }

    @Test
    public void checkIfUserWithUuidLikes_ReturnsTrueWhenThereIsLike() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");

        // given
        given(blobbRepository.existsById(uuid)).willReturn(true);
        given(blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), uuid))
                .willReturn(Optional.of(1L));
        // when
        boolean result = blobbService.checkIfUserWithUuidLikes(user, uuid);

        // then
        assertTrue(result);
    }

    @Test
    public void likeBlobb_ThrowsWhenBlobbDoesntExist() {
        User user = new User("test1", "mail@test.com", "", "");
        UUID blobbUuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(blobbUuid)).willReturn(false);

        // when
        String message = assertThrows(BlobbDoesntExistException.class, () -> {
            blobbService.likeBlobb(user, blobbUuid);
        }).getMessage();

        assertEquals(String.format("Blobb with UUID %s doesn't exist", blobbUuid), message);
    }

    @Test
    public void likeBlobb_WhenUserDoesntAlreadyLike() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID blobbUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(blobbRepository.existsById(blobbUuid)).willReturn(true);
        given(blobbRepository.checkIfUserWithUuidLikes(u1Uuid, blobbUuid))
                .willReturn(Optional.empty());
        given(blobbRepository.likeBlobbWithUuid(u1Uuid, blobbUuid))
                .willReturn(Optional.of(1L));

        // when
        boolean result = blobbService.likeBlobb(user, blobbUuid);

        // then
        assertTrue(result);
    }

    @Test
    public void likeBlobb_WhenUserAlreadyLikes() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID blobbUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(blobbRepository.existsById(blobbUuid)).willReturn(true);
        given(blobbRepository.checkIfUserWithUuidLikes(u1Uuid, blobbUuid))
                .willReturn(Optional.of(1L));

        // when
        boolean result = blobbService.likeBlobb(user, blobbUuid);

        // then
        assertTrue(result);
    }

    @Test
    public void likeBlobb_WhenLikeFails() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID blobbUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(blobbRepository.existsById(blobbUuid)).willReturn(true);
        given(blobbRepository.checkIfUserWithUuidLikes(u1Uuid, blobbUuid))
                .willReturn(Optional.empty());
        given(blobbRepository.likeBlobbWithUuid(u1Uuid, blobbUuid))
                .willReturn(Optional.empty());

        // when
        boolean result = blobbService.likeBlobb(user, blobbUuid);

        // then
        assertFalse(result);
    }

    @Test
    public void unlikeBlobb_ThrowsWhenBlobbDoesntExist() {
        User user = new User("test1", "mail@test.com", "", "");
        UUID blobbUuid = UUID.randomUUID();

        // given
        given(blobbRepository.existsById(blobbUuid)).willReturn(false);

        // when
        String message = assertThrows(BlobbDoesntExistException.class, () -> {
            blobbService.unlikeBlobb(user, blobbUuid);
        }).getMessage();

        assertEquals(String.format("Blobb with UUID %s doesn't exist", blobbUuid), message);
    }

    @Test
    public void unlikeBlobb_WhenUnlikeSucceeds() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID blobbUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(blobbRepository.existsById(blobbUuid)).willReturn(true);
        given(blobbRepository.checkIfUserWithUuidLikes(u1Uuid, blobbUuid))
                .willReturn(Optional.empty());

        // when
        boolean result = blobbService.unlikeBlobb(user, blobbUuid);

        // then
        assertTrue(result);
    }

    @Test
    public void unlikeBlobb_WhenUnlikeFails() throws Exception {
        UUID u1Uuid = UUID.randomUUID();
        User user = new User("test1", "mail@test.com", "", "");
        UUID blobbUuid = UUID.randomUUID();
        user.setUuid(u1Uuid);

        // given
        given(blobbRepository.existsById(blobbUuid)).willReturn(true);
        given(blobbRepository.checkIfUserWithUuidLikes(u1Uuid, blobbUuid))
                .willReturn(Optional.of(1L));

        // when
        boolean result = blobbService.unlikeBlobb(user, blobbUuid);

        // then
        assertFalse(result);
    }

    @Test
    public void getFeedForUser_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uuid = UUID.randomUUID();
        User u = new User();

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            blobbService.getFeedForUser(u, IBlobbService.BlobbsSince.ONE_HOUR, -1L, 5L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            blobbService.getFeedForUser(u, IBlobbService.BlobbsSince.ONE_HOUR, 0L, -1L);
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

        // inject fixed clock into the service
        blobbService.setClock(Clock.fixed(dateNow.toInstant(), ZoneId.systemDefault()));

        // given
        given(blobbRepository
                .getFeedForUserWithUuid_PostedBetween(uuid, dateOneHourAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedBlobb()));
        given(blobbRepository
                .getFeedForUserWithUuid_PostedBetween(uuid, dateSixHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedBlobb(), new FeedBlobb()));
        given(blobbRepository
                .getFeedForUserWithUuid_PostedBetween(uuid, dateTwelveHoursAgo, dateNow, 0L, 5L))
                .willReturn(List.of(new FeedBlobb(), new FeedBlobb(), new FeedBlobb()));

        // when
        List<FeedBlobb> oneHourResults = blobbService
                .getFeedForUser(u, IBlobbService.BlobbsSince.ONE_HOUR, 0L, 5L);

        List<FeedBlobb> sixHoursResults = blobbService
                .getFeedForUser(u, IBlobbService.BlobbsSince.SIX_HOURS, 0L, 5L);

        List<FeedBlobb> twelveHoursResults = blobbService
                .getFeedForUser(u, IBlobbService.BlobbsSince.TWELVE_HOURS, 0L, 5L);


        // then
        assertEquals(1, oneHourResults.size());
        assertEquals(2, sixHoursResults.size());
        assertEquals(3, twelveHoursResults.size());
    }

    @Test
    public void processBlobbAndSave_FindsNewTagsInContent() throws Exception {
        String expected1 = "#test";
        String expected2 = "#anothertest";
        String content = "This is " + expected1 + " and " + expected2;

        Blobb blobb = new Blobb(new User(), content);

        // given
        given(tagService.findByName(expected1))
                .willThrow(new TagDoesntExistException(expected1));
        given(tagService.findByName(expected2))
                .willThrow(new TagDoesntExistException(expected2));
        given(blobbRepository.save(blobb)).willReturn(blobb);

        // when
        Blobb processed = blobbService.processBlobbAndSave(blobb);

        // then
        assertEquals(2, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1));
        assertTrue(tagNames.contains(expected2));
    }

    @Test
    public void processBlobbAndSave_FindsExistingTagsInContent() throws Exception {
        String expected1 = "#test";
        String expected2 = "#anothertest";
        Tag tag1 = new Tag(expected1);
        Tag tag2 = new Tag(expected2);

        String content = "This is " + expected1 + " and " + expected2;

        Blobb blobb = new Blobb(new User(), content);

        // given
        given(tagService.findByName(expected1)).willReturn(tag1);
        given(tagService.findByName(expected2)).willReturn(tag2);
        given(blobbRepository.save(blobb)).willReturn(blobb);

        // when
        Blobb processed = blobbService.processBlobbAndSave(blobb);

        // then
        assertEquals(2, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1));
        assertTrue(tagNames.contains(expected2));

        assertTrue(processed.getTags().contains(tag1));
        assertTrue(processed.getTags().contains(tag2));
    }

    @Test
    public void processBlobbAndSave_OnlyFindsValidTagsInContent() throws Exception {
        String invalidTag1 = "#a"; // too short (min length is 2)
        String expected1 = "#C1"; // just right minimum length
        String expected2 = "#DDDDDDDDDDdddddddddd"; // just right maximum length
        String expected3 = "#bbbbbbbbbbbbbbbbbbbb"; // just right maximum length

        // add some trailing characters to expected3 to check if they are ignored
        String content = expected1 + " and " + expected2 + ". Also "
                + invalidTag1 + " and " + expected3 + "bbbb";

        Blobb blobb = new Blobb(new User(), content);

        // given
        given(tagService.findByName(expected1.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected1));
        given(tagService.findByName(expected2.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected2));
        given(tagService.findByName(expected3.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected3));
        given(blobbRepository.save(blobb)).willReturn(blobb);

        // when
        Blobb processed = blobbService.processBlobbAndSave(blobb);

        // then
        assertEquals(3, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1.toLowerCase()));
        assertTrue(tagNames.contains(expected2.toLowerCase()));
        assertTrue(tagNames.contains(expected3.toLowerCase()));
    }

    @Test
    public void processBlobbAndSave_DuplicateTagsCountOnlyOnce() throws Exception {
        String expected1 = "#test";
        String duplicate1 = "#test";
        String duplicate2 = "#TEST";

        String content = expected1 + " " + duplicate1 + " " + duplicate2;

        Blobb blobb = new Blobb(new User(), content);

        // given
        given(tagService.findByName(expected1.toLowerCase()))
                .willThrow(new TagDoesntExistException(expected1));
        given(blobbRepository.save(blobb)).willReturn(blobb);

        // when
        Blobb processed = blobbService.processBlobbAndSave(blobb);

        // then
        assertEquals(1, processed.getTags().size());

        Set<String> tagNames = processed.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        assertTrue(tagNames.contains(expected1.toLowerCase()));
    }
}
