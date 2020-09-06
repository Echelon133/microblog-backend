package ml.echelon133.blobb.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;


@ExtendWith(MockitoExtension.class)
public class UserControllerTests {

    static User testUser;

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private UserExceptionHandler userExceptionHandler;

    private JacksonTester<User> jsonUser;

    private JacksonTester<UserProfileInfo> jsonUserProfileInfo;

    private JacksonTester<List<User>> jsonUsers;

    private JacksonTester<List<UserBlobb>> jsonUserBlobbs;

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
                .standaloneSetup(userController)
                .setControllerAdvice(userExceptionHandler)
                .addFilter(filter)
                .build();
    }

    @Test
    public void getLoggedUser_ReturnsCorrectPrincipal() throws Exception {
        // json
        JsonContent<User> json = jsonUser.write(testUser);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/me")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
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

    @Test
    public void checkIfFollowed_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + invalidUuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void checkIfFollowed_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.checkIfUserFollows(testUser, uuid))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void checkIfFollowed_UserDoesntFollow() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.checkIfUserFollows(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"followed\":false}");
    }

    @Test
    public void checkIfFollowed_UserFollows() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.checkIfUserFollows(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"followed\":true}");
    }

    @Test
    public void followUser_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + invalidUuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void followUser_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.followUserWithUuid(testUser, uuid))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + uuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void followUser_Failure() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.followUserWithUuid(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + uuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"followed\":false}");
    }

    @Test
    public void followUser_Success() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.followUserWithUuid(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + uuid + "/follow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"followed\":true}");
    }

    @Test
    public void unfollowUser_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + invalidUuid + "/unfollow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void unfollowUser_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.unfollowUserWithUuid(testUser, uuid))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + uuid + "/unfollow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void unfollowUser_Failure() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.unfollowUserWithUuid(testUser, uuid))
                .willReturn(false);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + uuid + "/unfollow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"unfollowed\":false}");
    }

    @Test
    public void unfollowUser_Success() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.unfollowUserWithUuid(testUser, uuid))
                .willReturn(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/" + uuid + "/unfollow")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains("{\"unfollowed\":true}");
    }

    @Test
    public void getFollowers_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + invalidUuid + "/followers")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getFollowers_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findAllFollowersOfUser(uuid, 0L, 5L))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/followers")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getFollowers_NoSkipSetsSkipValueToDefault() throws Exception {
        List<User> followers = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(followers);

        // given
        given(userService.findAllFollowersOfUser(uuid, 0L, 20L))
                .willReturn(followers);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/followers")
                        .accept(APPLICATION_JSON)
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getFollowers_NoLimitSetsLimitValueToDefault() throws Exception {
        List<User> followers = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(followers);

        // given
        given(userService.findAllFollowersOfUser(uuid, 10L, 5L))
                .willReturn(followers);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/followers")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getFollowers_DefaultSkipAndLimitIsCorrect() throws Exception {
        List<User> followers = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(followers);

        // given
        given(userService.findAllFollowersOfUser(uuid, 0L, 5L))
                .willReturn(followers);

        // when
        // don't give skip & limit parameters to see if they are set to 0 & 5
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/followers")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getFollowers_ProvidedSkipAndLimitAreUsed() throws Exception {
        List<User> followers = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(followers);

        // given
        given(userService.findAllFollowersOfUser(uuid, 10L, 20L))
                .willReturn(followers);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/followers")
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
    public void getFollows_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + invalidUuid + "/follows")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");
    }

    @Test
    public void getFollows_DoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findAllFollowsOfUser(uuid, 0L, 5L))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follows")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }

    @Test
    public void getFollows_NoSkipSetsSkipValueToDefault() throws Exception {
        List<User> follows = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(follows);

        // given
        given(userService.findAllFollowsOfUser(uuid, 0L, 20L))
                .willReturn(follows);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follows")
                        .accept(APPLICATION_JSON)
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getFollows_NoLimitSetsLimitValueToDefault() throws Exception {
        List<User> follows = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(follows);

        // given
        given(userService.findAllFollowsOfUser(uuid, 10L, 5L))
                .willReturn(follows);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follows")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getFollows_DefaultSkipAndLimitIsCorrect() throws Exception {
        List<User> follows = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(follows);

        // given
        given(userService.findAllFollowsOfUser(uuid, 0L, 5L))
                .willReturn(follows);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follows")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .contains(json.getJson());
    }

    @Test
    public void getFollows_ProvidedSkipAndLimitAreUsed() throws Exception {
        List<User> follows = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        UUID uuid = UUID.randomUUID();

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(follows);

        // given
        given(userService.findAllFollowsOfUser(uuid, 10L, 20L))
                .willReturn(follows);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/follows")
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
    public void getUserByUsername_DoesntExist() throws Exception {
        String invalidUsername = "test321";

        // given
        given(userService.findByUsername(invalidUsername))
                .willThrow(new UserDoesntExistException(invalidUsername));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users")
                        .param("username", invalidUsername)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User %s doesn't exist", invalidUsername));
    }

    @Test
    public void getUserByUsername_ReturnsUser() throws Exception {
        String username = "test1";
        User user = new User(username, "", "", "");

        // expected json
        JsonContent<User> json = jsonUser.write(user);

        // given
        given(userService.findByUsername(username))
                .willReturn(user);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users")
                        .param("username", username)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getUserByUsername_NoRequiredParameter() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void getRecentBlobbs_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + invalidUuid + "/recentBlobbs")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");

    }

    @Test
    public void getRecentBlobbs_NotProvidedParametersSetToDefault() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<UserBlobb> recent = List.of(new UserBlobb(), new UserBlobb());

        // json
        JsonContent<List<UserBlobb>> json = jsonUserBlobbs.write(recent);

        // given
        given(userService.findRecentBlobbsOfUser(uuid, 0L, 10L))
                .willReturn(recent);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentBlobbs")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getRecentBlobbs_ProvidedSkipAndLimitAreUsed() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<UserBlobb> recent = List.of(new UserBlobb(), new UserBlobb());

        // json
        JsonContent<List<UserBlobb>> json = jsonUserBlobbs.write(recent);

        // given
        given(userService.findRecentBlobbsOfUser(uuid, 10L, 20L))
                .willReturn(recent);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentBlobbs")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getRecentBlobbs_CorrectResponseWhenUserDoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findRecentBlobbsOfUser(uuid, 10L, 20L))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentBlobbs")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString())
                .contains(String.format("User with UUID %s doesn't exist", uuid));
    }

    @Test
    public void getRecentBlobbs_CorrectResponseWhenSkipNegative() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findRecentBlobbsOfUser(uuid, -1L, 20L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentBlobbs")
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
    public void getRecentBlobbs_CorrectResponseWhenLimitNegative() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findRecentBlobbsOfUser(uuid, 10L, -20L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentBlobbs")
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
