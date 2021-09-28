package ml.echelon133.microblog.user.repository;

import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.model.UserPost;
import ml.echelon133.microblog.user.model.UserProfileInfo;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Neo4jRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    List<User> findAllByUsernameContains(String search);
    boolean existsUserByUsername(String username);

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

    @Query( "MATCH (u1:User)-[:FOLLOWS]->(known:User)-[:FOLLOWS]->(u2:User) " +
            "WHERE u1.uuid = $u1Uuid AND u2.uuid = $u2Uuid " +
            "AND known.uuid <> $u1Uuid AND known.uuid <> $u2Uuid " +
            "RETURN known ORDER BY datetime(known.creationDate) DESC SKIP $skip LIMIT $limit")
    List<User> findFollowersUserKnows(UUID u1Uuid, UUID u2Uuid, Long skip, Long limit);

    @Query( "MATCH (user:User) " +
            "WHERE user.uuid = $uuid " +
            "OPTIONAL MATCH (user)-[follows:FOLLOWS]->(o1:User) WHERE o1.uuid <> user.uuid " +
            "OPTIONAL MATCH (o2:User)-[followedBy:FOLLOWS]->(user) WHERE o2.uuid <> user.uuid " +
            "RETURN count(distinct(follows)) AS follows, count(distinct(followedBy)) AS followers")
    Optional<UserProfileInfo> getUserProfileInfo(UUID uuid);

    @Query( "MATCH (u:User)-[:POSTS]->(p:Post) " +
            "WHERE u.uuid = $userUuid AND p.deleted <> true " +
            "OPTIONAL MATCH (p:Post)-[:RESPONDS]->(respondsTo:Post)<-[:POSTS]-(respondsToUser:User)  " +
            "OPTIONAL MATCH (p:Post)-[:QUOTES]->(quotes:Post) " +
            "RETURN p.uuid AS uuid, p.content AS content, p.creationDate AS date, u AS author, " +
            "quotes.uuid AS quotes, respondsTo.uuid AS respondsTo, respondsToUser.username AS respondsToUsername " +
            "ORDER BY datetime(p.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<UserPost> findRecentPostsOfUser(UUID userUuid, Long skip, Long limit);
}
