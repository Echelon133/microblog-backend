package ml.echelon133.microblog.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.echelon133.microblog.user.controller.UserController;
import ml.echelon133.microblog.user.exception.UserCreationFailedException;
import ml.echelon133.microblog.user.exception.UserDoesntExistException;
import ml.echelon133.microblog.user.exception.UserExceptionHandler;
import ml.echelon133.microblog.user.exception.UsernameAlreadyTakenException;
import ml.echelon133.microblog.user.model.*;
import ml.echelon133.microblog.user.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
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

    private JacksonTester<List<UserPost>> jsonUserPosts;

    private JacksonTester<UserDetailsDto> jsonUserDetailsDto;

    private JacksonTester<NewUserDto> jsonNewUserDto;

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

        // given
        given(userService.findByUsername(testUser.getUsername()))
                .willReturn(testUser);

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
    public void updateUserDetails_RejectsFieldsWithInvalidLength() throws Exception {
        UserDetailsDto dto1 = new UserDetailsDto();
        UserDetailsDto dto2 = new UserDetailsDto();

        // invalid because both are not at least 1 character long
        dto1.setDisplayedUsername("");
        dto1.setDescription("");

        // invalid because both are above max character in length
        // length 71
        dto2.setDisplayedUsername("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        // length 201
        dto2.setDescription("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                            "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                            "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                            "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");

        // json
        JsonContent<UserDetailsDto> json1 = jsonUserDetailsDto.write(dto1);
        JsonContent<UserDetailsDto> json2 = jsonUserDetailsDto.write(dto2);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                put("/api/users/me")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json1.getJson())
        ).andReturn().getResponse();

        MockHttpServletResponse response2 = mockMvc.perform(
                put("/api/users/me")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json2.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString()).contains("Username length is invalid");
        assertThat(response1.getContentAsString()).contains("Description length is invalid");

        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString()).contains("Username length is invalid");
        assertThat(response2.getContentAsString()).contains("Description length is invalid");
    }

    @Test
    public void updateUserDetails_AcceptsFieldsWithValidLength() throws Exception {
        UserDetailsDto dto = new UserDetailsDto();

        // set all fields to their minimum length
        dto.setDisplayedUsername("d");
        dto.setDescription("d");
        dto.setAviURL("");

        // json
        JsonContent<UserDetailsDto> json = jsonUserDetailsDto.write(dto);

        // given
        given(userService.updateUser(any(), any())).willReturn(testUser);

        // when
        MockHttpServletResponse response1 = mockMvc.perform(
                put("/api/users/me")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.OK.value());
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
        List<User> users = List.of(user);

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(users);

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
    public void getUserByUsername_SearchReturnsEmptyResult() throws Exception {
        String invalidUsername = "test321";

        // given
        given(userService.findAllByUsernameContains(invalidUsername))
                .willReturn(List.of());

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users")
                        .param("search", invalidUsername)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("[]");
    }

    @Test
    public void getUserByUsername_SearchReturnsUsers() throws Exception {
        String username = "test1";
        List<User> users = List.of(
                new User(username + "23", "", "", ""),
                new User(username, "", "", "")
        );

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(users);

        // given
        given(userService.findAllByUsernameContains(username))
                .willReturn(users);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users")
                        .param("search", username)
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getUserByUsername_ParametersMutuallyExclusive() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users")
                        .param("search", "test")
                        .param("username", "test")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Parameters username and search mustn't be combined");
    }

    @Test
    public void getUserByUsername_NoRequiredParameters() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Parameters username or search must be specified");
    }

    @Test
    public void getRecentPosts_HandlesInvalidUuid() throws Exception {
        String invalidUuid = "asdf";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + invalidUuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Invalid UUID string");

    }

    @Test
    public void getRecentPosts_NotProvidedParametersSetToDefault() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<UserPost> recent = List.of(new UserPost(), new UserPost());

        // json
        JsonContent<List<UserPost>> json = jsonUserPosts.write(recent);

        // given
        given(userService.findRecentPostsOfUser(uuid, 0L, 10L))
                .willReturn(recent);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getRecentPosts_ProvidedSkipAndLimitAreUsed() throws Exception {
        UUID uuid = UUID.randomUUID();

        List<UserPost> recent = List.of(new UserPost(), new UserPost());

        // json
        JsonContent<List<UserPost>> json = jsonUserPosts.write(recent);

        // given
        given(userService.findRecentPostsOfUser(uuid, 10L, 20L))
                .willReturn(recent);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
                        .param("limit", "20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getRecentPosts_CorrectResponseWhenUserDoesntExist() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findRecentPostsOfUser(uuid, 10L, 20L))
                .willThrow(new UserDoesntExistException(uuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentPosts")
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
    public void getRecentPosts_CorrectResponseWhenSkipNegative() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findRecentPostsOfUser(uuid, -1L, 20L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentPosts")
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
    public void getRecentPosts_CorrectResponseWhenLimitNegative() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(userService.findRecentPostsOfUser(uuid, 10L, -20L))
                .willThrow(new IllegalArgumentException("Invalid skip and/or limit values."));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + uuid + "/recentPosts")
                        .accept(APPLICATION_JSON)
                        .param("skip", "10")
                        .param("limit", "-20")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("Invalid skip and/or limit values.");
    }

    @Test
    public void registerUser_CorrectResponseWhenPayloadNull() throws Exception {
        String payload = "{}";

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/register")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(payload)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Username is not valid");
        assertThat(response.getContentAsString()).contains("Passwords do not match");
        assertThat(response.getContentAsString()).contains("Email is required");
        assertThat(response.getContentAsString()).contains("Password doesn't satisfy complexity requirements");
    }

    @Test
    public void registerUser_CorrectResponseWhenUsernameLengthInvalid() throws Exception {
        List<String> invalidUsernames = List.of(
                "", // 0 characters
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab",  // 31 characters
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAb",  // 31 characters
                "0123456789012345678901234567890" // 31 characters
        );

        for (String invalidUsername : invalidUsernames) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setUsername(invalidUsername);

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).contains("Username is not valid");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenUsernameContainsInvalidCharacters() throws Exception {
        String invalidCharacters = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

        for (int i = 0; i < invalidCharacters.length(); i++) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setUsername(String.valueOf(invalidCharacters.charAt(i)));

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).contains("Username is not valid");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenUsernameValid() throws Exception {
        List<String> validUsernames = List.of(
                "a", // one lower char
                "A", // one upper char
                "0", // one number
                "test", // random lower char password
                "TEST", // random upper char password
                "0123", // random number password
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",  // 30 characters
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",  // 30 characters
                "012345678901234567890123456789" // 30 characters
        );

        for (String validUsername : validUsernames) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setUsername(validUsername);

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).doesNotContain("Username is not valid");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenEmailInvalid() throws Exception {
        List<String> invalidEmails = List.of(
                "a",  // no @
                "a@",  // no domain
                "a@.", // invalid domain
                "a@.com" // invalid domain
        );

        for (String invalidEmail : invalidEmails) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setEmail(invalidEmail);

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).contains("Email is not valid");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenPasswordLengthInvalid() throws Exception {
        List<String> invalidPasswords = List.of(
                "Aa;1aaa", // 7 characters
                "Aa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaBBBBB" // 65 characters
        );

        for (String invalidPassword : invalidPasswords) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setPassword(invalidPassword);

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).contains("Expected password length between 8 and 64 characters");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenPasswordLengthValid() throws Exception {
        List<String> validPasswords = List.of(
                "Aa;1aaaa", // 8 characters
                "Aa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaBBBB" // 64 characters
        );

        for (String validPassword : validPasswords) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setPassword(validPassword);

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).doesNotContain("Expected password length between 8 and 64 characters");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenPasswordComplexityValid() throws Exception {
        List<String> validPasswords = List.of(
                "Aa;1aaaa",
                "Aa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaBBBB",
                "Aa1 !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
        );

        for (String validPassword : validPasswords) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setPassword(validPassword);

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).doesNotContain("Password doesn't satisfy complexity requirements");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenPasswordsDoNotMatch() throws Exception {
        List<String> validPasswords = List.of(
                "Aa;1aaaa",
                "Aa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaBBBB",
                "Aa1 !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
        );

        for (String validPassword : validPasswords) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setPassword(validPassword);
            newUserDto.setPassword2("asdf"); // make passwords differ

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).contains("Passwords do not match");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenPasswordsMatch() throws Exception {
        List<String> validPasswords = List.of(
                "Aa;1aaaa",
                "Aa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaAa;1aaaaaaBBBB",
                "Aa1 !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
        );

        for (String validPassword : validPasswords) {
            NewUserDto newUserDto = new NewUserDto();
            newUserDto.setPassword(validPassword);
            newUserDto.setPassword2(validPassword);

            JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

            // when
            MockHttpServletResponse response = mockMvc.perform(
                    post("/api/users/register")
                            .accept(APPLICATION_JSON)
                            .contentType(APPLICATION_JSON)
                            .content(json.getJson())
            ).andReturn().getResponse();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(response.getContentAsString()).doesNotContain("Passwords do not match");
        }
    }

    @Test
    public void registerUser_CorrectResponseWhenDataValid() throws Exception {
        User user = new User();
        user.setUuid(UUID.randomUUID());

        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setUsername("test");
        newUserDto.setEmail("test@test.com");
        newUserDto.setPassword("Aa1;aaaa");
        newUserDto.setPassword2("Aa1;aaaa");

        JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

        // given
        given(userService.setupAndSaveUser(any())).willReturn(user);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/register")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                String.format("{\"uuid\":\"%s\"}", user.getUuid())
        );
    }

    @Test
    public void registerUser_CorrectResponseWhenUsernameAlreadyTaken() throws Exception {
        User user = new User();
        user.setUuid(UUID.randomUUID());

        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setUsername("test");
        newUserDto.setEmail("test@test.com");
        newUserDto.setPassword("Aa1;aaaa");
        newUserDto.setPassword2("Aa1;aaaa");

        JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

        // given
        given(userService.setupAndSaveUser(any())).willThrow(
                new UsernameAlreadyTakenException("Username already taken")
        );

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/register")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Username already taken");
    }

    @Test
    public void registerUser_CorrectResponseWhenUserCreationFails() throws Exception {
        User user = new User();
        user.setUuid(UUID.randomUUID());

        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setUsername("test");
        newUserDto.setEmail("test@test.com");
        newUserDto.setPassword("Aa1;aaaa");
        newUserDto.setPassword2("Aa1;aaaa");

        JsonContent<NewUserDto> json = jsonNewUserDto.write(newUserDto);

        // given
        given(userService.setupAndSaveUser(any())).willThrow(
                new UserCreationFailedException("User creation failed")
        );

        // when
        MockHttpServletResponse response = mockMvc.perform(
                post("/api/users/register")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(json.getJson())
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getContentAsString()).contains("User creation failed");
    }

    @Test
    public void getKnownFollowers_UserDoesNotExist() throws Exception {
        UUID otherUuid = UUID.randomUUID();

        // given
        given(userService.findFollowersUserKnows(testUser, otherUuid, 0L, 5L))
                .willThrow(new UserDoesntExistException(otherUuid));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + otherUuid + "/knownFollowers")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        String expectedMsg = String.format("User with UUID %s doesn't exist", otherUuid.toString());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).contains(expectedMsg);
    }

    @Test
    public void getKnownFollowers_UserChecksCommonWithThemselves() throws Exception {
        UUID otherUuid = testUser.getUuid();

        // given
        given(userService.findFollowersUserKnows(testUser, otherUuid, 0L, 5L))
                .willThrow(new IllegalArgumentException("UUID of checked user is equal to the UUID of currently logged in user"));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + otherUuid + "/knownFollowers")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("UUID of checked user is equal to the UUID of currently logged in user");
    }

    @Test
    public void getKnownFollowers_NoSkipAndNoLimitSetsValuesToDefault() throws Exception {
        UUID otherUuid = UUID.randomUUID();

        List<User> common = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(common);

        // given
        given(userService.findFollowersUserKnows(testUser, otherUuid, 0L, 5L))
                .willReturn(common);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + otherUuid + "/knownFollowers")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }

    @Test
    public void getKnownFollowers_ProvidedSkipAndLimitAreUsed() throws Exception {
        UUID otherUuid = UUID.randomUUID();

        List<User> common = List.of(
                new User("test1", "","",""),
                new User("test2", "", "", ""));

        // expected json
        JsonContent<List<User>> json = jsonUsers.write(common);

        // given
        given(userService.findFollowersUserKnows(testUser, otherUuid, 10L, 15L))
                .willReturn(common);

        // when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/users/" + otherUuid + "/knownFollowers")
                        .accept(APPLICATION_JSON)
                        .with(user(testUser))
                        .param("skip", "10")
                        .param("limit", "15")
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(json.getJson());
    }
}
