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

    @Query( "MATCH(u1:User)-[f:FOLLOWS]->(u2:User) " +
            "WHERE u1.uuid = $uuidOfFollower AND u2.uuid = $uuidOfFollowed " +
            "RETURN id(f)")
    Optional<Long> checkIfUserWithUuidFollows(UUID uuidOfFollower, UUID uuidOfFollowed);

    @Query( "MATCH (u1:User)-[f:FOLLOWS]->(u2:User) " +
            "WHERE u1.uuid = $uuidOfFollower AND u2.uuid = $uuidOfFollowed " +
            "DELETE f")
    void unfollowUserWithUuid(UUID uuidOfFollower, UUID uuidOfFollowed);

    @Query( "MATCH (u:User)-[:FOLLOWS]->(followed) " +
            "WHERE u.uuid = $uuid " +
            "RETURN followed " +
            "SKIP $skip " +
            "LIMIT $limit")
    List<User> findAllFollowsOfUserWithUuid(UUID uuid, Long skip, Long limit);

    @Query( "MATCH (following)-[:FOLLOWS]->(u:User) " +
            "WHERE u.uuid = $uuid " +
            "RETURN following " +
            "SKIP $skip " +
            "LIMIT $limit")
    List<User> findAllFollowersOfUserWithUuid(UUID uuid, Long skip, Long limit);

    @Query( "MATCH (user:User) " +
            "WHERE user.uuid = $uuid " +
            "OPTIONAL MATCH (user)-[follows:FOLLOWS]->(:User) " +
            "OPTIONAL MATCH (:User)-[followedBy:FOLLOWS]->(user) " +
            "RETURN user.uuid AS uuid, count(distinct(follows)) AS follows, count(distinct(followedBy)) AS followers")
    Optional<UserProfileInfo> getUserProfileInfo(UUID uuid);
}
