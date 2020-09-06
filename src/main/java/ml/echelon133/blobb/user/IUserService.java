package ml.echelon133.blobb.user;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    User findByUsername(String username) throws UserDoesntExistException;
    User findByUuid(UUID uuid) throws UserDoesntExistException;
    boolean checkIfUserFollows(User user, UUID followedUuid) throws UserDoesntExistException;
    boolean followUserWithUuid(User user, UUID followUuid) throws UserDoesntExistException, IllegalArgumentException;
    boolean unfollowUserWithUuid(User user, UUID unfollowUuid) throws UserDoesntExistException;
    List<User> findAllFollowsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    List<User> findAllFollowersOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    UserProfileInfo getUserProfileInfo(UUID uuid) throws UserDoesntExistException;
    List<UserBlobb> findRecentBlobbsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    User updateUser(User user, UserDetailsDto userDetailsDto);
}
