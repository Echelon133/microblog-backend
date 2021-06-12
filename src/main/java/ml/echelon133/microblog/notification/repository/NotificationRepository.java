package ml.echelon133.microblog.notification.repository;

import ml.echelon133.microblog.notification.model.NotificationResult;
import ml.echelon133.microblog.notification.model.Notification;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends Neo4jRepository<Notification, UUID> {

    @Query( "MATCH (a:User)-[:POSTS]->(p:Post)-[notif:NOTIFIES]->(u:User) " +
            "WHERE u.uuid = $userUuid " +
            "RETURN notif.uuid AS uuid, a.username AS notifiedBy, notif.read AS read, " +
            "notif.type AS type, p.uuid AS notificationPost " +
            "ORDER BY datetime(notif.creationDate) DESC SKIP $skip LIMIT $limit ")
    List<NotificationResult> findAllNotificationsOfUser(UUID userUuid, Long skip, Long limit);

    @Query( "MATCH (:Post)-[notif:NOTIFIES]->(u:User) " +
            "WHERE u.uuid = $userUuid " +
            "AND notif.read = false " +
            "RETURN COUNT(notif)")
    Long countUnreadNotificationsOfUser(UUID userUuid);

    @Query( "MATCH (:Post)-[notif:NOTIFIES]->(u:User) " +
            "WHERE u.uuid = $userUuid AND notif.read = false " +
            "SET notif.read = true " +
            "RETURN COUNT(notif)")
    Long readAllNotificationsOfUser(UUID userUuid);

    @Query( "MATCH (:Post)-[notif:NOTIFIES]->(u:User) " +
            "WHERE u.uuid = $userUuid AND notif.uuid = $notificationUuid AND notif.read = false " +
            "SET notif.read = true " +
            "RETURN COUNT(notif) = 1")
    boolean readSingleNotificationOfUser(UUID userUuid, UUID notificationUuid);
}
