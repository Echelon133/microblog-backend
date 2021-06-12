package ml.echelon133.microblog.report.repository;

import ml.echelon133.microblog.report.model.Report;
import ml.echelon133.microblog.report.model.ReportResult;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.annotation.Query;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends Neo4jRepository<Report, UUID> {

    @Query( "MATCH (reporter:User)-[r:REPORTS]->(reported:Post)<-[:POSTS]-(u:User) " +
            "WHERE r.checked = $checked " +
            "RETURN r.uuid AS uuid, reported.uuid AS reportedPostUuid, " +
            "reporter.username AS reportAuthorUsername, " +
            "u.username AS postAuthorUsername, " +
            "reported.content AS postContent, " +
            "reported.deleted AS postDeleted, " +
            "r.reason AS reason, " +
            "r.checked AS checked, " +
            "r.description AS description " +
            "ORDER BY datetime(r.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<ReportResult> findAllReports(Long skip, Long limit, boolean checked);

    // set the report as 'checked' and post as 'deleted'
    @Query( "MATCH (reporter:User)-[r:REPORTS]->(reported:Post) " +
            "WHERE r.uuid = $reportUuid AND r.checked = false " +
            "SET r.checked = true, reported.deleted = true " +
            "RETURN r.checked")
    boolean acceptReport(UUID reportUuid);

    // set the report as 'checked' but leave the post as it is
    @Query( "MATCH (reporter:User)-[r:REPORTS]->(reported:Post) " +
            "WHERE r.uuid = $reportUuid AND r.checked = false " +
            "SET r.checked = true " +
            "RETURN r.checked")
    boolean rejectReport(UUID reportUuid);
}
