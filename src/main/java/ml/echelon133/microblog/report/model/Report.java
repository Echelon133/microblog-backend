package ml.echelon133.microblog.report.model;

import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.user.User;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

import java.util.Date;
import java.util.UUID;

@RelationshipEntity(type = "REPORTS")
public class Report {

    public enum Reason {
        SPAM,
        ABUSIVE,
        AGAINST_TOS
    }

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    @Convert(UuidStringConverter.class)
    private UUID uuid;
    private Date creationDate;
    private Reason reason;
    private boolean checked;
    private String description;

    @EndNode
    private Post reportedPost;

    @StartNode
    private User reportingUser;

    public Report(Reason reason, String description, Post reportedPost, User reportingUser) {
        this.reason = reason;
        this.reportedPost = reportedPost;
        this.reportingUser = reportingUser;
        this.checked = false;
        this.creationDate = new Date();
        this.description = description;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
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

    public Post getReportedPost() {
        return reportedPost;
    }

    public void setReportedPost(Post reportedPost) {
        this.reportedPost = reportedPost;
    }

    public User getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(User reportingUser) {
        this.reportingUser = reportingUser;
    }
}
