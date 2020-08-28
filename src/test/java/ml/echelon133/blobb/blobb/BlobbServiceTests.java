package ml.echelon133.blobb.blobb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
