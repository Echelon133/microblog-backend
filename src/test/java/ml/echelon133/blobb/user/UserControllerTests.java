package ml.echelon133.blobb.user;

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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@ExtendWith(MockitoExtension.class)
public class UserControllerTests {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private UserExceptionHandler userExceptionHandler;

    private JacksonTester<User> jsonUser;

    private JacksonTester<UserProfileInfo> jsonUserProfileInfo;

    @BeforeEach
    public void beforeEach() {
        JacksonTester.initFields(this, new ObjectMapper());

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(userExceptionHandler)
                .build();
    }

    @Test
    public void getUser_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + invalidUuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getUser_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findByUuid(uuid))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getUser_ReturnsUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User("test1", "test@mail.com", "", "");
        user.setUuid(uuid);

        // expected json
        JsonContent<User> json = jsonUser.write(user);

        // given
        given(userService.findByUuid(uuid)).willReturn(user);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getUserProfile_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + invalidUuid + "/profile")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getUserProfile_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.getUserProfileInfo(uuid))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/profile")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getUserProfile_ReturnsUserProfileInfo() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserProfileInfo uProfileInfo = new UserProfileInfo();
        uProfileInfo.setUuid(uuid);
        uProfileInfo.setFollows(1L);
        uProfileInfo.setFollowers(10L);

        // expected json
        JsonContent<UserProfileInfo> json = jsonUserProfileInfo.write(uProfileInfo);

        // given
        given(userService.getUserProfileInfo(uuid)).willReturn(uProfileInfo);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/profile")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }
}
