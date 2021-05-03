package ml.echelon133.microblog.report;

import ml.echelon133.microblog.report.validators.ValidReason;
import org.hibernate.validator.constraints.Length;

public class ReportDto {
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
