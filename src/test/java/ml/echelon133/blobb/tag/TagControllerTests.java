package ml.echelon133.blobb.tag;

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
}
