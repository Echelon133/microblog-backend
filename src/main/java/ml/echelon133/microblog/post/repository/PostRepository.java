package ml.echelon133.microblog.post.repository;

import ml.echelon133.microblog.post.model.FeedPost;
import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.post.model.PostInfo;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends Neo4jRepository<Post, UUID> {

    /*
        User's feed consists of most recent posts/quotes/responses that have been posted
        either by themselves or users that they follow.

        This query can be simplified if every user follows themselves by default. Then it can be performed as a single case.
        Other queries that count/list follows/followers should hide the fact that the user follows themselves by filtering
        results.
     */
    @Query( "MATCH (u:User)-[:FOLLOWS]->(poster:User)-[:POSTS]->(posts:Post) " +
            "WHERE u.uuid = $uuid AND posts.deleted <> true " +
            "OPTIONAL MATCH (posts:Post)-[:RESPONDS]->(respondsTo:Post) " +
            "OPTIONAL MATCH (posts:Post)-[:QUOTES]->(quotes:Post) " +
            "RETURN posts.uuid AS uuid, posts.content AS content, posts.creationDate AS date, poster AS author, " +
            "quotes.uuid AS quotes, respondsTo.uuid AS respondsTo " +
            "ORDER BY datetime(posts.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<FeedPost> getFeedForUserWithUuid(UUID uuid, Long skip, Long limit);

    @Query( "MATCH (u:User)-[:FOLLOWS]->(poster:User)-[:POSTS]->(posts:Post) " +
            "WHERE u.uuid = $uuid AND posts.deleted <> true AND posts.creationDate >= $oldestDateAllowed " +
            "OPTIONAL MATCH (posts:Post)-[:RESPONDS]->(respondsTo:Post) " +
            "OPTIONAL MATCH (posts:Post)-[:QUOTES]->(quotes:Post) " +
            "OPTIONAL MATCH (:User)-[l:LIKES]->(posts) " +
            "WITH posts, quotes, respondsTo, poster, count(l) as numberOfLikes " +
            "RETURN posts.uuid AS uuid, posts.content AS content, posts.creationDate AS date, poster AS author, " +
            "quotes.uuid AS quotes, respondsTo.uuid AS respondsTo " +
            "ORDER BY numberOfLikes DESC, datetime(posts.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<FeedPost> getFeedForUserWithUuid_Popular(UUID uuid, Date oldestDateAllowed, Long skip, Long limit);

    @Query( "MATCH (poster:User)-[:POSTS]->(posts:Post) " +
            "WHERE posts.deleted <> true AND posts.creationDate >= $oldestDateAllowed " +
            "OPTIONAL MATCH (posts:Post)-[:RESPONDS]->(respondsTo:Post) " +
            "OPTIONAL MATCH (posts:Post)-[:QUOTES]->(quotes:Post) " +
            "OPTIONAL MATCH (:User)-[l:LIKES]->(posts) " +
            "WITH posts, quotes, respondsTo, poster, count(l) as numberOfLikes " +
            "RETURN posts.uuid AS uuid, posts.content AS content, posts.creationDate AS date, poster AS author, " +
            "quotes.uuid AS quotes, respondsTo.uuid AS respondsTo " +
            "ORDER BY numberOfLikes DESC, datetime(posts.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<FeedPost> getFeedForAnonymousUser_Popular(Date oldestDateAllowed, Long skip, Long limit);

    @Query( "MATCH (u:User)-[:POSTS]->(post:Post) WHERE post.uuid = $uuid AND post.deleted <> true " +
            "OPTIONAL MATCH (post:Post)-[:RESPONDS]->(respondsTo:Post) " +
            "OPTIONAL MATCH (post:Post)-[:QUOTES]->(quotes:Post) " +
            "RETURN post.uuid AS uuid, post.content AS content, post.creationDate AS date, u AS author, " +
            "quotes.uuid AS quotes, respondsTo.uuid AS respondsTo ")
    Optional<FeedPost> getPostWithUuid(UUID uuid);

    @Query( "MATCH (post:Post) WHERE post.uuid = $uuid AND post.deleted <> true " +
            "OPTIONAL MATCH (:User)-[likes:LIKES]->(post:Post) " +
            "OPTIONAL MATCH (res:ResponsePost)-[responses:RESPONDS]->(post) WHERE res.deleted <> true " +
            "OPTIONAL MATCH (q:QuotePost)-[quotes:QUOTES]->(post) WHERE q.deleted <> true " +
            "RETURN post.uuid AS uuid, count(distinct(responses)) AS responses, count(distinct(likes)) AS likes, " +
            "count(distinct(quotes)) AS quotes")
    Optional<PostInfo> getInfoAboutPostWithUuid(UUID uuid);

    @Query( "MATCH (u:User) WHERE u.uuid = $uuidOfUser " +
            "MATCH (p:Post) WHERE p.uuid = $uuidOfPost AND p.deleted <> true " +
            "CREATE (u)-[l:LIKES]->(p) " +
            "RETURN id(l)")
    Optional<Long> likePostWithUuid(UUID uuidOfUser, UUID uuidOfPost);

    @Query( "MATCH (u:User)-[l:LIKES]->(p:Post)" +
            "WHERE u.uuid = $uuidOfUser AND p.uuid = $uuidOfPost AND p.deleted <> true " +
            "RETURN id(l)")
    Optional<Long> checkIfUserWithUuidLikes(UUID uuidOfUser, UUID uuidOfPost);

    @Query( "MATCH (u:User)-[l:LIKES]->(p:Post) " +
            "WHERE u.uuid = $uuidOfUser AND p.uuid = $uuidOfPost " +
            "DELETE l")
    void unlikePostWithUuid(UUID uuidOfUser, UUID uuidOfPost);

    // allow listing responses to posts marked as deleted
    // but dont list responses that are marked as deleted
    @Query( "MATCH (post:Post) WHERE post.uuid = $uuid " +
            "MATCH (u:User)-[:POSTS]->(response:ResponsePost)-[:RESPONDS]->(post) WHERE response.deleted <> true " +
            "RETURN response.uuid AS uuid, response.content AS content, response.creationDate AS date, u AS author, " +
            "NULL AS quotes, post.uuid AS respondsTo " +
            "ORDER BY date ASC SKIP $skip LIMIT $limit")
    List<FeedPost> getAllResponsesToPostWithUuid(UUID uuid, Long skip, Long limit);

    // allow listing quotes even when referenced post is marked as deleted
    // but don't list quotes that are marked as deleted
    @Query( "MATCH (post:Post) WHERE post.uuid = $uuid " +
            "MATCH (u:User)-[:POSTS]->(quotes:QuotePost)-[:QUOTES]->(post) WHERE quotes.deleted <> true " +
            "RETURN quotes.uuid AS uuid, quotes.content AS content, quotes.creationDate AS date, u AS author, " +
            "post.uuid AS quotes, NULL AS respondsTo " +
            "ORDER BY date ASC SKIP $skip LIMIT $limit")
    List<FeedPost> getAllQuotesOfPostWithUuid(UUID uuid, Long skip, Long limit);
}
