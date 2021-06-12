package ml.echelon133.microblog.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.microblog.notification.controller.NotificationController;
import ml.echelon133.microblog.notification.exception.NotificationExceptionHandler;
import ml.echelon133.microblog.notification.model.NotificationResult;
import ml.echelon133.microblog.notification.service.NotificationService;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@ExtendWith(MockitoExtension.class)
public class NotificationControllerTests {

    static User user;

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationExceptionHandler notificationExceptionHandler;

    @InjectMocks
    private NotificationController notificationController;

    private JacksonTester<List<NotificationResult>> jsonNotifications;

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
                .standaloneSetup(notificationController)
                .setControllerAdvice(notificationExceptionHandler)
                .addFilter(filter)
                .build();
    }

    private NotificationResult buildTestNotification() {
        NotificationResult notif = new NotificationResult();
        notif.setUuid(UUID.randomUUID());
        notif.setRead(false);
        notif.setNotificationPost(UUID.randomUUID());
        return notif;
    }

    @Test
    public void getAllNotifications_SkipAndLimitDefaultValuesAreSet() throws Exception {
        NotificationResult testNotif = buildTestNotification();
        List<NotificationResult> notifications = List.of(testNotif);

        // expected json
        JsonContent<List<NotificationResult>> json = jsonNotifications.write(notifications);

        // given
        given(notificationService.findAllNotificationsOfUser(user, 0L, 5L))
                .willReturn(notifications);
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/notifications")
                        .accept(APPLICATION_JSON)
                .with(user(user))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getAllNotifications_CustomSkipAndLimitValuesAreSet() throws Exception {
        NotificationResult testNotif = buildTestNotification();
        List<NotificationResult> notifications = List.of(testNotif);

        // expected json
        JsonContent<List<NotificationResult>> json = jsonNotifications.write(notifications);

        // given
        given(notificationService.findAllNotificationsOfUser(user, 10L, 1L))
                .willReturn(notifications);
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/notifications")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("skip", "10")
                        .param("limit", "1")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getAllNotifications_HandlesNegativeSkipValue() throws Exception {
        // given
        given(notificationService.findAllNotificationsOfUser(user, -10L, 5L))
                .willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/notifications")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("skip", "-10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Skip or limit cannot be negative");
    }

    @Test
    public void getAllNotifications_HandlesNegativeLimitValue() throws Exception {
        // given
        given(notificationService.findAllNotificationsOfUser(user, 0L, -10L))
                .willCallRealMethod();

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/notifications")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
                        .param("limit", "-10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Skip or limit cannot be negative");
    }

    @Test
    public void getUnreadCounter_ReturnsNumberOfUnreadNotifications() throws Exception {
        // given
        given(notificationService.countUnreadNotificationsOfUser(user))
                .willReturn(10L);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/notifications/unreadCounter")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("{\"unreadCounter\":10}");
    }

    @Test
    public void readAllNotifications_ReturnsCorrectResponse() throws Exception {
        // given
        given(notificationService.readAllNotificationsOfUser(user))
                .willReturn(100L);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/notifications/readAll")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("{\"markedAsRead\":100}");
    }

    @Test
    public void readSingleNotification_ReturnsCorrectResponse() throws Exception {
        UUID notificationUuid = UUID.randomUUID();

        // given
        given(notificationService.readSingleNotificationOfUser(user, notificationUuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/notifications/" + notificationUuid + "/read")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("{\"read\":true}");
    }
    @Test

    public void readSingleNotification_HandlesInvalidUuid() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/notifications/aaaa/read")
                        .accept(APPLICATION_JSON)
                        .with(user(user))
        ).andReturn().getResponse();

        // then
        System.out.println(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Invalid UUID string");
    }
}
