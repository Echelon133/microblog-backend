package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class BlobbServiceTests {

    @Mock
    private BlobbRepository blobbRepository;

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
}
