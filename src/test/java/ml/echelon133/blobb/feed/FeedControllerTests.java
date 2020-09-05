package ml.echelon133.blobb.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.blobb.blobb.BlobbService;
import ml.echelon133.blobb.blobb.FeedBlobb;
import ml.echelon133.blobb.blobb.IBlobbService;
import ml.echelon133.blobb.user.User;
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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@ExtendWith(MockitoExtension.class)
public class FeedControllerTests {

    static User testUser;

    private MockMvc mockMvc;

    @Mock
    private BlobbService blobbService;

    @InjectMocks
    private FeedController feedController;

    @InjectMocks
    private FeedExceptionHandler feedExceptionHandler;

    private JacksonTester<List<FeedBlobb>> jsonFeedBlobbs;

    @BeforeAll
    public static void beforeAll() {
        testUser = new User("user1", "", "","");
        testUser.setUuid(UUID.randomUUID());
    }

    @BeforeEach
    public void beforeEach() {
        JacksonTester.initFields(this, new ObjectMapper());

        // custom filter that lets us use
        // SecurityMockMvcRequestPostProcessors.user(UserDetails user)
        // while doing test request
        SecurityContextPersistenceFilter filter;
        filter = new SecurityContextPersistenceFilter();

        mockMvc = MockMvcBuilders
                .standaloneSetup(feedController)
                .setControllerAdvice(feedExceptionHandler)
                .addFilter(filter)
                .build();
    }

    @Test
    public void getUserFeed_SetsCorrectDefaultParameters() throws Exception {
        List<FeedBlobb> testBlobbs = List.of(new FeedBlobb());

        // json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbs.write(testBlobbs);

        // given
        given(blobbService.getFeedForUser(testUser, IBlobbService.BlobbsSince.ONE_HOUR, 0L, 20L))
                .willReturn(testBlobbs);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getUserFeed_ProvidedSkipAndLimitAreUsed() throws Exception {
        List<FeedBlobb> testBlobbs = List.of(new FeedBlobb());

        // json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbs.write(testBlobbs);

        // given
        given(blobbService.getFeedForUser(testUser, IBlobbService.BlobbsSince.ONE_HOUR, 5L, 50L))
                .willReturn(testBlobbs);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("skip", "5")
                        .param("limit", "50")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getUserFeed_ProvidedSinceParameterIsUsed() throws Exception {
        List<FeedBlobb> hour = List.of(new FeedBlobb());
        List<FeedBlobb> sixHours = List.of(new FeedBlobb(), new FeedBlobb());
        List<FeedBlobb> twelveHours = List.of(new FeedBlobb(), new FeedBlobb(), new FeedBlobb());

        // json
        JsonContent<List<FeedBlobb>> json1 = jsonFeedBlobbs.write(hour);
        JsonContent<List<FeedBlobb>> json2 = jsonFeedBlobbs.write(sixHours);
        JsonContent<List<FeedBlobb>> json3 = jsonFeedBlobbs.write(twelveHours);

        // given
        given(blobbService.getFeedForUser(testUser, IBlobbService.BlobbsSince.ONE_HOUR, 0L, 20L))
                .willReturn(hour);
        given(blobbService.getFeedForUser(testUser, IBlobbService.BlobbsSince.SIX_HOURS, 0L, 20L))
                .willReturn(sixHours);
        given(blobbService.getFeedForUser(testUser, IBlobbService.BlobbsSince.TWELVE_HOURS, 0L, 20L))
                .willReturn(twelveHours);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("since", "hOuR")
        ).andReturn().getResponse();

        MockHttpServletResponse response2 = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("since", "six_HOURS")
        ).andReturn().getResponse();

        MockHttpServletResponse response3 = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("since", "tweLVE_houRS")
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response1.getContentAsString()).isEqualTo(json1.getJson());

        assertThat(response2.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response2.getContentAsString()).isEqualTo(json2.getJson());

        assertThat(response3.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response3.getContentAsString()).isEqualTo(json3.getJson());
    }

    @Test
    public void getUserFeed_HandlesNegativeSkipValue() throws Exception {
        // given
        given(blobbService.getFeedForUser(testUser, IBlobbService.BlobbsSince.ONE_HOUR, -1L, 20L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("skip", "-1")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid skip and/or limit values.");
    }

    @Test
    public void getUserFeed_HandlesNegativeLimitValue() throws Exception {
        // given
        given(blobbService.getFeedForUser(testUser, IBlobbService.BlobbsSince.ONE_HOUR, 0L, -1L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("limit", "-1")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid skip and/or limit values.");
    }

    @Test
    public void getUserFeed_ProvidedByParameterIsUsed() throws Exception {
        List<FeedBlobb> testBlobbs1 = List.of(new FeedBlobb());
        List<FeedBlobb> testBlobbs2 = List.of(new FeedBlobb(), new FeedBlobb());

        // json
        JsonContent<List<FeedBlobb>> json1 = jsonFeedBlobbs.write(testBlobbs1);
        JsonContent<List<FeedBlobb>> json2 = jsonFeedBlobbs.write(testBlobbs2);

        // given
        given(blobbService.getFeedForUser_Popular(testUser, IBlobbService.BlobbsSince.ONE_HOUR, 0L, 20L))
                .willReturn(testBlobbs1);
        given(blobbService.getFeedForUser_Popular(testUser, IBlobbService.BlobbsSince.SIX_HOURS, 5L, 12L))
                .willReturn(testBlobbs2);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("by", "popularity")
        ).andReturn().getResponse();

        MockHttpServletResponse response2 = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("by", "PoPULarITY")
                        .param("since", "six_HOURS")
                        .param("limit", "12")
                        .param("skip", "5")
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response1.getContentAsString()).isEqualTo(json1.getJson());

        assertThat(response2.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response2.getContentAsString()).isEqualTo(json2.getJson());
    }
}
