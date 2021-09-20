package ml.echelon133.microblog.user.service;

import ml.echelon133.microblog.user.exception.UserCreationFailedException;
import ml.echelon133.microblog.user.exception.UserDoesntExistException;
import ml.echelon133.microblog.user.exception.UsernameAlreadyTakenException;
import ml.echelon133.microblog.user.exception.HiddenStateModificationAttemptException;
import ml.echelon133.microblog.user.model.*;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    User findByUsername(String username) throws UserDoesntExistException;
    List<User> findAllByUsernameContains(String search);
    User findByUuid(UUID uuid) throws UserDoesntExistException;
    List<User> findFollowersUserKnows(UserPrincipal user, UUID otherUser, Long skip, Long limit)
            throws UserDoesntExistException, IllegalArgumentException;
    boolean checkIfUserFollows(UserPrincipal user, UUID followedUuid) throws UserDoesntExistException;
    boolean followUserWithUuid(UserPrincipal user, UUID followUuid) throws UserDoesntExistException,
            HiddenStateModificationAttemptException;
    boolean unfollowUserWithUuid(UserPrincipal user, UUID unfollowUuid) throws UserDoesntExistException,
            HiddenStateModificationAttemptException;
    List<User> findAllFollowsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    List<User> findAllFollowersOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    UserProfileInfo getUserProfileInfo(UUID uuid) throws UserDoesntExistException;
    List<UserPost> findRecentPostsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException;
    User setupAndSaveUser(User newUser) throws UsernameAlreadyTakenException, UserCreationFailedException;
    User updateUser(User user, UserDetailsDto userDetailsDto);
}
