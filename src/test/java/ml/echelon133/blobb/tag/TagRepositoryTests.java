package ml.echelon133.blobb.tag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class TagRepositoryTests {

    private TagRepository tagRepository;

    @Autowired
    public TagRepositoryTests(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Test
    public void savedTagGetsUuid() {
        Tag tag = new Tag("test");
        assertNull(tag.getUuid());

        Tag savedTag = tagRepository.save(tag);
        assertNotNull(savedTag.getUuid());
    }

    @Test
    public void findByName_FindsExistingTags() {
        Tag tag = new Tag("test");
        Tag savedTag = tagRepository.save(tag);

        Optional<Tag> foundTag = tagRepository.findByName("test");

        assertEquals(savedTag, foundTag.orElse(null));
    }
}
