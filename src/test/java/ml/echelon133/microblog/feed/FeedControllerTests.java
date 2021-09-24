package ml.echelon133.microblog.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.microblog.feed.controller.FeedController;
import ml.echelon133.microblog.feed.exception.FeedExceptionHandler;
import ml.echelon133.microblog.post.service.PostService;
import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.model.UserPost;
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

    private JacksonTester<List<UserPost>> jsonUserPosts;

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
        List<UserPost> testPosts = List.of(new UserPost());

        // json
        JsonContent<List<UserPost>> json = jsonUserPosts.write(testPosts);

        // given
        given(postService.getFeedForUser(testUser, 0L, 20L))
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
        List<UserPost> testPosts = List.of(new UserPost());

        // json
        JsonContent<List<UserPost>> json = jsonUserPosts.write(testPosts);

        // given
        given(postService.getFeedForUser(testUser, 5L, 50L))
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
    public void getUserFeed_HandlesNegativeSkipValue() throws Exception {
        // given
        given(postService.getFeedForUser(testUser, -1L, 20L))
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
        given(postService.getFeedForUser(testUser, 0L, -1L))
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
        List<UserPost> testPosts1 = List.of(new UserPost());
        List<UserPost> testPosts2 = List.of(new UserPost(), new UserPost());

        // json
        JsonContent<List<UserPost>> json1 = jsonUserPosts.write(testPosts1);
        JsonContent<List<UserPost>> json2 = jsonUserPosts.write(testPosts2);

        // given
        given(postService.getFeedForUser_Popular(testUser,0L, 20L))
                .willReturn(testPosts1);
        given(postService.getFeedForUser_Popular(testUser, 5L, 12L))
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
