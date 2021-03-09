package ml.echelon133.microblog.tag;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends Neo4jRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);

    @Query( "MATCH (t:Tag)-[r:TAGS]->(p:Post) " +
            "WHERE p.creationDate >= $first AND p.creationDate <= $second AND p.deleted <> true " +
            "WITH t, count(r) as tagCounter RETURN t ORDER BY tagCounter DESC LIMIT $howManyTagsToGet")
    List<Tag> findMostPopularTags_Between(Date first, Date second, Long howManyTagsToGet);

    @Query( "MATCH (t:Tag)-[:TAGS]->(p:Post)<-[:POSTS]-(u:User) " +
            "WHERE t.uuid = $tagUuid AND p.deleted <> true " +
            "OPTIONAL MATCH (p:Post)-[:RESPONDS]->(respondsTo:Post) " +
            "OPTIONAL MATCH (p:Post)-[:QUOTES]->(quotes:Post) " +
            "RETURN p.uuid AS uuid, p.content AS content, p.creationDate AS date, u AS author, " +
            "quotes.uuid AS quotes, respondsTo.uuid AS respondsTo " +
            "ORDER BY datetime(p.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<RecentPost> findRecentPostsTagged(UUID tagUuid, Long skip, Long limit);
}
