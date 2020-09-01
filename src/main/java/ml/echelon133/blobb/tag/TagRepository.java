package ml.echelon133.blobb.tag;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends Neo4jRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);

    @Query( "MATCH (t:Tag)-[r:TAGS]->(b:Blobb) " +
            "WHERE b.creationDate >= $first AND b.creationDate <= $second AND b.deleted <> true " +
            "WITH t, count(r) as tagCounter RETURN t ORDER BY tagCounter DESC LIMIT $howManyTagsToGet")
    List<Tag> findMostPopularTags_Between(Date first, Date second, Long howManyTagsToGet);

    @Query( "MATCH (t:Tag)-[:TAGS]->(b:Blobb)<-[:POSTS]-(u:User) " +
            "WHERE t.uuid = $tagUuid AND b.deleted <> true " +
            "OPTIONAL MATCH (b:Blobb)-[:RESPONDS]->(respondsTo:Blobb) " +
            "OPTIONAL MATCH (b:Blobb)-[:REBLOBBS]->(reblobbs:Blobb) " +
            "RETURN b.uuid AS uuid, b.content AS content, b.creationDate AS date, u AS author, " +
            "reblobbs.uuid AS reblobbs, respondsTo.uuid AS respondsTo " +
            "ORDER BY datetime(b.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<RecentBlobb> findRecentBlobbsTagged(UUID tagUuid, Long skip, Long limit);
}
