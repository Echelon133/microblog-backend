package ml.echelon133.microblog.report.service;

import ml.echelon133.microblog.report.model.ReportResult;
import ml.echelon133.microblog.report.exception.ResourceDoesNotExistException;

import java.util.List;
import java.util.UUID;

public interface IReportService {
    List<ReportResult> findAllReports(Long skip, Long limit, boolean checked) throws IllegalArgumentException;
    boolean createNewReport(UUID reportingUserUuid, UUID reportedPostUuid, String reason, String description)
            throws ResourceDoesNotExistException, IllegalArgumentException;
    boolean checkReport(UUID reportUuid, boolean acceptReport) throws ResourceDoesNotExistException;
}
