package ml.echelon133.microblog.notification.model;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.UUID;

@QueryResult
public class NotificationResult {

    @Convert(value = UuidStringConverter.class)
    private UUID uuid;
    private String notifiedBy;
    private boolean read;
    private String type;
    @Convert(value = UuidStringConverter.class)
    private UUID notificationPost;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getNotifiedBy() {
        return notifiedBy;
    }

    public void setNotifiedBy(String notifiedBy) {
        this.notifiedBy = notifiedBy;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getNotificationPost() {
        return notificationPost;
    }

    public void setNotificationPost(UUID notificationPost) {
        this.notificationPost = notificationPost;
    }
}
