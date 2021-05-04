package ml.echelon133.microblog.report;

import ml.echelon133.microblog.post.Post;
import ml.echelon133.microblog.post.PostRepository;
import ml.echelon133.microblog.user.User;
import ml.echelon133.microblog.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService implements IReportService {

    private ReportRepository reportRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         PostRepository postRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public List<ReportResult> findAllReports(Long skip, Long limit, boolean checked) throws IllegalArgumentException {

        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Skip or limit cannot be negative");
        }

        return reportRepository.findAllReports(skip, limit, checked);
    }

    @Override
    public boolean createNewReport(UUID reportingUserUuid, UUID reportedPostUuid, String reason, String description)
            throws ResourceDoesNotExistException, IllegalArgumentException {

        Optional<User> reportingUser = userRepository.findById(reportingUserUuid);
        if (reportingUser.isEmpty()) {
            String msg = String.format("User with UUID %s does not exist", reportingUserUuid.toString());
            throw new ResourceDoesNotExistException(msg);
        }

        Optional<Post> reportedPost = postRepository.findById(reportedPostUuid);
        if (reportedPost.isEmpty()) {
            String msg = String.format("Post with UUID %s does not exist", reportedPostUuid.toString());
            throw new ResourceDoesNotExistException(msg);
        }

        User authorOfReportedPost = reportedPost.get().getAuthor();
        if (authorOfReportedPost.getUuid().equals(reportingUser.get().getUuid())) {
            throw new IllegalArgumentException("User cannot report their own posts");
        }

        Report.Reason reason_ = Report.Reason.valueOf(reason.toUpperCase());
        Report savedReport = reportRepository.save(new Report(reason_, description, reportedPost.get(), reportingUser.get()));
        return savedReport.getUuid() != null;
    }

    @Override
    public boolean checkReport(UUID reportUuid, boolean acceptReport) throws ResourceDoesNotExistException {
        if (reportRepository.existsById(reportUuid)) {
            boolean report;
            if (acceptReport) {
                report = reportRepository.acceptReport(reportUuid);
            } else {
                report = reportRepository.rejectReport(reportUuid);
            }
            return report;
        }
        String msg = String.format("Report with UUID %s does not exist", reportUuid.toString());
        throw new ResourceDoesNotExistException(msg);
    }
}
