package ml.echelon133.microblog.user;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    User findByUsername(String username) throws UserDoesntExistException;
    List<User> findAllByUsernameContains(String search);
    User findByUuid(UUID uuid) throws UserDoesntExistException;
    boolean checkIfUserFollows(UserPrincipal user, UUID followedUuid) throws UserDoesntExistException;
    boolean followUserWithUuid(UserPrincipal user, UUID followUuid) throws UserDoesntExistException, IllegalArgumentException;
    boolean unfollowUserWithUuid(UserPrincipal user, UUID unfollowUuid) throws UserDoesntExistException;
    List<User> findAllFollowsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    List<User> findAllFollowersOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    UserProfileInfo getUserProfileInfo(UUID uuid) throws UserDoesntExistException;
    List<UserPost> findRecentPostsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    User setupAndSaveUser(User newUser) throws UsernameAlreadyTakenException, UserCreationFailedException;
    User updateUser(User user, UserDetailsDto userDetailsDto);
}
