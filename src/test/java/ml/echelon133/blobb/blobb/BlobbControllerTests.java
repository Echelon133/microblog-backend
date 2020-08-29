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

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
}
