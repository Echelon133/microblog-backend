package ml.echelon133.blobb.blobb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
