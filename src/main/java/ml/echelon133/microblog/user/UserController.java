package ml.echelon133.microblog.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getUserByUsername(@RequestParam(required = false) String username,
                                                        @RequestParam(required = false) String search) throws Exception {
        List<User> users;
        if (username != null && search != null) {
            throw new IllegalArgumentException("Parameters username and search mustn't be combined");
        } else if (username != null) {
            users = List.of(userService.findByUsername(username));
        } else if (search != null) {
            users = userService.findAllByUsernameContains(search);
        } else {
            throw new IllegalArgumentException("Parameters username or search must be specified");
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getLoggedUser() throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User foundUser = userService.findByUsername(loggedUser.getUsername());
        return new ResponseEntity<>(foundUser, HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateUserDetails(@Valid @RequestBody UserDetailsDto userDetailsDto,
                                                  BindingResult result) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User foundUser = userService.findByUsername(loggedUser.getUsername());

        if (result.hasFieldErrors()) {
            List<String> messages = new ArrayList<>();
            for (FieldError fe : result.getFieldErrors()) {
                messages.add(fe.getDefaultMessage());
            }
            throw new InvalidUserDetailsFieldException(messages);
        }

        User updatedUser = userService.updateUser(foundUser, userDetailsDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, UUID>> registerUser(@Valid @RequestBody NewUserDto newUserDto, BindingResult result)
            throws NewUserDataInvalidException, UsernameAlreadyTakenException, UserCreationFailedException {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new NewUserDataInvalidException(errorMessages);
        }

        User user = new User(
                newUserDto.getUsername(),
                newUserDto.getEmail(),
                newUserDto.getPassword(),
                ""
        );
        User savedUser = userService.setupAndSaveUser(user);
        return new ResponseEntity<>(Map.of("uuid", savedUser.getUuid()), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<User> getUser(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(userService.findByUuid(UUID.fromString(uuid)), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/profile")
    public ResponseEntity<UserProfileInfo> getUserProfile(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(userService.getUserProfileInfo(UUID.fromString(uuid)), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/follow")
    public ResponseEntity<Map<String, Boolean>> checkIfFollowed(@PathVariable String uuid) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = userService.checkIfUserFollows(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("followed", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/follow")
    public ResponseEntity<Map<String, Boolean>> followUser(@PathVariable String uuid) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = userService.followUserWithUuid(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("followed", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/unfollow")
    public ResponseEntity<Map<String, Boolean>> unfollowUser(@PathVariable String uuid) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = userService.unfollowUserWithUuid(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("unfollowed", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{uuid}/followers")
    public ResponseEntity<List<User>> getFollowers(@PathVariable String uuid,
                                                   @RequestParam(required = false) Long skip,
                                                   @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 5L;
        }
        return new ResponseEntity<>(
                userService.findAllFollowersOfUser(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/follows")
    public ResponseEntity<List<User>> getFollows(@PathVariable String uuid,
                                                 @RequestParam(required = false) Long skip,
                                                 @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 5L;
        }
        return new ResponseEntity<>(
                userService.findAllFollowsOfUser(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/recentPosts")
    public ResponseEntity<List<UserPost>> getRecentPosts(@PathVariable String uuid,
                                                          @RequestParam(required = false) Long skip,
                                                          @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 10L;
        }

        return new ResponseEntity<>(
                userService.findRecentPostsOfUser(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }
}
