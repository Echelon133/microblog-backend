package ml.echelon133.blobb.blobb;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
public class BlobbControllerTests {

    static User testUser;

    private MockMvc mockMvc;

    @Mock
    private BlobbService blobbService;

    @InjectMocks
    private BlobbController blobbController;

    @InjectMocks
    private BlobbExceptionHandler blobbExceptionHandler;

    private JacksonTester<FeedBlobb> jsonFeedBlobb;

    private JacksonTester<BlobbInfo> jsonBlobbInfo;

    private JacksonTester<List<FeedBlobb>> jsonFeedBlobbList;

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
                .standaloneSetup(blobbController)
                .setControllerAdvice(blobbExceptionHandler)
                .addFilter(filter)
                .build();
    }

    @Test
    public void getBlobbWithUuid_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + invalidUuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getBlobbWithUuid_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.getByUuid(uuid))
                .willThrow(new BlobbDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getBlobbWithUuid_ReturnsBlobb() throws Exception {
        UUID uuid = UUID.randomUUID();
        FeedBlobb blobb = new FeedBlobb();
        blobb.setUuid(uuid);
        blobb.setAuthor(testUser);

        // expected json
        JsonContent<FeedBlobb> json = jsonFeedBlobb.write(blobb);

        // given
        given(blobbService.getByUuid(uuid)).willReturn(blobb);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getInfoAboutBlobbWithUuid_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + invalidUuid + "/info")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getInfoAboutBlobbWithUuid_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.getBlobbInfo(uuid))
                .willThrow(new BlobbDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/info")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getInfoAboutBlobbWithUuid_ReturnsBlobbInfo() throws Exception {
        UUID uuid = UUID.randomUUID();
        BlobbInfo info = new BlobbInfo();
        info.setUuid(uuid);
        info.setLikes(10L);
        info.setReblobbs(20L);
        info.setResponses(5L);

        // expected json
        JsonContent<BlobbInfo> json = jsonBlobbInfo.write(info);

        // given
        given(blobbService.getBlobbInfo(uuid)).willReturn(info);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/info")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getResponsesToBlobb_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + invalidUuid + "/responses")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getResponsesToBlobb_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.getAllResponsesTo(uuid, 0L, 5L))
                .willThrow(new BlobbDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getResponsesToBlobb_NoSkipSetsSkipValueToDefault() throws Exception {
        List<FeedBlobb> responses = List.of(new FeedBlobb(), new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(responses);

        // given
        given(blobbService.getAllResponsesTo(uuid, 0L, 20L))
                .willReturn(responses);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getResponsesToBlobb_NoLimitSetsLimitValueToDefault() throws Exception {
        List<FeedBlobb> responses = List.of(new FeedBlobb(), new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(responses);

        // given
        given(blobbService.getAllResponsesTo(uuid, 10L, 5L))
                .willReturn(responses);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getResponsesToBlobb_DefaultSkipAndLimitIsCorrect() throws Exception {
        List<FeedBlobb> responses = List.of(new FeedBlobb(), new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(responses);

        // given
        given(blobbService.getAllResponsesTo(uuid, 0L, 5L))
                .willReturn(responses);

        // when
        // don't give skip & limit parameters to see if they are set to 0 & 5
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/responses")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getResponsesToBlobb_ProvidedSkipAndLimitAreUsed() throws Exception {
        List<FeedBlobb> responses = List.of(new FeedBlobb(), new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(responses);

        // given
        given(blobbService.getAllResponsesTo(uuid, 10L, 20L))
                .willReturn(responses);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/responses")
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
    public void getReblobbsOfBlobb_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + invalidUuid + "/reblobbs")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getReblobbsOfBlobb_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.getAllReblobbsOf(uuid, 0L, 5L))
                .willThrow(new BlobbDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/reblobbs")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getReblobbsOfBlobb_NoSkipSetsSkipValueToDefault() throws Exception {
        List<FeedBlobb> reblobbs = List.of(new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(reblobbs);

        // given
        given(blobbService.getAllReblobbsOf(uuid, 0L, 20L))
                .willReturn(reblobbs);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/reblobbs")
                        .accept(APPLICATION_JSON)
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getReblobbsOfBlobb_NoLimitSetsLimitValueToDefault() throws Exception {
        List<FeedBlobb> reblobbs = List.of(new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(reblobbs);

        // given
        given(blobbService.getAllReblobbsOf(uuid, 10L, 5L))
                .willReturn(reblobbs);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/reblobbs")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getReblobbsOfBlobb_DefaultSkipAndLimitIsCorrect() throws Exception {
        List<FeedBlobb> reblobbs = List.of(new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(reblobbs);

        // given
        given(blobbService.getAllReblobbsOf(uuid, 0L, 5L))
                .willReturn(reblobbs);

        // when
        // don't give skip & limit parameters to see if they are set to 0 & 5
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/reblobbs")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getReblobbsOfBlobb_ProvidedSkipAndLimitAreUsed() throws Exception {
        List<FeedBlobb> reblobbs = List.of(new FeedBlobb());

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<FeedBlobb>> json = jsonFeedBlobbList.write(reblobbs);

        // given
        given(blobbService.getAllReblobbsOf(uuid, 10L, 20L))
                .willReturn(reblobbs);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/reblobbs")
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
                get("/api/blobbs/" + invalidUuid + "/like")
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
        given(blobbService.checkIfUserWithUuidLikes(testUser, uuid))
                .willThrow(new BlobbDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void checkIfLikes_UserDoesntLike() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.checkIfUserWithUuidLikes(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/like")
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
        given(blobbService.checkIfUserWithUuidLikes(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/blobbs/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"liked\":true}");
    }

    @Test
    public void likeBlobb_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + invalidUuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void likeBlobb_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.likeBlobb(testUser, uuid))
                .willThrow(new BlobbDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void likeBlobb_Failure() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.likeBlobb(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"liked\":false}");
    }

    @Test
    public void likeBlobb_Success() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.likeBlobb(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + uuid + "/like")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"liked\":true}");
    }

    @Test
    public void unlikeBlobb_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + invalidUuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void unlikeBlobb_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.unlikeBlobb(testUser, uuid))
                .willThrow(new BlobbDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + uuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void unlikeBlobb_Failure() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.unlikeBlobb(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + uuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"unliked\":false}");
    }

    @Test
    public void unlikeBlobb_Success() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(blobbService.unlikeBlobb(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/blobbs/" + uuid + "/unlike")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"unliked\":true}");
    }
}
