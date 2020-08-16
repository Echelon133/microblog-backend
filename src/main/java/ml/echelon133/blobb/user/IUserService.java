package ml.echelon133.blobb.user;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    boolean followUserWithUuid(User user, UUID followUuid) throws UserDoesntExistException;
    boolean unfollowUserWithUuid(User user, UUID unfollowUuid) throws UserDoesntExistException;
    List<User> findAllFollowedBy(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    List<User> findAllFollowing(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    UserProfileInfo getUserProfileInfo(UUID uuid) throws UserDoesntExistException;
}
