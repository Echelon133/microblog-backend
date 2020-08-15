package ml.echelon133.blobb.user;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Neo4jRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    @Query( "MATCH (u1:User), (u2:User) " +
            "WHERE u1.uuid = $uuidOfFollower AND u2.uuid = $uuidOfFollowed " +
            "CREATE (u1)-[f:FOLLOWS]->(u2) " +
            "RETURN id(f)")
    Optional<Long> followUserWithUuid(UUID uuidOfFollower, UUID uuidOfFollowed);

    @Query( "MATCH (u1:User)-[f:FOLLOWS]->(u2:User) " +
            "WHERE u1.uuid = $uuidOfFollower AND u2.uuid = $uuidOfFollowed " +
            "DELETE f")
    void unfollowUserWithUuid(UUID uuidOfFollower, UUID uuidOfFollowed);

    @Query( "MATCH (u:User)-[:FOLLOWS]->(followed) " +
            "WHERE u.uuid = $uuid " +
            "RETURN followed")
    List<User> findAllFollowedByUserWithUuid(UUID uuid);

    @Query( "MATCH (following)-[:FOLLOWS]->(u:User) " +
            "WHERE u.uuid = $uuid " +
            "RETURN following")
    List<User> findAllFollowingUserWithUuid(UUID uuid);

}
