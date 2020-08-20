package ml.echelon133.blobb.tag;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends Neo4jRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);
}
