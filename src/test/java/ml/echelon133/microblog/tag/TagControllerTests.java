package ml.echelon133.microblog.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
public class TagControllerTests {

    private MockMvc mockMvc;

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    @InjectMocks
    private TagExceptionHandler tagExceptionHandler;

    private JacksonTester<Tag> jsonTag;

    private JacksonTester<List<Tag>> jsonListTags;

    private JacksonTester<List<RecentPost>> jsonRecentPosts;

    @BeforeEach
    public void beforeEach() {
        JacksonTester.initFields(this, new ObjectMapper());

        mockMvc = MockMvcBuilders
                .standaloneSetup(tagController)
                .setControllerAdvice(tagExceptionHandler)
                .build();
    }

    @Test
    public void getTagByName_DoesntExist() throws Exception {
        String name = "testtag1";

        // given
        given(tagService.findByName(name))
                .willThrow(new TagDoesntExistException(name));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags")
                        .param("name", name)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Tag #%s doesn't exist", name));
    }

    @Test
    public void getTagByName_ReturnsTag() throws Exception {
        String name = "testtag1";
        Tag tag = new Tag(name);

        // expected json
        JsonContent<Tag> json = jsonTag.write(tag);

        // given
        given(tagService.findByName(name))
                .willReturn(tag);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags")
                        .param("name", name)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getTagByName_NoRequiredParameter() throws Exception {

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void listPopularTags_NotProvidedParametersSetToDefault() throws Exception {
        List<Tag> tags = List.of(new Tag("#test"), new Tag("#test1"));

        // json
        JsonContent<List<Tag>> json = jsonListTags.write(tags);

        // given
        // not provided 'limit' and 'since' are set to 5L and ONE_HOUR respectively
        given(tagService.findMostPopular(5L, ITagService.PopularSince.ONE_HOUR))
                .willReturn(tags);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/popular")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void listPopularTags_ProvidedLimitParameterIsUsed() throws Exception {
        List<Tag> tags = List.of(new Tag("#test"), new Tag("#test1"));

        // json
        JsonContent<List<Tag>> json = jsonListTags.write(tags);

        // given
        given(tagService.findMostPopular(30L, ITagService.PopularSince.ONE_HOUR))
                .willReturn(tags);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/popular")
                        .accept(APPLICATION_JSON)
                        .param("limit", "30")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void listPopularTags_ProvidedSinceParameterIsUsed() throws Exception {
        List<Tag> oneHour = List.of(new Tag("#test"));
        List<Tag> oneDay = List.of(new Tag("#test123"));
        List<Tag> oneWeek = List.of(new Tag("#test3333"));

        // json
        JsonContent<List<Tag>> json1 = jsonListTags.write(oneHour);
        JsonContent<List<Tag>> json2 = jsonListTags.write(oneDay);
        JsonContent<List<Tag>> json3 = jsonListTags.write(oneWeek);

        // given
        given(tagService.findMostPopular(5L, ITagService.PopularSince.ONE_HOUR))
                .willReturn(oneHour);
        given(tagService.findMostPopular(5L, ITagService.PopularSince.DAY))
                .willReturn(oneDay);
        given(tagService.findMostPopular(5L, ITagService.PopularSince.WEEK))
                .willReturn(oneWeek);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                get("/api/tags/popular")
                        .accept(APPLICATION_JSON)
                        .param("since", "hOUr")
        ).andReturn().getResponse();

        MockHttpServletResponse response2 = mockMvc.perform(
                get("/api/tags/popular")
                        .accept(APPLICATION_JSON)
                        .param("since", "dAY")
        ).andReturn().getResponse();

        MockHttpServletResponse response3 = mockMvc.perform(
                get("/api/tags/popular")
                        .accept(APPLICATION_JSON)
                        .param("since", "weeK")
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
    public void listPopularTags_CorrectResponseWhenLimitNegative() throws Exception {
        // given
        given(tagService.findMostPopular(-1L, ITagService.PopularSince.ONE_HOUR))
                .willThrow(new IllegalArgumentException("Limit cannot be negative"));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/popular")
                        .accept(APPLICATION_JSON)
                        .param("limit", "-1")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Limit cannot be negative");
    }

    @Test
    public void findRecentPosts_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/" + invalidUuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");

    }

    @Test
    public void findRecentPosts_NotProvidedParametersSetToDefault() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<RecentPost> recent = List.of(new RecentPost(), new RecentPost());

        // json
        JsonContent<List<RecentPost>> json = jsonRecentPosts.write(recent);

        // given
        given(tagService.findRecentPostsTagged(uuid, 0L, 5L))
                .willReturn(recent);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void findRecentPosts_ProvidedSkipAndLimitAreUsed() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<RecentPost> recent = List.of(new RecentPost(), new RecentPost());

        // json
        JsonContent<List<RecentPost>> json = jsonRecentPosts.write(recent);

        // given
        given(tagService.findRecentPostsTagged(uuid, 10L, 20L))
                .willReturn(recent);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void findRecentPosts_CorrectResponseWhenTagDoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagService.findRecentPostsTagged(uuid, 10L, 20L))
                .willThrow(new TagDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("Tag with UUID %s doesn't exist", uuid));
    }

    @Test
    public void findRecentPosts_CorrectResponseWhenSkipNegative() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagService.findRecentPostsTagged(uuid, -1L, 20L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // whens
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
                        .param("skip", "-1")
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Invalid skip and/or limit values.");
    }

    @Test
    public void findRecentPosts_CorrectResponseWhenLimitNegative() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagService.findRecentPostsTagged(uuid, 10L, -20L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/tags/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
                        .param("limit", "-20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Invalid skip and/or limit values.");
    }
}
