package ml.echelon133.blobb.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<User> getUserByUsername(@RequestParam String username) throws Exception {
        User user = userService.findByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
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
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = userService.checkIfUserFollows(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("followed", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/follow")
    public ResponseEntity<Map<String, Boolean>> followUser(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = userService.followUserWithUuid(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("followed", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/unfollow")
    public ResponseEntity<Map<String, Boolean>> unfollowUser(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

    @GetMapping("/{uuid}/recentBlobbs")
    public ResponseEntity<List<UserBlobb>> getRecentBlobbs(@PathVariable String uuid,
                                                           @RequestParam(required = false) Long skip,
                                                           @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 10L;
        }

        return new ResponseEntity<>(
                userService.findRecentBlobbsOfUser(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }
}
