package ml.echelon133.microblog.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    private void throwIfUserDoesntExist(UUID uuid) throws UserDoesntExistException{
        if (!userRepository.existsById(uuid)) {
            throw new UserDoesntExistException(uuid);
        }
    }

    private Role getDefaultRole() {
        Optional<Role> defaultRole = roleRepository.findByName("ROLE_USER");
        if (defaultRole.isEmpty()) {
            Role role = new Role("ROLE_USER");
            return roleRepository.save(role);
        }
        return defaultRole.get();
    }

    @Override
    public User setupAndSaveUser(User newUser) throws UsernameAlreadyTakenException, UserCreationFailedException {
        if (userRepository.existsUserByUsername(newUser.getUsername())) {
            throw new UsernameAlreadyTakenException("Username already taken");
        }

        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        Role defaultRole = getDefaultRole();
        newUser.addRole(defaultRole);

        User savedUser = userRepository.save(newUser);

        // every user must follow themselves
        // this simplifies searching for actions of users while creating
        // their feeds
        Optional<Long> followId = userRepository.followUserWithUuid(savedUser.getUuid(), savedUser.getUuid());
        if (followId.isEmpty()) {
            // if followUserWithUuid fails, delete the account
            // because it hasn't been fully setup
            userRepository.delete(savedUser);
           throw new UserCreationFailedException("User creation failed");
        }
        return savedUser;
    }

    @Override
    public User findByUsername(String username) throws UserDoesntExistException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        }
        throw new UserDoesntExistException(username);
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
    public boolean followUserWithUuid(UserPrincipal user, UUID followUuid) throws UserDoesntExistException, IllegalArgumentException {
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
    public boolean unfollowUserWithUuid(UserPrincipal user, UUID unfollowUuid) throws UserDoesntExistException {
        throwIfUserDoesntExist(unfollowUuid);
        userRepository.unfollowUserWithUuid(user.getUuid(), unfollowUuid);
        return userRepository.checkIfUserWithUuidFollows(user.getUuid(), unfollowUuid).isEmpty();
    }

    @Override
    public List<User> findAllFollowsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException {
        throwIfUserDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return userRepository.findAllFollowsOfUserWithUuid(uuid, skip, limit);
    }

    @Override
    public List<User> findAllFollowersOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException {
        throwIfUserDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return userRepository.findAllFollowersOfUserWithUuid(uuid, skip, limit);
    }

    @Override
    public UserProfileInfo getUserProfileInfo(UUID uuid) throws UserDoesntExistException {
        throwIfUserDoesntExist(uuid);
        return userRepository.getUserProfileInfo(uuid).orElse(new UserProfileInfo());
    }

    @Override
    public boolean checkIfUserFollows(UserPrincipal user, UUID followedUuid) throws UserDoesntExistException {
        throwIfUserDoesntExist(followedUuid);
        return userRepository.checkIfUserWithUuidFollows(user.getUuid(), followedUuid).isPresent();
    }

    @Override
    public List<UserPost> findRecentPostsOfUser(UUID uuid, Long skip, Long limit) throws UserDoesntExistException, IllegalArgumentException {
        throwIfUserDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return userRepository.findRecentPostsOfUser(uuid, skip, limit);
    }

    @Override
    public User updateUser(User user, UserDetailsDto userDetailsDto) {
        user.setDisplayedUsername(userDetailsDto.getDisplayedUsername());
        user.setDescription(userDetailsDto.getDescription());
        user.setAviURL(userDetailsDto.getAviURL());
        return userRepository.save(user);
    }

    @Override
    public List<User> findAllByUsernameContains(String search) {
        return userRepository.findAllByUsernameContains(search);
    }
}
