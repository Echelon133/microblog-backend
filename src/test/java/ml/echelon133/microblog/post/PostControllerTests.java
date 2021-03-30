package ml.echelon133.microblog.post;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerTests {

    static User testUser;

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    @InjectMocks
    private PostExceptionHandler postExceptionHandler;

    private JacksonTester<FeedPost> jsonFeedPost;

    private JacksonTester<PostInfo> jsonPostInfo;

    private JacksonTester<List<FeedPost>> jsonFeedPostList;

    private JacksonTester<PostDto> jsonPostDto;

    private JacksonTester<ResponseDto> jsonResponseDto;

    private JacksonTester<QuotePostDto> jsonQuotesDto;

    private JacksonTester<Map<String, String>> jsonPostResult;

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
                .standaloneSetup(postController)
                .setControllerAdvice(postExceptionHandler)
                .addFilter(filter)
                .build();
    }

    @Test
    public void getPostWithUuid_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + invalidUuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getPostWithUuid_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.getByUuid(uuid))
                .willThrow(new PostDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getPostWithUuid_ReturnsPost() throws Exception {
        UUID uuid = UUID.randomUUID();
        FeedPost post = new FeedPost();
        post.setUuid(uuid);
        post.setAuthor(testUser);

        // expected json
        JsonContent<FeedPost> json = jsonFeedPost.write(post);

        // given
        given(postService.getByUuid(uuid)).willReturn(post);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getInfoAboutPostWithUuid_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + invalidUuid + "/info")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getInfoAboutPostWithUuid_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.getPostInfo(uuid))
                .willThrow(new PostDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/info")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getInfoAboutPostWithUuid_ReturnsPostInfo() throws Exception {
        UUID uuid = UUID.randomUUID();
        PostInfo info = new PostInfo();
        info.setUuid(uuid);
        info.setLikes(10L);
        info.setQuotes(20L);
        info.setResponses(5L);

        // expected json
        JsonContent<PostInfo> json = jsonPostInfo.write(info);

        // given
        given(postService.getPostInfo(uuid)).willReturn(info);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/info")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getResponsesToPost_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + invalidUuid + "/responses")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getResponsesToPost_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.getAllResponsesTo(uuid, 0L, 5L))
                .willThrow(new PostDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getResponsesToPost_NoSkipSetsSkipValueToDefault() throws Exception {
        List<FeedPost> responses = List.of(new FeedPost(), new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(responses);

        // given
        given(postService.getAllResponsesTo(uuid, 0L, 20L))
                .willReturn(responses);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getResponsesToPost_NoLimitSetsLimitValueToDefault() throws Exception {
        List<FeedPost> responses = List.of(new FeedPost(), new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(responses);

        // given
        given(postService.getAllResponsesTo(uuid, 10L, 5L))
                .willReturn(responses);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getResponsesToPost_DefaultSkipAndLimitIsCorrect() throws Exception {
        List<FeedPost> responses = List.of(new FeedPost(), new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(responses);

        // given
        given(postService.getAllResponsesTo(uuid, 0L, 5L))
                .willReturn(responses);

        // when
        // don't give skip & limit parameters to see if they are set to 0 & 5
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getResponsesToPost_ProvidedSkipAndLimitAreUsed() throws Exception {
        List<FeedPost> responses = List.of(new FeedPost(), new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(responses);

        // given
        given(postService.getAllResponsesTo(uuid, 10L, 20L))
                .willReturn(responses);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/responses")
                        .param("skip", "10")
                        .param("limit", "20")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getQuotesOfPost_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + invalidUuid + "/quotes")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getQuotesOfPost_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.getAllQuotesOf(uuid, 0L, 5L))
                .willThrow(new PostDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/quotes")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getQuotesOfPost_NoSkipSetsSkipValueToDefault() throws Exception {
        List<FeedPost> reblobbs = List.of(new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(reblobbs);

        // given
        given(postService.getAllQuotesOf(uuid, 0L, 20L))
                .willReturn(reblobbs);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/quotes")
                        .accept(APPLICATION_JSON)
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getQuotesOfPost_NoLimitSetsLimitValueToDefault() throws Exception {
        List<FeedPost> quotes = List.of(new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(quotes);

        // given
        given(postService.getAllQuotesOf(uuid, 10L, 5L))
                .willReturn(quotes);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/quotes")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getQuotesOfPost_DefaultSkipAndLimitIsCorrect() throws Exception {
        List<FeedPost> quotes = List.of(new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(quotes);

        // given
        given(postService.getAllQuotesOf(uuid, 0L, 5L))
                .willReturn(quotes);

        // when
        // don't give skip & limit parameters to see if they are set to 0 & 5
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/quotes")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getQuotesOfPost_ProvidedSkipAndLimitAreUsed() throws Exception {
        List<FeedPost> quotes = List.of(new FeedPost());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedPost>> json = jsonFeedPostList.write(quotes);

        // given
        given(postService.getAllQuotesOf(uuid, 10L, 20L))
                .willReturn(quotes);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/quotes")
                        .param("skip", "10")
                        .param("limit", "20")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void checkIfLikes_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + invalidUuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void checkIfLikes_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.checkIfUserWithUuidLikes(testUser, uuid))
                .willThrow(new PostDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void checkIfLikes_UserDoesntLike() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.checkIfUserWithUuidLikes(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"liked\":false}");
    }

    @Test
    public void checkIfLikes_UserLikes() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.checkIfUserWithUuidLikes(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/posts/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"liked\":true}");
    }

    @Test
    public void likePost_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + invalidUuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void likePost_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.likePost(testUser, uuid))
                .willThrow(new PostDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void likePost_Failure() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.likePost(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"liked\":false}");
    }

    @Test
    public void likePost_Success() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.likePost(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"liked\":true}");
    }

    @Test
    public void unlikePost_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + invalidUuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void unlikePost_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.unlikePost(testUser, uuid))
                .willThrow(new PostDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + uuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void unlikePost_Failure() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.unlikePost(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + uuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"unliked\":false}");
    }

    @Test
    public void unlikePost_Success() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.unlikePost(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + uuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"unliked\":true}");
    }

    @Test
    public void postPost_RejectsInvalidPostLength() throws Exception {
        PostDto dto1 = new PostDto();
        PostDto dto2 = new PostDto();
        dto1.setContent(""); // length 0
        dto2.setContent(
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        ); // length 301

        // json
        JsonContent<PostDto> json1 = jsonPostDto.write(dto1);
        JsonContent<PostDto> json2 = jsonPostDto.write(dto2);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                post("/api/posts")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json1.getJson())
        ).andReturn().getResponse();

        MockHttpServletResponse response2 = mockMvc.perform(
                post("/api/posts")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json2.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString())
                .contains("Post length is invalid");

        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString())
                .contains("Post length is invalid");
    }

    @Test
    public void postPost_AcceptsValidPostLength() throws Exception {
        PostDto dto1 = new PostDto();
        dto1.setContent("This is a test post");

        Post b = new Post(testUser, dto1.getContent());
        b.setUuid(UUID.randomUUID());

        // json
        JsonContent<PostDto> json = jsonPostDto.write(dto1);
        JsonContent<Map<String, String>> expected = jsonPostResult.write(
                Map.of("uuid", b.getUuid().toString())
        );

        // given
        given(postService.postPost(testUser, dto1.getContent())).willReturn(b);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(expected.getJson());
    }

    @Test
    public void respondToPost_RejectsInvalidPostLength() throws Exception {
        UUID postUuid = UUID.randomUUID();
        ResponseDto dto1 = new ResponseDto();
        ResponseDto dto2 = new ResponseDto();
        dto1.setContent(""); // length 0
        dto2.setContent(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        ); // length 301

        // json
        JsonContent<ResponseDto> json1 = jsonResponseDto.write(dto1);
        JsonContent<ResponseDto> json2 = jsonResponseDto.write(dto2);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                post("/api/posts/" + postUuid + "/respond")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json1.getJson())
        ).andReturn().getResponse();

        MockHttpServletResponse response2 = mockMvc.perform(
                post("/api/posts/" + postUuid + "/respond")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json2.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString())
                .contains("Response length is invalid");

        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString())
                .contains("Response length is invalid");
    }

    @Test
    public void respondToPost_AcceptsValidPostLength() throws Exception {
        UUID postUuid = UUID.randomUUID();
        ResponseDto dto1 = new ResponseDto();
        dto1.setContent("This is a test response");

        Post b = new ResponsePost(testUser, dto1.getContent(), new Post());
        b.setUuid(UUID.randomUUID());

        // json
        JsonContent<ResponseDto> json = jsonResponseDto.write(dto1);
        JsonContent<Map<String, String>> expected = jsonPostResult.write(
                Map.of("uuid", b.getUuid().toString())
        );

        // given
        given(postService.postResponse(testUser, dto1.getContent(), postUuid)).willReturn(b);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + postUuid + "/respond")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(expected.getJson());
    }

    @Test
    public void respondToPost_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "test";

        ResponseDto dto1 = new ResponseDto();
        dto1.setContent("This is a test response");

        // json
        JsonContent<ResponseDto> json = jsonResponseDto.write(dto1);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + invalidUuid + "/respond")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void quoteOfPost_RejectsInvalidPostLength() throws Exception {
        UUID postUuid = UUID.randomUUID();
        QuotePostDto dto1 = new QuotePostDto();
        dto1.setContent(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        ); // length 301

        // json
        JsonContent<QuotePostDto> json1 = jsonQuotesDto.write(dto1);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                post("/api/posts/" + postUuid + "/quote")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json1.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString())
                .contains("Quote length is invalid");
    }

    @Test
    public void quoteOfPost_AcceptsValidPostLength() throws Exception {
        UUID postUuid = UUID.randomUUID();
        QuotePostDto dto1 = new QuotePostDto();
        QuotePostDto dto2 = new QuotePostDto();
        dto1.setContent(""); // quote should accept empty content
        dto2.setContent("Test quote content");

        Post b1 = new QuotePost(testUser, dto1.getContent(), new Post());
        b1.setUuid(UUID.randomUUID());
        Post b2 = new QuotePost(testUser, dto2.getContent(), new Post());
        b2.setUuid(UUID.randomUUID());

        // json
        JsonContent<QuotePostDto> json1 = jsonQuotesDto.write(dto1);
        JsonContent<QuotePostDto> json2 = jsonQuotesDto.write(dto2);
        JsonContent<Map<String, String>> expected1 = jsonPostResult.write(
                Map.of("uuid", b1.getUuid().toString())
        );
        JsonContent<Map<String, String>> expected2 = jsonPostResult.write(
                Map.of("uuid", b2.getUuid().toString())
        );

        // given
        given(postService.postQuote(testUser, dto1.getContent(), postUuid)).willReturn(b1);
        given(postService.postQuote(testUser, dto2.getContent(), postUuid)).willReturn(b2);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                post("/api/posts/" + postUuid + "/quote")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json1.getJson())
        ).andReturn().getResponse();

        MockHttpServletResponse response2 = mockMvc.perform(
                post("/api/posts/" + postUuid + "/quote")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json2.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response1.getContentAsString()).isEqualTo(expected1.getJson());

        assertThat(response2.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response2.getContentAsString()).isEqualTo(expected2.getJson());
    }

    @Test
    public void quoteOfPost_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "test";

        QuotePostDto dto1 = new QuotePostDto();
        dto1.setContent("This is a test quote");

        // json
        JsonContent<QuotePostDto> json = jsonQuotesDto.write(dto1);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/posts/" + invalidUuid + "/quote")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void deletePost_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "test";


        // when
        MockHttpServletResponse response = mockMvc.perform(
                delete("/api/posts/" + invalidUuid)
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void deletePost_CorrectResponseWhenNotAuthorized() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.markPostAsDeleted(testUser, uuid))
                .willThrow(new UserCannotDeletePostException(testUser, uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                delete("/api/posts/" + uuid)
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User %s cannot delete post with %s uuid",
                        testUser.getUsername(), uuid));
    }

    @Test
    public void deletePost_CorrectResponseWhenDeleted() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.markPostAsDeleted(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                delete("/api/posts/" + uuid)
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("{\"deleted\":true}");
    }

    @Test
    public void deletePost_CorrectResponseWhenNotDeleted() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(postService.markPostAsDeleted(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                delete("/api/posts/" + uuid)
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo("{\"deleted\":false}");
    }
}
