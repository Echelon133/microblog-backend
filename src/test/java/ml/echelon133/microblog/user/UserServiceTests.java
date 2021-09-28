package ml.echelon133.microblog.user;

import ml.echelon133.microblog.user.exception.HiddenStateModificationAttemptException;
import ml.echelon133.microblog.user.exception.UserCreationFailedException;
import ml.echelon133.microblog.user.exception.UserDoesntExistException;
import ml.echelon133.microblog.user.exception.UsernameAlreadyTakenException;
import ml.echelon133.microblog.user.model.*;
import ml.echelon133.microblog.user.repository.RoleRepository;
import ml.echelon133.microblog.user.repository.UserRepository;
import ml.echelon133.microblog.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User getTestUser() {
        User u = new User("test", "", "", "");
        u.setUuid(UUID.randomUUID());
        return u;
    }

    @Test
    public void setupAndSaveUser_ThrowsWhenUsernameAlreadyTaken() {
        User user = getTestUser();

        // given
        given(userRepository.existsUserByUsername(user.getUsername())).willReturn(true);

        // when
        String msg = assertThrows(UsernameAlreadyTakenException.class, () -> {
            userService.setupAndSaveUser(user);
        }).getMessage();

        // then
        assertEquals("Username already taken", msg);
    }

    @Test
    public void setupAndSaveUser_EncodesUserPassword() throws Exception {
        User user = getTestUser();
        String expectedPassword = "encoded" + user.getPassword();

        // given
        given(userRepository.existsUserByUsername(user.getUsername())).willReturn(false);
        given(passwordEncoder.encode(user.getPassword())).willReturn(expectedPassword);
        given(userRepository.save(user)).willReturn(user);
        given(userRepository.followUserWithUuid(user.getUuid(), user.getUuid())).willReturn(Optional.of(1L));
        given(roleRepository.findByName("ROLE_USER"))
                .willReturn(Optional.of(new Role("ROLE_USER")));

        // when
        User savedUser = userService.setupAndSaveUser(user);

        // then
        assertEquals(expectedPassword, savedUser.getPassword());
    }

    @Test
    public void setupAndSaveUser_ThrowsWhenSetupFails() {
        User user = getTestUser();

        // given
        given(userRepository.existsUserByUsername(user.getUsername())).willReturn(false);
        given(passwordEncoder.encode(user.getPassword())).willReturn(user.getPassword());
        given(userRepository.save(user)).willReturn(user);
        given(userRepository.followUserWithUuid(user.getUuid(), user.getUuid()))
                .willReturn(Optional.empty());
        given(roleRepository.findByName("ROLE_USER"))
                .willReturn(Optional.of(new Role("ROLE_USER")));

        // when
        String msg = assertThrows(UserCreationFailedException.class, () -> {
            userService.setupAndSaveUser(user);
        }).getMessage();

        // then
        assertEquals("User creation failed", msg);
    }

    @Test
    public void followUserWithUuid_ThrowsWhenUserDoesntExist() {
        User user = getTestUser();
        UUID u2Uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(false);

        // when
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.followUserWithUuid(user, u2Uuid);
        }).getMessage();

        assertEquals(String.format("User with UUID %s doesn't exist", u2Uuid), message);
    }

    @Test
    public void followUserWithUuid_WhenUserDoesntAlreadyFollow() throws Exception {
        User user = getTestUser();
        UUID u2Uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), u2Uuid)).willReturn(Optional.empty());
        given(userRepository.followUserWithUuid(user.getUuid(), u2Uuid)).willReturn(Optional.of(1L));

        // when
        boolean result = userService.followUserWithUuid(user, u2Uuid);

        // then
        assertTrue(result);
    }

    @Test
    public void followUserWithUuid_WhenUserAlreadyFollows() throws Exception {
        User user = getTestUser();
        UUID u2Uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), u2Uuid)).willReturn(Optional.of(1L));

        // when
        boolean result = userService.followUserWithUuid(user, u2Uuid);

        // then
        assertTrue(result);
    }

    @Test
    public void followUserWithUuid_WhenFollowingFails() throws Exception {
        User user = getTestUser();
        UUID u2Uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), u2Uuid)).willReturn(Optional.empty());
        given(userRepository.followUserWithUuid(user.getUuid(), u2Uuid)).willReturn(Optional.empty());

        // when
        boolean result = userService.followUserWithUuid(user, u2Uuid);

        // then
        assertFalse(result);
    }

    @Test
    public void followUserWithUuid_ThrowsWhenUserFollowsThemselves() {
        User user = getTestUser();

        // given
        given(userRepository.existsById(user.getUuid())).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), user.getUuid())).willReturn(Optional.empty());

        // then
        String message = assertThrows(HiddenStateModificationAttemptException.class, () -> {
            userService.followUserWithUuid(user, user.getUuid());
        }).getMessage();

        assertEquals("Users cannot follow themselves.", message);
    }

    @Test
    public void unfollowUserWithUuid_ThrowsWhenUserDoesntExist() {
        User user = getTestUser();
        UUID u2Uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(false);

        // when
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.unfollowUserWithUuid(user, u2Uuid);
        }).getMessage();

        // then
        assertEquals(String.format("User with UUID %s doesn't exist", u2Uuid), message);
    }

    @Test
    public void unfollowUserWithUuid_WhenUnfollowSucceeds() throws Exception {
        User user = getTestUser();
        UUID u2Uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), u2Uuid)).willReturn(Optional.empty());

        // when
        boolean result = userService.unfollowUserWithUuid(user, u2Uuid);

        // then
        assertTrue(result);
    }

    @Test
    public void unfollowUserWithUuid_WhenUnfollowFails() throws Exception {
        User user = getTestUser();
        UUID u2Uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), u2Uuid)).willReturn(Optional.of(1L));

        // when
        boolean result = userService.unfollowUserWithUuid(user, u2Uuid);

        // then
        assertFalse(result);
    }

    @Test
    public void unfollowUserWithUuid_ThrowsWhenUserTriesToUnfollowThemselves() {
        User user = getTestUser();
        UUID u2Uuid = user.getUuid();

        // given
        given(userRepository.existsById(u2Uuid)).willReturn(true);

        // then
        String message = assertThrows(HiddenStateModificationAttemptException.class, () -> {
            userService.unfollowUserWithUuid(user, user.getUuid());
        }).getMessage();

        assertEquals("Users cannot unfollow themselves.", message);
    }

    @Test
    public void findAllFollowsOfUser_ThrowsWhenUserDoesntExist() {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(false);

        // when
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.findAllFollowsOfUser(uUuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals(String.format("User with UUID %s doesn't exist", uUuid), message);
    }

    @Test
    public void findAllFollowsOfUser_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findAllFollowsOfUser(uUuid, -1L, 0L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findAllFollowsOfUser(uUuid, 0L, -1L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void findAllFollowsOfUser_ReturnsEmptyListIfNobodyFollowed() throws Exception {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);
        given(userRepository.findAllFollowsOfUserWithUuid(uUuid, 0L, 5L)).willReturn(List.of());

        // when
        List<User> followedBy = userService.findAllFollowsOfUser(uUuid, 0L, 5L);

        // then
        assertEquals(0, followedBy.size());
    }

    @Test
    public void findAllFollowsOfUser_ReturnsListOfFollowers() throws Exception {
        UUID uUuid = UUID.randomUUID();

        List<User> mockList = List.of(new User(), new User(), new User());

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);
        given(userRepository.findAllFollowsOfUserWithUuid(uUuid, 0L, 5L)).willReturn(mockList);

        // when
        List<User> followedBy = userService.findAllFollowsOfUser(uUuid, 0L, 5L);

        // then
        assertEquals(3, followedBy.size());
    }

    @Test
    public void findAllFollowersOfUser_ThrowsWhenUserDoesntExist() {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(false);

        // when
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.findAllFollowersOfUser(uUuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals(String.format("User with UUID %s doesn't exist", uUuid), message);
    }

    @Test
    public void findAllFollowersOfUser_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findAllFollowersOfUser(uUuid, -1L, 0L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findAllFollowersOfUser(uUuid, 0L, -1L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void findAllFollowersOfUser_ReturnsListOfFollowers() throws Exception {
        UUID uUuid = UUID.randomUUID();

        List<User> mockList = List.of(getTestUser(), getTestUser(), getTestUser());

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);
        given(userRepository.findAllFollowersOfUserWithUuid(uUuid, 0L, 5L)).willReturn(mockList);

        // when
        List<User> following = userService.findAllFollowersOfUser(uUuid, 0L, 5L);

        // then
        assertEquals(3, following.size());
    }

    @Test
    public void findAllFollowersOfUser_ReturnsEmptyListIfNobodyFollowed() throws Exception {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);
        given(userRepository.findAllFollowersOfUserWithUuid(uUuid, 0L, 5L)).willReturn(List.of());

        // when
        List<User> following = userService.findAllFollowersOfUser(uUuid, 0L, 5L);

        // then
        assertEquals(0, following.size());
    }

    @Test
    public void getUserProfileInfo_ThrowsWhenUserDoesntExist() {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(false);

        // then
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.getUserProfileInfo(uUuid);
        }).getMessage();

        assertEquals(String.format("User with UUID %s doesn't exist", uUuid), message);
    }

    @Test
    public void getUserProfileInfo_ReturnsPlaceholderObjectWhenFails() throws Exception {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);
        given(userRepository.getUserProfileInfo(uUuid)).willReturn(Optional.empty());

        // when
        UserProfileInfo profileInfo = userService.getUserProfileInfo(uUuid);

        // then
        assertNull(profileInfo.getFollows());
        assertNull(profileInfo.getFollowers());
    }

    @Test
    public void getUserProfileInfo_ReturnsObject() throws Exception {
        UUID uUuid = UUID.randomUUID();

        UserProfileInfo mockProfileInfo = new UserProfileInfo();
        mockProfileInfo.setFollowers(10L);
        mockProfileInfo.setFollows(20L);

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);
        given(userRepository.getUserProfileInfo(uUuid)).willReturn(Optional.of(mockProfileInfo));

        // when
        UserProfileInfo profileInfo = userService.getUserProfileInfo(uUuid);

        // then
        assertEquals(20L, profileInfo.getFollows());
        assertEquals(10L, profileInfo.getFollowers());
    }

    @Test
    public void findByUuid_ThrowsWhenUserDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uuid)).willReturn(false);

        // then
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.findByUuid(uuid);
        }).getMessage();

        assertEquals(String.format("User with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void findByUuid_ThrowsWhenDatabaseFails() {
        UUID uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uuid)).willReturn(true);
        given(userRepository.findById(uuid)).willReturn(Optional.empty());

        // then
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.findByUuid(uuid);
        }).getMessage();

        assertEquals(String.format("User with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void findByUuid_ReturnsObject() throws Exception {
        User user = getTestUser();

        // given
        given(userRepository.existsById(user.getUuid())).willReturn(true);
        given(userRepository.findById(user.getUuid())).willReturn(Optional.of(user));

        // when
        User foundUser = userService.findByUuid(user.getUuid());

        //then
        assertNotNull(foundUser);
        assertEquals(user.getUuid(), foundUser.getUuid());
    }

    @Test
    public void checkIfUserFollows_ThrowsWhenUserDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uuid)).willReturn(false);

        // then
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.checkIfUserFollows(any(User.class), uuid);
        }).getMessage();

        assertEquals(String.format("User with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void checkIfUserFollows_ReturnsFalseWhenThereIsNoFollow() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = getTestUser();

        // given
        given(userRepository.existsById(uuid)).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), uuid)).willReturn(Optional.empty());

        // when
        boolean result = userService.checkIfUserFollows(user, uuid);

        // then
        assertFalse(result);
    }

    @Test
    public void checkIfUserFollows_ReturnsTrueWhenThereIsFollow() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = getTestUser();

        // given
        given(userRepository.existsById(uuid)).willReturn(true);
        given(userRepository.checkIfUserWithUuidFollows(user.getUuid(), uuid)).willReturn(Optional.of(1L));

        // when
        boolean result = userService.checkIfUserFollows(user, uuid);

        // then
        assertTrue(result);
    }

    @Test
    public void findByUsername_ThrowsWhenUserDoesntExist() {
        String invalidUsername = "test321";

        // given
        given(userRepository.findByUsername(invalidUsername)).willReturn(Optional.empty());

        // then
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.findByUsername(invalidUsername);
        }).getMessage();

        assertEquals(String.format("User %s doesn't exist", invalidUsername), message);
    }

    @Test
    public void findByUsername_ReturnsExistingObject() throws Exception {
        User user = getTestUser();

        // given
        given(userRepository.findByUsername(user.getUsername()))
                .willReturn(Optional.of(user));

        // when
        User receivedUser = userService.findByUsername(user.getUsername());

        // then
        assertEquals(user, receivedUser);
    }

    @Test
    public void findRecentPostsOfUser_ThrowsWhenUserDoesntExist() {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(false);

        // when
        String message = assertThrows(UserDoesntExistException.class, () -> {
            userService.findRecentPostsOfUser(uUuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals(String.format("User with UUID %s doesn't exist", uUuid), message);
    }

    @Test
    public void findRecentPostsOfUser_ThrowsWhenSkipAndLimitArgumentsNegative() {
        UUID uUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);

        // then
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findRecentPostsOfUser(uUuid, -1L, 0L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findRecentPostsOfUser(uUuid, 0L, -1L);
        });

        assertEquals("Invalid skip and/or limit values.", ex.getMessage());
    }

    @Test
    public void findRecentPostsOfUser_ReturnsObjects() throws Exception {
        UUID uUuid = UUID.randomUUID();

        List<UserPost> recent = List.of(new UserPost(), new UserPost());

        // given
        given(userRepository.existsById(uUuid)).willReturn(true);
        given(userRepository.findRecentPostsOfUser(uUuid, 0L, 5L))
                .willReturn(recent);

        // when
        List<UserPost> retrieved = userService.findRecentPostsOfUser(uUuid, 0L, 5L);

        // then
        assertEquals(2, retrieved.size());
    }

    @Test
    public void updateUser_UpdatesUserDetails() throws Exception {
        User testUser = getTestUser();

        UserDetailsDto dto = new UserDetailsDto();
        dto.setDisplayedUsername("My displayed username");
        dto.setDescription("This is my description");
        dto.setAviURL("");

        // given
        given(userRepository.save(testUser)).willReturn(testUser);

        // when
        User updated = userService.updateUser(testUser, dto);

        // then
        assertEquals(dto.getDisplayedUsername(), updated.getDisplayedUsername());
        assertEquals(dto.getDescription(), updated.getDescription());
        assertEquals(dto.getAviURL(), updated.getAviURL());
    }

    @Test
    public void findFollowersUserKnows_ThrowsWhenOtherUserDoesNotExist() {
        User testUser = getTestUser();
        UUID otherUserUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(otherUserUuid)).willReturn(false);

        // when
        String msg = assertThrows(UserDoesntExistException.class, () -> {
            userService.findFollowersUserKnows(testUser, otherUserUuid, 0L, 5L);
        }).getMessage();

        // then
        String expectedMsg = String.format("User with UUID %s doesn't exist", otherUserUuid.toString());
        assertEquals(expectedMsg, msg);
    }

    @Test
    public void findFollowersUserKnows_ThrowsWhenSkipOrLimitNegative() {
        User testUser = getTestUser();
        UUID otherUserUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(otherUserUuid)).willReturn(true);

        // then
        String ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findFollowersUserKnows(testUser, otherUserUuid, -1L, 5L);
        }).getMessage();

        assertEquals("Invalid skip and/or limit values.", ex);

        ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.findFollowersUserKnows(testUser, otherUserUuid, 0L, -1L);
        }).getMessage();

        assertEquals("Invalid skip and/or limit values.", ex);
    }

    @Test
    public void findFollowersUserKnows_ThrowsWhenUserTriesToCheckKnownFollowersWithThemselves() {
        User testUser = getTestUser();
        UUID otherUserUuid = testUser.getUuid();

        // given
        given(userRepository.existsById(otherUserUuid)).willReturn(true);

        // when
        String msg = assertThrows(IllegalArgumentException.class, () -> {
            userService.findFollowersUserKnows(testUser, otherUserUuid, 0L, 5L);
        }).getMessage();

        // then
        assertEquals("UUID of checked user is equal to the UUID of currently logged in user", msg);
    }

    @Test
    public void findFollowersUserKnows_ReturnsObjects() throws Exception {
        User testUser = getTestUser();
        UUID otherUserUuid = UUID.randomUUID();

        // given
        given(userRepository.existsById(otherUserUuid)).willReturn(true);
        given(userRepository.findFollowersUserKnows(testUser.getUuid(), otherUserUuid, 0L, 5L))
                .willReturn(List.of(new User(), new User()));

        // when
        List<User> common = userService.findFollowersUserKnows(testUser, otherUserUuid, 0L, 5L);

        // then
        assertEquals(2, common.size());
    }
}
