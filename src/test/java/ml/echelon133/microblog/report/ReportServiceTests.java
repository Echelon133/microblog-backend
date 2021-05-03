package ml.echelon133.microblog.report;

import ml.echelon133.microblog.post.Post;
import ml.echelon133.microblog.post.PostRepository;
import ml.echelon133.microblog.user.User;
import ml.echelon133.microblog.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTests {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    public void findAllReports_ThrowsWhenSkipOrLimitNegative() {
        // when
        String uncheckedSkip = assertThrows(IllegalArgumentException.class, () -> {
            reportService.findAllReports(-1L, 1L, false);
        }).getMessage();

        String uncheckedLimit = assertThrows(IllegalArgumentException.class, () -> {
            reportService.findAllReports(1L, -1L, false);
        }).getMessage();

        String checkedSkip = assertThrows(IllegalArgumentException.class, () -> {
            reportService.findAllReports(-1L, 1L, true);
        }).getMessage();

        String checkedLimit = assertThrows(IllegalArgumentException.class, () -> {
            reportService.findAllReports(1L, -1L, true);
        }).getMessage();

        // then
        assertEquals("Skip or limit cannot be negative", uncheckedSkip);
        assertEquals("Skip or limit cannot be negative", uncheckedLimit);
        assertEquals("Skip or limit cannot be negative", checkedSkip);
        assertEquals("Skip or limit cannot be negative", checkedLimit);
    }

    @Test
    public void findAllReports_ReturnsReports() {
        // given
        given(reportRepository.findAllReports(0L, 10L, true))
                .willReturn(List.of(new ReportResult()));
        given(reportRepository.findAllReports(0L, 10L, false))
                .willReturn(List.of(new ReportResult(), new ReportResult()));

        // when
        List<ReportResult> checkedReports = reportService.findAllReports(0L, 10L, true);
        List<ReportResult> uncheckedReports = reportService.findAllReports(0L, 10L, false);

        // then
        assertEquals(1, checkedReports.size());
        assertEquals(2, uncheckedReports.size());
    }

    @Test
    public void createNewReport_ThrowsWhenUserDoesNotExist() {
        UUID reportingUserUuid = UUID.randomUUID();
        UUID reportedPostUuid = UUID.randomUUID();

        // given
        given(userRepository.findById(reportingUserUuid)).willReturn(Optional.empty());

        // when
        String ex = assertThrows(ResourceDoesNotExistException.class, () -> {
            reportService.createNewReport(reportingUserUuid, reportedPostUuid, "", "");
        }).getMessage();

        // then
        String expectedMsg = String.format("User with UUID %s does not exist", reportingUserUuid);
        assertEquals(expectedMsg, ex);
    }

    @Test
    public void createNewReport_ThrowsWhenPostDoesNotExist() {
        UUID reportingUserUuid = UUID.randomUUID();
        UUID reportedPostUuid = UUID.randomUUID();

        // given
        given(userRepository.findById(reportingUserUuid)).willReturn(Optional.of(new User()));
        given(postRepository.findById(reportedPostUuid)).willReturn(Optional.empty());

        // when
        String ex = assertThrows(ResourceDoesNotExistException.class, () -> {
            reportService.createNewReport(reportingUserUuid, reportedPostUuid, "", "");
        }).getMessage();

        // then
        String expectedMsg = String.format("Post with UUID %s does not exist", reportedPostUuid);
        assertEquals(expectedMsg, ex);
    }

    @Test
    public void createNewReport_ThrowsWhenUserReportsTheirOwnPost() {
        UUID reportingUserUuid = UUID.randomUUID();
        UUID reportedPostUuid = UUID.randomUUID();

        User author = new User();
        author.setUuid(reportingUserUuid);
        Post post = new Post(author, "test content");
        post.setUuid(reportedPostUuid);

        // given
        given(userRepository.findById(reportingUserUuid)).willReturn(Optional.of(author));
        given(postRepository.findById(reportedPostUuid)).willReturn(Optional.of(post));

        // when
        String ex = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createNewReport(reportingUserUuid, reportedPostUuid, "", "");
        }).getMessage();

        // then
        assertEquals("User cannot report their own posts", ex);
    }

    @Test
    public void createNewReport_ReturnsCorrectlyOnSuccess() throws Exception {
        UUID reportingUserUuid = UUID.randomUUID();
        UUID reportedPostUuid = UUID.randomUUID();

        User reportingUser = new User();
        reportingUser.setUuid(reportingUserUuid);
        User author = new User();
        author.setUuid(UUID.randomUUID());
        Post reportedPost = new Post(author, "test content");
        reportedPost.setUuid(reportedPostUuid);

        Report report = new Report(Report.Reason.SPAM, "", reportedPost, reportingUser);
        report.setUuid(UUID.randomUUID());

        // given
        given(userRepository.findById(reportingUserUuid)).willReturn(Optional.of(reportingUser));
        given(postRepository.findById(reportedPostUuid)).willReturn(Optional.of(reportedPost));
        given(reportRepository.save(any())).willReturn(report);

        // when
        boolean result = reportService.createNewReport(reportingUserUuid, reportedPostUuid, "spam", "");

        // then
        assertTrue(result);
    }

    @Test
    public void checkReport_AcceptsReport() {
        UUID reportUuid = UUID.randomUUID();

        // given
        given(reportRepository.acceptReport(reportUuid)).willReturn(true);

        // when
        boolean result = reportService.checkReport(reportUuid, true);

        // then
        assertTrue(result);
    }

    @Test
    public void checkReport_RejectsReport() {
        UUID reportUuid = UUID.randomUUID();

        // given
        given(reportRepository.rejectReport(reportUuid)).willReturn(true);

        // when
        boolean result = reportService.checkReport(reportUuid, false);

        // then
        assertTrue(result);
    }
}
