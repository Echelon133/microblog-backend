package ml.echelon133.microblog.report.model;

import ml.echelon133.microblog.report.validators.ValidReason;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

public class ReportDto {

    @NotNull(message = "reportedPostUuid is required")
    private String reportedPostUuid;

    @ValidReason
    private String reason;

    @Length(max = 300, message = "Description length is invalid")
    private String description;

    public String getReportedPostUuid() {
        return reportedPostUuid;
    }

    public void setReportedPostUuid(String reportedPostUuid) {
        this.reportedPostUuid = reportedPostUuid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
