package ml.echelon133.blobb.blobb;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlobbRepository extends Neo4jRepository<Blobb, UUID> {

    /*
        User's feed consists of most recent blobbs/reblobbs/responses that have been posted
        either by themselves or users that they follow.

        This query can be simplified if every user follows themselves by default. Then it can be performed as a single case.
        Other queries that count/list follows/followers should hide the fact that the user follows themselves by filtering
        results.
     */
    @Query( "MATCH (u:User)-[:FOLLOWS]->(poster:User)-[:POSTS]->(blobbs:Blobb) " +
            "WHERE u.uuid = $uuid AND blobbs.creationDate >= $first AND blobbs.creationDate <= $second " +
            "OPTIONAL MATCH (blobbs:Blobb)-[:RESPONDS]->(respondsTo:Blobb) " +
            "OPTIONAL MATCH (blobbs:Blobb)-[:REBLOBBS]->(reblobbs:Blobb) " +
            "RETURN blobbs.uuid AS uuid, blobbs.content AS content, blobbs.creationDate AS date, poster AS author, " +
            "reblobbs.uuid AS reblobbs, respondsTo.uuid AS respondsTo " +
            "ORDER BY datetime(blobbs.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<FeedBlobb> getFeedForUserWithUuid_PostedBetween(UUID uuid, Date first, Date second, Long skip, Long limit);

    @Query( "MATCH (u:User)-[:POSTS]->(blobb:Blobb) WHERE blobb.uuid = $uuid " +
            "OPTIONAL MATCH (blobb:Blobb)-[:RESPONDS]->(respondsTo:Blobb) " +
            "OPTIONAL MATCH (blobb:Blobb)-[:REBLOBBS]->(reblobbs:Blobb) " +
            "RETURN blobb.uuid AS uuid, blobb.content AS content, blobb.creationDate AS date, u AS author, " +
            "reblobbs.uuid AS reblobbs, respondsTo.uuid AS respondsTo ")
    Optional<FeedBlobb> getBlobbWithUuid(UUID uuid);
}
