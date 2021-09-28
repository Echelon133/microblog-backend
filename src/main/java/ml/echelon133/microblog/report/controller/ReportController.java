package ml.echelon133.microblog.report.controller;

import ml.echelon133.microblog.report.model.ReportDto;
import ml.echelon133.microblog.report.model.ReportResult;
import ml.echelon133.microblog.report.exception.InvalidReportDataException;
import ml.echelon133.microblog.report.exception.ResourceDoesNotExistException;
import ml.echelon133.microblog.report.service.IReportService;
import ml.echelon133.microblog.user.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private IReportService reportService;

    @Autowired
    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<ReportResult>> getAllReports(@RequestParam(defaultValue = "0") Long skip,
                                                            @RequestParam(defaultValue = "20") Long limit,
                                                            @RequestParam(required = false) boolean checked) {
        return new ResponseEntity<>(reportService.findAllReports(skip, limit, checked), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> createReport(@Valid @RequestBody ReportDto report, BindingResult result)
            throws InvalidReportDataException, IllegalArgumentException, ResourceDoesNotExistException {

        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (result.hasFieldErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
            throw new InvalidReportDataException(errors);
        }

        UUID reportedPostUuid = UUID.fromString(report.getReportedPostUuid());
        boolean created = reportService
                .createNewReport(loggedUser.getUuid(), reportedPostUuid, report.getReason(), report.getDescription());
        return new ResponseEntity<>(Map.of("created", created), HttpStatus.OK);
    }

    @PostMapping("/{uuid}")
    public ResponseEntity<Map<String, Boolean>> checkReport(@PathVariable String uuid,
                                                            @RequestParam(required = false) boolean accept)
            throws IllegalArgumentException, ResourceDoesNotExistException {

        UUID reportUuid = UUID.fromString(uuid);
        boolean checked = reportService.checkReport(reportUuid, accept);
        return new ResponseEntity<>(Map.of("checked", checked), HttpStatus.OK);
    }
}
