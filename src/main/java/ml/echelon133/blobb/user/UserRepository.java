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
            "WHERE u.uuid = $uuid AND followed.uuid <> $uuid " +
            "RETURN followed " +
            "SKIP $skip " +
            "LIMIT $limit")
    List<User> findAllFollowsOfUserWithUuid(UUID uuid, Long skip, Long limit);

    @Query( "MATCH (following)-[:FOLLOWS]->(u:User) " +
            "WHERE u.uuid = $uuid AND following.uuid <> $uuid " +
            "RETURN following " +
            "SKIP $skip " +
            "LIMIT $limit")
    List<User> findAllFollowersOfUserWithUuid(UUID uuid, Long skip, Long limit);

    @Query( "MATCH (user:User) " +
            "WHERE user.uuid = $uuid " +
            "OPTIONAL MATCH (user)-[follows:FOLLOWS]->(o1:User) WHERE o1.uuid <> user.uuid " +
            "OPTIONAL MATCH (o2:User)-[followedBy:FOLLOWS]->(user) WHERE o2.uuid <> user.uuid " +
            "RETURN user.uuid AS uuid, count(distinct(follows)) AS follows, count(distinct(followedBy)) AS followers")
    Optional<UserProfileInfo> getUserProfileInfo(UUID uuid);

    @Query( "MATCH (u:User)-[:POSTS]->(b:Blobb) " +
            "WHERE u.uuid = $userUuid AND b.deleted <> true " +
            "OPTIONAL MATCH (b:Blobb)-[:RESPONDS]->(respondsTo:Blobb) " +
            "OPTIONAL MATCH (b:Blobb)-[:REBLOBBS]->(reblobbs:Blobb) " +
            "RETURN b.uuid AS uuid, b.content AS content, b.creationDate AS date, u AS author, " +
            "reblobbs.uuid AS reblobbs, respondsTo.uuid AS respondsTo " +
            "ORDER BY datetime(b.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<UserBlobb> findRecentBlobbsOfUser(UUID userUuid, Long skip, Long limit);
}
