package ml.echelon133.blobb.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void throwIfUserDoesntExist(UUID uuid) throws UserDoesntExistException{
        if (!userRepository.existsById(uuid)) {
            throw new UserDoesntExistException(uuid);
        }
    }

    @Override
    public User findByUuid(UUID uuid) throws UserDoesntExistException {
        throwIfUserDoesntExist(uuid);
        Optional<User> user = userRepository.findById(uuid);
        if (user.isPresent()) {
            return user.get();
        }
        throw new UserDoesntExistException(uuid);
    }

    @Override
    public boolean followUserWithUuid(User user, UUID followUuid) throws UserDoesntExistException, IllegalArgumentException {
        throwIfUserDoesntExist(followUuid);
        Optional<Long> following = userRepository.checkIfUserWithUuidFollows(user.getUuid(), followUuid);

        if (user.getUuid().equals(followUuid)) {
            throw new IllegalArgumentException("Users cannot follow themselves.");
        }

        // only follow if there is no already existing 'follows' relationship
        // between these users
        // otherwise this will create duplicate relationships
        if (following.isEmpty()) {
            following = userRepository.followUserWithUuid(user.getUuid(), followUuid);
        }
        return following.isPresent();
    }

    @Override
    public boolean unfollowUserWithUuid(User user, UUID unfollowUuid) throws UserDoesntExistException {
        throwIfUserDoesntExist(unfollowUuid);
        userRepository.unfollowUserWithUuid(user.getUuid(), unfollowUuid);
        return userRepository.checkIfUserWithUuidFollows(user.getUuid(), unfollowUuid).isEmpty();
    }

    @Override
    public List<User> findAllFollowedBy(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException {
        throwIfUserDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return userRepository.findAllFollowedByUserWithUuid(uuid, skip, limit);
    }

    @Override
    public List<User> findAllFollowing(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException {
        throwIfUserDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return userRepository.findAllFollowingUserWithUuid(uuid, skip, limit);
    }

    @Override
    public UserProfileInfo getUserProfileInfo(UUID uuid) throws UserDoesntExistException {
        throwIfUserDoesntExist(uuid);
        return userRepository.getUserProfileInfo(uuid).orElse(new UserProfileInfo());
    }

    @Override
    public boolean checkIfUserFollows(User user, UUID followedUuid) throws UserDoesntExistException {
        throwIfUserDoesntExist(followedUuid);
        return userRepository.checkIfUserWithUuidFollows(user.getUuid(), followedUuid).isPresent();
    }
}
