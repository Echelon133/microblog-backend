package ml.echelon133.microblog.report.model;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.UUID;

@QueryResult
public class ReportResult {

    @Convert(value = UuidStringConverter.class)
    private UUID uuid;

    @Convert(value = UuidStringConverter.class)
    private UUID reportedPostUuid;

    private String reportAuthorUsername;
    private String postAuthorUsername;
    private String postContent;
    private boolean postDeleted;
    private String reason;
    private boolean checked;
    private String description;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getReportedPostUuid() {
        return reportedPostUuid;
    }

    public void setReportedPostUuid(UUID reportedPostUuid) {
        this.reportedPostUuid = reportedPostUuid;
    }

    public String getReportAuthorUsername() {
        return reportAuthorUsername;
    }

    public void setReportAuthorUsername(String reportAuthorUsername) {
        this.reportAuthorUsername = reportAuthorUsername;
    }

    public String getPostAuthorUsername() {
        return postAuthorUsername;
    }

    public void setPostAuthorUsername(String postAuthorUsername) {
        this.postAuthorUsername = postAuthorUsername;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public boolean isPostDeleted() {
        return postDeleted;
    }

    public void setPostDeleted(boolean postDeleted) {
        this.postDeleted = postDeleted;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
