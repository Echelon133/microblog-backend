package ml.echelon133.microblog.user;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends Neo4jRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}
