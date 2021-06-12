package ml.echelon133.microblog.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.microblog.report.controller.ReportController;
import ml.echelon133.microblog.report.exception.ReportExceptionHandler;
import ml.echelon133.microblog.report.exception.ResourceDoesNotExistException;
import ml.echelon133.microblog.report.model.ReportDto;
import ml.echelon133.microblog.report.model.ReportResult;
import ml.echelon133.microblog.report.service.ReportService;
import ml.echelon133.microblog.user.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTests {

    static User user;

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @InjectMocks
    private ReportExceptionHandler reportExceptionHandler;

    private JacksonTester<List<ReportResult>> jsonReports;

    private JacksonTester<ReportDto> jsonReportDto;

    @BeforeAll
    public static void beforeAll() {
        user = new User();
        user.setUuid(UUID.randomUUID());
    }

    @BeforeEach
    public void beforeEach() {
        JacksonTester.initFields(this, new ObjectMapper());

        SecurityContextPersistenceFilter filter;
        filter = new SecurityContextPersistenceFilter();

        mockMvc = MockMvcBuilders
                .standaloneSetup(reportController)
                .setControllerAdvice(reportExceptionHandler)
                .addFilter(filter)
                .build();
    }

    private ReportResult buildTestReport() {
        ReportResult report = new ReportResult();
        report.setUuid(UUID.randomUUID());
        return report;
    }

    @Test
    public void getAllReports_SkipAndLimitDefaultValuesAreSet() throws Exception {
        ReportResult testReport = buildTestReport();
        List<ReportResult> reports = List.of(testReport);

        // expected json
        JsonContent<List<ReportResult>> json = jsonReports.write(reports);

        // given
        given(reportService.findAllReports(0L, 20L, false))
                .willReturn(reports);
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getAllReports_CustomSkipAndLimitValuesAreSet() throws Exception {
        ReportResult testReport = buildTestReport();
        List<ReportResult> reports = List.of(testReport);

        // expected json
        JsonContent<List<ReportResult>> json = jsonReports.write(reports);

        // given
        given(reportService.findAllReports(5L, 10L, false))
                .willReturn(reports);
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("skip", "5")
                        .param("limit", "10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getAllReports_CustomCheckedValueIsSet() throws Exception {
        ReportResult testReport = buildTestReport();
        List<ReportResult> reports = List.of(testReport);

        // expected json
        JsonContent<List<ReportResult>> json = jsonReports.write(reports);

        // given
        given(reportService.findAllReports(0L, 20L, true))
                .willReturn(reports);
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("checked", "true")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getAllReports_HandlesNegativeSkipValue() throws Exception {
        // given
        given(reportService.findAllReports(-5L, 20L, false))
                .willThrow(new IllegalArgumentException("Skip or limit cannot be negative"));
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("skip", "-5")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Skip or limit cannot be negative");
    }

    @Test
    public void getAllReports_HandlesNegativeLimitValue() throws Exception {
        // given
        given(reportService.findAllReports(0L, -20L, false))
                .willThrow(new IllegalArgumentException("Skip or limit cannot be negative"));
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("limit", "-20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Skip or limit cannot be negative");
    }

    @Test
    public void createReport_HandlesInvalidReportedPostUuid() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportedPostUuid("invalid-uuid-test");
        dto.setReason("spam");
        dto.setDescription("");

        JsonContent<ReportDto> json = jsonReportDto.write(dto);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Invalid UUID string: invalid-uuid-test");
    }

    @Test
    public void createReport_HandlesInvalidReasons() throws Exception {
        List<String> invalidReasons = List.of("asdf", "fdsaf", "asdfawe");

        for (String reason : invalidReasons) {
            ReportDto dto = new ReportDto();
            dto.setReportedPostUuid(UUID.randomUUID().toString());
            dto.setReason(reason);
            dto.setDescription("");

            JsonContent<ReportDto> json = jsonReportDto.write(dto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/reports")
                            .accept(APPLICATION_JSON)
                            .with(user(user))
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).contains("Reason is not valid");
        }
    }

    @Test
    public void createReport_HandlesValidReasonsInDifferentCases() throws Exception {
        List<String> validReasons = List.of(
                "spam", "SPAM", "spAM",
                "abuSIVE", "abusive", "ABUsive",
                "againsT_TOS", "against_TOS", "aGAINST_tos"
        );

        for (String reason : validReasons) {
            ReportDto dto = new ReportDto();
            dto.setReportedPostUuid(UUID.randomUUID().toString());
            dto.setReason(reason);
            dto.setDescription("");

            JsonContent<ReportDto> json = jsonReportDto.write(dto);

            // given
            given(reportService.createNewReport(any(), any(), any(), any())).willReturn(true);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/reports")
                            .accept(APPLICATION_JSON)
                            .with(user(user))
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.getContentAsString())
                    .isEqualTo("{\"created\":true}");
        }
    }

    @Test
    public void createReport_AcceptsValidDescription() throws Exception {
        // 300 characters (upper bound)
        String validDescription = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        ReportDto dto = new ReportDto();
        dto.setReportedPostUuid(UUID.randomUUID().toString());
        dto.setReason("spam");
        dto.setDescription(validDescription);

        JsonContent<ReportDto> json = jsonReportDto.write(dto);

        // given
        given(reportService.createNewReport(any(), any(), any(), any())).willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("{\"created\":true}");
    }

    @Test
    public void createReport_HandlesTooLongDescription() throws Exception {
        // 301 characters
        String tooLongDescription = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "a";

        ReportDto dto = new ReportDto();
        dto.setReportedPostUuid(UUID.randomUUID().toString());
        dto.setReason("spam");
        dto.setDescription(tooLongDescription);

        JsonContent<ReportDto> json = jsonReportDto.write(dto);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Description length is invalid");
    }

    @Test
    public void createReport_HandlesNullReportedPostUuid() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReason("spam");
        dto.setDescription("");

        JsonContent<ReportDto> json = jsonReportDto.write(dto);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("reportedPostUuid is required");
    }

    @Test
    public void createReport_HandlesResourceDoesNotExistException() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportedPostUuid(UUID.randomUUID().toString());
        dto.setReason("spam");
        dto.setDescription("");

        JsonContent<ReportDto> json = jsonReportDto.write(dto);

        // given
        given(reportService.createNewReport(any(), any(), any(), any()))
                .willThrow(new ResourceDoesNotExistException("User with UUID test does not exist"));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains("User with UUID test does not exist");
    }

    @Test
    public void createReport_HandlesIllegalArgumentException() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportedPostUuid(UUID.randomUUID().toString());
        dto.setReason("spam");
        dto.setDescription("");

        JsonContent<ReportDto> json = jsonReportDto.write(dto);

        // given
        given(reportService.createNewReport(any(), any(), any(), any()))
                .willThrow(new IllegalArgumentException("User cannot report their own posts"));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("User cannot report their own posts");
    }

    @Test
    public void checkReport_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports/" + invalidUuid)
                        .accept(APPLICATION_JSON)
                        .with(user(user))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Invalid UUID string: asdf");
    }

    @Test
    public void checkReport_ReturnsResult() throws Exception {
        String uuid = UUID.randomUUID().toString();

        // given
        given(reportService.checkReport(UUID.fromString(uuid), true))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/reports/" + uuid)
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("accept", "true")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("{\"checked\":true}");
    }
}
