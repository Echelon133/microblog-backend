package ml.echelon133.microblog.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.microblog.post.PostService;
import ml.echelon133.microblog.post.FeedPost;
import ml.echelon133.microblog.post.IPostService;
import ml.echelon133.microblog.user.User;
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
    private PostService postService;

    @InjectMocks
    private FeedController feedController;

    @InjectMocks
    private FeedExceptionHandler feedExceptionHandler;

    private JacksonTester<List<FeedPost>> jsonFeedPosts;

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
        List<FeedPost> testPosts = List.of(new FeedPost());

        // json
        JsonContent<List<FeedPost>> json = jsonFeedPosts.write(testPosts);

        // given
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.ONE_HOUR, 0L, 20L))
                .willReturn(testPosts);

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
        List<FeedPost> testPosts = List.of(new FeedPost());

        // json
        JsonContent<List<FeedPost>> json = jsonFeedPosts.write(testPosts);

        // given
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.ONE_HOUR, 5L, 50L))
                .willReturn(testPosts);

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
        List<FeedPost> hour = List.of(new FeedPost());
        List<FeedPost> sixHours = List.of(new FeedPost(), new FeedPost());
        List<FeedPost> twelveHours = List.of(new FeedPost(), new FeedPost(), new FeedPost());
        List<FeedPost> day = List.of(new FeedPost(), new FeedPost(), new FeedPost(), new FeedPost());

        // json
        JsonContent<List<FeedPost>> json1 = jsonFeedPosts.write(hour);
        JsonContent<List<FeedPost>> json2 = jsonFeedPosts.write(sixHours);
        JsonContent<List<FeedPost>> json3 = jsonFeedPosts.write(twelveHours);
        JsonContent<List<FeedPost>> json4 = jsonFeedPosts.write(day);

        // given
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.ONE_HOUR, 0L, 20L))
                .willReturn(hour);
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.SIX_HOURS, 0L, 20L))
                .willReturn(sixHours);
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.TWELVE_HOURS, 0L, 20L))
                .willReturn(twelveHours);
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.DAY, 0L, 20L))
                .willReturn(day);

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

        MockHttpServletResponse response4 = mockMvc.perform(
                get("/api/feed")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("since", "Day")
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response1.getContentAsString()).isEqualTo(json1.getJson());

        assertThat(response2.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response2.getContentAsString()).isEqualTo(json2.getJson());

        assertThat(response3.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response3.getContentAsString()).isEqualTo(json3.getJson());

        assertThat(response4.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response4.getContentAsString()).isEqualTo(json4.getJson());
    }

    @Test
    public void getUserFeed_HandlesNegativeSkipValue() throws Exception {
        // given
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.ONE_HOUR, -1L, 20L))
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
        given(postService.getFeedForUser(testUser, IPostService.PostsSince.ONE_HOUR, 0L, -1L))
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
        List<FeedPost> testPosts1 = List.of(new FeedPost());
        List<FeedPost> testPosts2 = List.of(new FeedPost(), new FeedPost());

        // json
        JsonContent<List<FeedPost>> json1 = jsonFeedPosts.write(testPosts1);
        JsonContent<List<FeedPost>> json2 = jsonFeedPosts.write(testPosts2);

        // given
        given(postService.getFeedForUser_Popular(testUser, IPostService.PostsSince.ONE_HOUR, 0L, 20L))
                .willReturn(testPosts1);
        given(postService.getFeedForUser_Popular(testUser, IPostService.PostsSince.SIX_HOURS, 5L, 12L))
                .willReturn(testPosts2);

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
