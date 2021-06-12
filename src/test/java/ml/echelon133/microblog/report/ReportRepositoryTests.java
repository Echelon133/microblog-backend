package ml.echelon133.microblog.report;

import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.post.repository.PostRepository;
import ml.echelon133.microblog.report.model.Report;
import ml.echelon133.microblog.report.model.ReportResult;
import ml.echelon133.microblog.report.repository.ReportRepository;
import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class ReportRepositoryTests {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private ReportRepository reportRepository;

    private List<Post> posts;
    private List<User> users;

    @Autowired
    public ReportRepositoryTests(UserRepository userRepository, PostRepository postRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.reportRepository = reportRepository;
    }

    @BeforeEach
    public void beforeEach() {
        // setup users
        User u1 = userRepository.save(new User("user1", "user1@mail.com", "user1", ""));
        User u2 = userRepository.save(new User("user2", "user2@mail.com", "user2", ""));
        users = List.of(u1, u2);

        // setup posts
        Post p1 = postRepository.save(new Post(u1, "test content1"));
        Post p2 = postRepository.save(new Post(u1, "test content2"));
        Post p3 = postRepository.save(new Post(u1, "test content3"));
        posts = List.of(p1, p2, p3);
    }

    @Test
    public void findAllReports_ReturnsEmptyListWhenNoReports() {

        // when
        List<ReportResult> r1 = reportRepository.findAllReports(0L, 20L, false);
        List<ReportResult> r2 = reportRepository.findAllReports(0L, 20L, true);

        // then
        assertEquals(0, r1.size());
        assertEquals(0, r2.size());
    }

    @Test
    public void findAllReports_ReturnsUncheckedReportsInCorrectOrder() {
        User reportingUser = users.get(1); // report as 'user2'

        // report all posts of 'user1' as 'user2'
        for (Post p : posts) {
            Report r = new Report(Report.Reason.SPAM, "", p, reportingUser);
            reportRepository.save(r);
        }

        // when
        List<ReportResult> unchecked = reportRepository.findAllReports(0L, 10L, false);
        List<ReportResult> checked = reportRepository.findAllReports(0L, 10L, true);

        // then
        assertEquals(0, checked.size());
        assertEquals(3, unchecked.size());

        // newest reports are on top, so the last reported post should be first
        List<String> expectedOrder = List.of("test content3", "test content2", "test content1");
        for (int i = 0; i < unchecked.size(); i++) {
            assertEquals(expectedOrder.get(i), unchecked.get(i).getPostContent());
        }
    }

    @Test
    public void findAllReports_ReturnsCheckedReportsInCorrectOrder() {
        User reportingUser = users.get(1); // report as 'user2'

        // report all posts of 'user1' as 'user2'
        for (Post p : posts) {
            Report r = new Report(Report.Reason.SPAM, "", p, reportingUser);
            reportRepository.save(r);
            reportRepository.acceptReport(r.getUuid());
        }

        // when
        List<ReportResult> unchecked = reportRepository.findAllReports(0L, 10L, false);
        List<ReportResult> checked = reportRepository.findAllReports(0L, 10L, true);

        // then
        assertEquals(3, checked.size());
        assertEquals(0, unchecked.size());

        // newest reports are on top, so the last reported post should be first
        List<String> expectedOrder = List.of("test content3", "test content2", "test content1");
        for (int i = 0; i < checked.size(); i++) {
            assertEquals(expectedOrder.get(i), checked.get(i).getPostContent());
        }
    }

    @Test
    public void findAllReports_SkipWorksWithUncheckedReports() {
        User reportingUser = users.get(1); // report as 'user2'

        // report all posts of 'user1' as 'user2'
        for (Post p : posts) {
            Report r = new Report(Report.Reason.SPAM, "", p, reportingUser);
            reportRepository.save(r);
        }

        // when
        List<ReportResult> unchecked = reportRepository.findAllReports(1L, 10L, false);

        // then
        // since 1 of 3 was skipped, expect only 2 reports
        assertEquals(2, unchecked.size());

        List<String> expectedOrder = List.of("test content2", "test content1");
        for (int i = 0; i < unchecked.size(); i++) {
            assertEquals(expectedOrder.get(i), unchecked.get(i).getPostContent());
        }
    }

    @Test
    public void findAllReports_LimitWorksWithUncheckedReports() {
        User reportingUser = users.get(1); // report as 'user2'

        // report all posts of 'user1' as 'user2'
        for (Post p : posts) {
            Report r = new Report(Report.Reason.SPAM, "", p, reportingUser);
            reportRepository.save(r);
        }

        // when
        List<ReportResult> unchecked = reportRepository.findAllReports(0L, 2L, false);

        // then
        assertEquals(2, unchecked.size());

        // only two first are taken
        List<String> expectedOrder = List.of("test content3", "test content2");
        for (int i = 0; i < unchecked.size(); i++) {
            assertEquals(expectedOrder.get(i), unchecked.get(i).getPostContent());
        }
    }

    @Test
    public void findAllReports_SkipWorksWithCheckedReports() {
        User reportingUser = users.get(1); // report as 'user2'

        // report all posts of 'user1' as 'user2'
        for (Post p : posts) {
            Report r = new Report(Report.Reason.SPAM, "", p, reportingUser);
            reportRepository.save(r);
            reportRepository.acceptReport(r.getUuid());
        }

        // when
        List<ReportResult> checked = reportRepository.findAllReports(1L, 10L, true);

        // then
        // since 1 of 3 was skipped, expect only 2 reports
        assertEquals(2, checked.size());

        List<String> expectedOrder = List.of("test content2", "test content1");
        for (int i = 0; i < checked.size(); i++) {
            assertEquals(expectedOrder.get(i), checked.get(i).getPostContent());
        }
    }

    @Test
    public void findAllReports_LimitWorksWithCheckedReports() {
        User reportingUser = users.get(1); // report as 'user2'

        // report all posts of 'user1' as 'user2'
        for (Post p : posts) {
            Report r = new Report(Report.Reason.SPAM, "", p, reportingUser);
            reportRepository.save(r);
            reportRepository.acceptReport(r.getUuid());
        }

        // when
        List<ReportResult> checked = reportRepository.findAllReports(0L, 2L, true);

        // then
        assertEquals(2, checked.size());

        // only two first are taken
        List<String> expectedOrder = List.of("test content3", "test content2");
        for (int i = 0; i < checked.size(); i++) {
            assertEquals(expectedOrder.get(i), checked.get(i).getPostContent());
        }
    }

    @Test
    public void acceptReport_SetsPostAsDeletedAndReportAsChecked() {
        User reportingUser = users.get(1); // report as 'user2'
        Post reportedPost = posts.get(0);
        Report r = new Report(Report.Reason.SPAM, "", reportedPost, reportingUser);
        reportRepository.save(r);

        // when
        boolean checked = reportRepository.acceptReport(r.getUuid());
        List<ReportResult> results = reportRepository.findAllReports(0L, 10L, true);

        // then
        ReportResult rr = results.get(0);
        assertEquals(1, results.size());
        assertTrue(checked);
        assertTrue(rr.isChecked());
        assertTrue(rr.isPostDeleted());
    }

    @Test
    public void rejectReport_LeavesPostAsNonDeletedAndReportAsChecked() {
        User reportingUser = users.get(1); // report as 'user2'
        Post reportedPost = posts.get(0);
        Report r = new Report(Report.Reason.SPAM, "", reportedPost, reportingUser);
        reportRepository.save(r);

        // when
        boolean checked = reportRepository.rejectReport(r.getUuid());
        List<ReportResult> results = reportRepository.findAllReports(0L, 10L, true);

        // then
        ReportResult rr = results.get(0);
        assertEquals(1, results.size());
        assertTrue(checked);
        assertTrue(rr.isChecked());
        assertFalse(rr.isPostDeleted());
    }
}
