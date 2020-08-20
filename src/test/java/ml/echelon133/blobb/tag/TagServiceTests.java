package ml.echelon133.blobb.tag;

import ml.echelon133.blobb.user.UserDoesntExistException;
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
public class TagServiceTests {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    public void findByUuid_ThrowsWhenTagDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagRepository.findById(uuid)).willReturn(Optional.empty());

        // when
        String message = assertThrows(TagDoesntExistException.class, () -> {
            tagService.findByUuid(uuid);
        }).getMessage();

        // then
        assertEquals(String.format("Tag with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void findByUuid_ReturnsObject() throws Exception {
        UUID uuid = UUID.randomUUID();
        Tag tag = new Tag("test");

        // given
        given(tagRepository.findById(uuid)).willReturn(Optional.of(tag));

        // when
        Tag foundTag = tagService.findByUuid(uuid);

        // then
        assertEquals(tag, foundTag);
    }

    @Test
    public void findByName_ThrowsWhenTagDoesntExist() {
        String name = "test";

        // given
        given(tagRepository.findByName(name)).willReturn(Optional.empty());

        // when
        String message = assertThrows(TagDoesntExistException.class, () -> {
            tagService.findByName(name);
        }).getMessage();

        // then
        assertEquals(String.format("Tag #%s doesn't exist", name), message);
    }

    @Test
    public void findByName_ReturnsObject() throws Exception {
        String name = "test";
        Tag tag = new Tag(name);

        // given
        given(tagRepository.findByName(name)).willReturn(Optional.of(tag));

        // when
        Tag foundTag = tagService.findByName(name);

        // then
        assertEquals(tag, foundTag);
    }
}
